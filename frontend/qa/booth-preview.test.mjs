import test from 'node:test';
import assert from 'node:assert/strict';
import {boothProductId,boothCandidate,boothHtmlCandidate,handleBoothPreview} from '../booth-preview.ts';

const id='3403696',url='https://booth.pm/ja/items/'+id;
const product={id:Number(id),name:'鰯と柊【クトゥルフ神話TRPG/6版】',description:'シナリオ：○助\nNPC立ち絵：別の人',shop:{name:'ショップ'},images:[{original:'https://booth.pximg.net/test/i/'+id+'/cover.jpg'}]};
const request=(value=url,origin='https://toki989.github.io')=>new Request('https://preview.example/booth/preview?url='+encodeURIComponent(value),{headers:origin?{Origin:origin}:{}});
test('only product IDs from BOOTH URLs become fixed fetch destinations',async()=>{
 let calls=0;
 const response=await handleBoothPreview(request('https://shop.booth.pm/items/'+id+'?tracking=x#fragment'),{},async(target,options)=>{calls++;assert.equal(target,url+'.json');assert.equal(options.redirect,'manual');assert.equal(Object.hasOwn(options,'credentials'),false);assert.deepEqual(options.headers,{Accept:'application/json','User-Agent':'Takukairo/0.1 (user-requested BOOTH product preview)'});return Response.json(product);});
 assert.equal(calls,1);assert.equal(response.status,200);assert.equal(response.headers.get('Access-Control-Allow-Origin'),'https://toki989.github.io');assert.deepEqual(await response.json(),{name:product.name,authorName:'○助',gameSystem:'クトゥルフ神話TRPG 6版',sourceUrl:url,externalImageUrl:product.images[0].original});
 for(const value of ['https://booth.pm.evil.test/items/'+id,'https://user@booth.pm/items/'+id,'http://localhost:8080/api/session','file:///etc/passwd',url+'/more'])assert.equal(boothProductId(value),null);
});
test('unapproved origins and invalid URLs do not trigger outbound requests',async()=>{
 const forbidden=()=>{throw new Error('must not fetch');};
 assert.equal((await handleBoothPreview(request(url,'https://unrelated.example'),{},forbidden)).status,403);
 const invalid=await handleBoothPreview(request('https://example.com/items/'+id),{},forbidden);assert.equal(invalid.status,400);assert.equal((await invalid.json()).fieldErrors[0].path,'/url');
});
test('redirects are rejected and binary or oversized responses are not parsed',async()=>{
 for(const reply of [new Response(null,{status:302,headers:{Location:'http://localhost:8080/'}}),new Response('binary',{headers:{'Content-Type':'image/png'}}),new Response('x'.repeat(2*1024*1024+1),{headers:{'Content-Type':'application/json'}})]){
  const response=await handleBoothPreview(request(),{},async()=>reply);assert.equal(response.status,502);
 }
});
test('missing images and unclear editions remain unset, foreign images are never returned',()=>{
 const result=boothCandidate(id,{...product,name:'クトゥルフ神話TRPG シナリオ',description:'',images:[{original:'http://localhost/image.jpg'}]});assert.equal(result.externalImageUrl,null);assert.equal(result.gameSystem,null);assert.equal(result.authorName,'ショップ');
 assert.throws(()=>boothCandidate('1',product));
});
test('fetch errors become readable responses without storing any application data',async()=>{
 const response=await handleBoothPreview(request(),{},async()=>{throw new Error('unavailable');});assert.equal(response.status,502);assert.equal((await response.json()).code,'BOOTH_FETCH_FAILED');assert.equal(response.headers.get('Cache-Control'),'no-store');
});
test('ordinary public HTML can provide the same candidates when JSON is unavailable',async()=>{
 const embedded={'@type':'Product',name:product.name,description:product.description,url,brand:product.shop,image:product.images[0].original};
 const html='<script type="application/ld+json">'+JSON.stringify(embedded)+'</script><img class="market-item-detail-item-image" data-origin="https://booth.pximg.net/test/i/'+id+'/original.png">';
 const calls=[];
 const reply=await handleBoothPreview(request(),{},async(target)=>{calls.push(target);return target.endsWith('.json')?new Response('Forbidden',{status:403}):new Response(html,{headers:{'Content-Type':'text/html; charset=utf-8'}});});
 assert.equal(reply.status,200);const candidate=await reply.json();assert.equal(candidate.name,product.name);assert.equal(candidate.authorName,'○助');assert.ok(candidate.externalImageUrl.endsWith('/original.png'));assert.deepEqual(calls,[url+'.json',url]);assert.throws(()=>boothHtmlCandidate('1',html));
});
test('a denial from both public formats is reported without any further requests',async()=>{
 let calls=0;const reply=await handleBoothPreview(request(),{},async()=>{calls++;return new Response('Forbidden',{status:403});});assert.equal(reply.status,502);assert.equal(calls,2);assert.deepEqual((await reply.json()).upstreamStatuses,{json:403,html:403});
});

test('denial diagnostics identify challenges without exposing provider bodies or cookies',async()=>{
 const reply=await handleBoothPreview(request(),{},async()=>new Response('<title>Just a moment...</title><script>window._cf_chl_opt = {secret:"private-token",ip:"192.0.2.1"}</script>',{status:403,headers:{'Content-Type':'text/html','Server':'cloudflare','cf-mitigated':'challenge','cf-ray':'test-NRT','Set-Cookie':'private-session=secret','X-Private':'private-header'}}));
 const result=await reply.json(),diagnostic=result.upstreamDiagnostics.json;
 assert.equal(diagnostic.classification,'cloudflare-challenge');assert.equal(diagnostic.headers['cf-ray'],'test-NRT');
 assert.equal(diagnostic.pageSummary.title,'Just a moment...');
 const serialized=JSON.stringify(result);for(const value of ['private-token','192.0.2.1','private-session','private-header'])assert.equal(serialized.includes(value),false);
});

test('denial diagnostics distinguish error pages from an unknown 403 and bound inspection',async()=>{
 for(const [body,classification,errorCode] of [['<div class="cf-error-details">Error code: 1020</div>','provider-error-page','1020'],['Forbidden','unclassified-denial',null]]){
  const reply=await handleBoothPreview(request(),{},async()=>new Response(body,{status:403,headers:{Server:'cloudflare'}}));
  const diagnostic=(await reply.json()).upstreamDiagnostics.html;assert.equal(diagnostic.classification,classification);assert.equal(diagnostic.errorCode,errorCode);
 }
 let canceled=0;
 const reply=await handleBoothPreview(request(),{},async()=>new Response(new ReadableStream({start(controller){controller.enqueue(new TextEncoder().encode('x'.repeat(2*1024*1024+1)));},cancel(){canceled++;}}),{status:403}));
 const diagnostic=(await reply.json()).upstreamDiagnostics.json;assert.equal(diagnostic.bodyInspectedBytes,2*1024*1024);assert.equal(diagnostic.truncated,true);assert.equal(canceled,2);
});

test('block messages after large inline styles are inspected without returning page text',async()=>{
 const html='<style>.cf-error-details{--ignored:"Error 1006 Forbidden"}'+'.padding{}'.repeat(3000)+'</style><h1>Sorry, you have been blocked</h1><p>This website is using a security service to protect itself from online attacks.</p><p>private IP 192.0.2.1</p>';
 const reply=await handleBoothPreview(request(),{},async()=>new Response(html,{status:403,headers:{Server:'cloudflare'}}));
 const result=await reply.json(),diagnostic=result.upstreamDiagnostics.html;
 assert.equal(diagnostic.classification,'cloudflare-block');assert.equal(diagnostic.errorCode,null);assert.equal(diagnostic.knownPage,'sorry-you-have-been-blocked');assert.equal(diagnostic.signals.securityService,true);assert.equal(diagnostic.truncated,false);assert.ok(diagnostic.bodyInspectedBytes>16384);assert.equal(JSON.stringify(result).includes('192.0.2.1'),false);
});

test('a broken denial body retains the upstream status as diagnostic evidence',async()=>{
 const reply=await handleBoothPreview(request(),{},async()=>new Response(new ReadableStream({start(controller){controller.error(new Error('private connection detail'));}}),{status:403}));
 const result=await reply.json();assert.deepEqual(result.upstreamStatuses,{json:403,html:403});assert.equal(result.upstreamDiagnostics.json.readFailed,true);assert.equal(JSON.stringify(result).includes('private connection detail'),false);
});

test('error summaries preserve nonstandard Japanese text and redact identifying details',async()=>{
 const html='<title>&#x30A8;ラー</title><body><style>.cf-error-details{}</style><svg><text>private vector text</text></svg><h1>アクセスを確認できません</h1><p>この環境からのアクセスは制限されています。</p><p>192.0.2.1 2001:db8::1 person@example.com https://example.com/?token=private-url-token</p><script>private-script-token</script></body>';
 const reply=await handleBoothPreview(request(),{},async()=>new Response(html,{status:403}));
 const result=await reply.json(),summary=result.upstreamDiagnostics.html.pageSummary;
 assert.equal(summary.title,'エラー');assert.deepEqual(summary.headings,['アクセスを確認できません']);assert.ok(summary.visibleText.includes('この環境からのアクセスは制限されています。'));
 for(const value of ['192.0.2.1','2001:db8::1','person@example.com','private-url-token','private-script-token','private vector text'])assert.equal(JSON.stringify(result).includes(value),false);
 assert.ok(summary.visibleText.length<=1800);
});

/** Public metadata only, shared by local demo serving and the optional stateless Worker. */
export interface BoothPreviewEnvironment { ALLOWED_ORIGINS?: string }
type Candidate={name:string;authorName:string|null;gameSystem:string|null;sourceUrl:string;externalImageUrl:string|null};
const MAX_BYTES=2*1024*1024;
const text=(value:unknown)=>typeof value==='string'&&value.trim()?value.trim():null;
const object=(value:unknown):Record<string,unknown>=>value!=null&&typeof value==='object'&&!Array.isArray(value)?value as Record<string,unknown>:{};

export function boothProductId(value:unknown):string|null {
 try{
  if(typeof value!=='string'||value.length>2048)return null;
  const url=new URL(value),host=url.hostname.toLowerCase();
  if(!['http:','https:'].includes(url.protocol)||url.username||url.password||(host!=='booth.pm'&&!host.endsWith('.booth.pm')))return null;
  return url.pathname.match(/^\/(?:ja\/)?items\/([0-9]{1,15})$/)?.[1]??null;
 }catch{return null;}
}
function system(value:string):string|null {
 const normalized=value.normalize('NFKC');
 const coc=/クトゥルフ|\bCoC\b|\bCoC[67]/i.test(normalized),emoklore=normalized.includes('エモクロア');
 if(emoklore)return coc?null:'エモクロアTRPG';
 if(!coc)return null;
 const six=/6\s*版|第\s*6\s*版|CoC\s*6(?:th)?\b/i.test(normalized),seven=/7\s*版|第\s*7\s*版|CoC\s*7(?:th)?\b|新クトゥルフ/i.test(normalized);
 return six===seven?null:six?'クトゥルフ神話TRPG 6版':'クトゥルフ神話TRPG 7版';
}
export function boothCandidate(id:string,value:unknown):Candidate {
 const product=object(value),name=text(product.name);
 if(String(product.id)!==id||!name||name.length>255)throw new Error('INVALID_PRODUCT');
 const description=text(product.description)??'';
 const author=description.match(/^\s*(?:シナリオ(?:作者|制作)?|作者|著者)[ \t]*[:：][ \t]*([^\r\n]+)/m)?.[1]?.trim();
 const image=Array.isArray(product.images)?object(product.images[0]):{};
 let imageUrl:string|null=null;
 try{
  const url=new URL(text(image.original)??'');
  if(url.protocol==='https:'&&url.hostname==='booth.pximg.net'&&!url.username&&!url.password&&(!url.port||url.port==='443')&&url.pathname.includes('/i/'+id+'/')&&url.href.length<=2048)imageUrl=url.href;
 }catch{/* A missing image does not prevent product metadata use. */}
 const explicitSystem=description.match(/^\s*(?:ゲームシステム|システム|対応ルール|ルールブック)[ \t]*[:：][ \t]*([^\r\n]+)/m)?.[1]??'';
 const authorName=text(author)??text(object(product.shop).name);
 return {name,authorName:authorName&&authorName.length<=255?authorName:null,gameSystem:system(name)??system(explicitSystem),sourceUrl:'https://booth.pm/ja/items/'+id,externalImageUrl:imageUrl};
}
export function boothHtmlCandidate(id:string,html:string):Candidate {
 function productNode(value:unknown):Record<string,unknown>|null {
  if(Array.isArray(value)){for(const child of value){const found=productNode(child);if(found)return found;}return null;}
  const node=object(value),type=node['@type'];
  if((type==='Product'||Array.isArray(type)&&type.includes('Product'))&&boothProductId(node.url)===id)return node;
  return node['@graph']?productNode(node['@graph']):null;
 }
 let product:Record<string,unknown>|null=null;
 for(const script of html.matchAll(/<script\b[^>]*\btype\s*=\s*["']application\/ld\+json["'][^>]*>([\s\S]*?)<\/script\s*>/gi)){
  try{product=productNode(JSON.parse(script[1]));if(product)break;}catch{/* Ignore unrelated or malformed structured data. */}
 }
 if(!product)throw new Error('INVALID_PRODUCT');
 let image:string|null=null;
 const attribute=(tag:string,name:string)=>tag.match(new RegExp('\\s'+name+'\\s*=\\s*(["\'])(.*?)\\1','i'))?.[2]?.replace(/&amp;/g,'&')??null;
 for(const match of html.matchAll(/<img\b[^>]*>/gi)){
  if(!attribute(match[0],'class')?.split(/\s+/).includes('market-item-detail-item-image'))continue;
  image=attribute(match[0],'data-origin')??attribute(match[0],'src')??attribute(match[0],'data-lazy');if(image)break;
 }
 if(!image){const value=Array.isArray(product.image)?product.image[0]:product.image;image=text(value)??text(object(value).url);}
 return boothCandidate(id,{...product,id:Number(id),images:image?[{original:image}]:[],shop:object(product.brand)});
}
async function responseText(response:Response):Promise<string> {
 const reader=response.body?.getReader();if(!reader)throw new Error('EMPTY_RESPONSE');
 const decoder=new TextDecoder();let body='',size=0;
 try{for(;;){const chunk=await reader.read();if(chunk.done)break;size+=chunk.value.length;if(size>MAX_BYTES){await reader.cancel();throw new Error('RESPONSE_TOO_LARGE');}body+=decoder.decode(chunk.value,{stream:true});}return body+decoder.decode();}finally{reader.releaseLock();}
}
async function denialDiagnostics(response:Response) {
 // Expose selected headers and bounded, redacted visible error-page text.
 // Never return raw HTML, scripts, Set-Cookie, or visitor request headers.
 const headers:Record<string,string>={};
 for(const name of ['content-type','server','cf-mitigated','cf-ray','retry-after']){
  const value=response.headers.get(name);if(value)headers[name]=value.slice(0,160);
 }
 const reader=response.body?.getReader(),decoder=new TextDecoder();let body='',size=0,readFailed=false,truncated=false;
 try{
  if(reader)for(;;){
   const chunk=await reader.read();if(chunk.done)break;
   const remaining=MAX_BYTES-size;body+=decoder.decode(chunk.value.subarray(0,remaining),{stream:true});size+=Math.min(chunk.value.length,remaining);
   if(size>=MAX_BYTES){truncated=true;break;}
  }
  body+=decoder.decode();
 }catch{readFailed=true;}
 finally{if(reader){try{await reader.cancel();}catch{/* Preserve the original denial. */}reader.releaseLock();}}
 const challenge=headers['cf-mitigated']==='challenge'||/\/cdn-cgi\/challenge-platform\/|_cf_chl_opt\s*=/.test(body);
 // Cloudflare pages may place the actual message after large inline styles/SVGs.
 const clean=(value:string)=>value.replace(/<!--[\s\S]*?-->/g,' ').replace(/<(style|script|svg)\b[^>]*>[\s\S]*?<\/\1\s*>/gi,' ').replace(/<[^>]*>/g,' ')
  .replace(/&#(x[0-9a-f]+|[0-9]+);/gi,(_,code:string)=>{const point=code[0].toLowerCase()==='x'?parseInt(code.slice(1),16):parseInt(code,10);return point>0&&point<=0x10ffff?String.fromCodePoint(point):' ';})
  .replace(/&(amp|lt|gt|quot|apos|nbsp);/gi,(_,name:string)=>({amp:'&',lt:'<',gt:'>',quot:'"',apos:"'",nbsp:' '})[name.toLowerCase()]??' ')
  .replace(/https?:\/\/[^\s<>]+/gi,'[URL]').replace(/\b[a-z0-9_+\/-]{40,}={0,2}/gi,'[token]')
  .replace(/[a-z0-9.!#$%&'*+\/=?^_`{|}~-]{1,64}@[a-z0-9.-]{1,253}\.[a-z]{2,24}/gi,'[email]')
  .replace(/\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b/g,'[IP]').replace(/(?<![a-f0-9:])(?:[a-f0-9]{0,4}:){2,}[a-f0-9]{0,4}(?![a-f0-9:])/gi,'[IP]')
  .replace(/\s+/g,' ').trim();
 const visible=clean(body);
 const pageSummary={title:clean(body.match(/<title\b[^>]*>([\s\S]*?)<\/title\s*>/i)?.[1]??'').slice(0,200)||null,headings:[...body.matchAll(/<h[1-3]\b[^>]*>([\s\S]*?)<\/h[1-3]\s*>/gi)].slice(0,6).map(match=>clean(match[1]).slice(0,240)),visibleText:clean(body.match(/<body\b[^>]*>([\s\S]*?)<\/body\s*>/i)?.[1]??body).slice(0,1800)};
 const errorCode=visible.match(/\berror\s*(?:code\s*)?[:：]?\s*(1[0-9]{3})\b/i)?.[1]??body.match(/cf-error-code[^>]*>\s*(1[0-9]{3})\b/i)?.[1]??null;
 const signals={cloudflareError:/cf-error-details|cloudflare-error|Cloudflare Ray ID/i.test(body),accessDenied:/access denied|attention required|request (?:was )?blocked|forbidden|you have been blocked/i.test(visible),securityService:/security service to protect itself from online attacks/i.test(visible),ipBanned:/your IP (?:address )?has been banned/i.test(visible),cookiesRequired:/please enable cookies/i.test(visible)};
 const knownPage=/sorry,? you have been blocked/i.test(visible)?'sorry-you-have-been-blocked':/attention required/i.test(visible)?'attention-required':/just a moment/i.test(visible)?'just-a-moment':null;
 return {status:response.status,headers,classification:challenge?'cloudflare-challenge':errorCode?'provider-error-page':signals.cloudflareError&&signals.accessDenied?'cloudflare-block':'unclassified-denial',errorCode,knownPage,signals,pageSummary,bodyInspectedBytes:size,truncated,readFailed};
}
function result(status:number,body:unknown,origin:string|null):Response {
 const headers=new Headers({'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff','Vary':'Origin'});
 if(origin)headers.set('Access-Control-Allow-Origin',origin);
 return new Response(JSON.stringify(body),{status,headers});
}
export async function handleBoothPreview(request:Request,env:BoothPreviewEnvironment={},fetcher:typeof fetch=fetch):Promise<Response> {
 const allowed=(env.ALLOWED_ORIGINS??'https://toki989.github.io').split(',').map(value=>value.trim());
 const origin=request.headers.get('Origin');
 if(origin&&!allowed.includes(origin))return result(403,{code:'ORIGIN_NOT_ALLOWED',message:'このページからは商品情報を取得できません。',fieldErrors:[]},null);
 const fail=(status:number,code:string,message:string,path?:string)=>result(status,{code,message,fieldErrors:path?[{path,code,message}]:[]},origin);
 if(request.method==='OPTIONS')return new Response(null,{status:204,headers:{'Access-Control-Allow-Origin':origin??allowed[0],'Access-Control-Allow-Methods':'GET, OPTIONS','Access-Control-Allow-Headers':'Accept','Vary':'Origin'}});
 if(request.method!=='GET')return fail(405,'METHOD_NOT_ALLOWED','商品情報の取得だけを利用できます。');
 const query=new URL(request.url);
 if(query.pathname==='/')return result(200,{service:'takukairo-booth-preview',ready:true,version:'2026-10-28-error-page-summary'},origin);
 if(!['/booth/preview','/__booth/preview'].includes(query.pathname))return fail(404,'NOT_FOUND','取得先が見つかりません。');
 const id=boothProductId(query.searchParams.get('url'));
 if(!id)return fail(400,'INVALID_BOOTH_URL','BOOTHの商品URLを入力してください。','/url');
 const controller=new AbortController(),abort=()=>controller.abort(),timer=setTimeout(abort,18000);
 request.signal.addEventListener('abort',abort,{once:true});
 try{
  if(request.signal.aborted)controller.abort();
  const response=await fetcher('https://booth.pm/ja/items/'+id+'.json',{method:'GET',redirect:'manual',headers:{'Accept':'application/json','User-Agent':'Takukairo/0.1 (user-requested BOOTH product preview)'},signal:controller.signal});
  if([403,404,406,415].includes(response.status)){
   const jsonDiagnostics=await denialDiagnostics(response);
   // Try the ordinary public product page; never use cookies, challenges or an authentication bypass.
   const page=await fetcher('https://booth.pm/ja/items/'+id,{method:'GET',redirect:'manual',headers:{'Accept':'text/html','User-Agent':'Takukairo/0.1 (user-requested BOOTH product preview)'},signal:controller.signal});
   if(!page.ok)return result(502,{code:'BOOTH_FETCH_FAILED',message:'BOOTHの商品情報を取得できませんでした。手入力で続けてください。',fieldErrors:[],upstreamStatus:page.status,upstreamStatuses:{json:response.status,html:page.status},upstreamDiagnostics:{json:jsonDiagnostics,html:await denialDiagnostics(page)}},origin);
   if(!page.headers.get('content-type')?.toLowerCase().startsWith('text/html'))throw new Error('INVALID_CONTENT_TYPE');
   return result(200,boothHtmlCandidate(id,await responseText(page)),origin);
  }
  if(!response.ok)return result(502,{code:'BOOTH_FETCH_FAILED',message:'BOOTHの商品情報を取得できませんでした。商品ページを確認するか、手入力で続けてください。',fieldErrors:[],upstreamStatus:response.status,upstreamDiagnostics:{json:await denialDiagnostics(response)}},origin);
  if(!response.headers.get('content-type')?.toLowerCase().startsWith('application/json'))return fail(502,'BOOTH_FETCH_FAILED','BOOTHの商品情報を読み取れませんでした。手入力で続けてください。');
  return result(200,boothCandidate(id,JSON.parse(await responseText(response))),origin);
 }catch(error){
  if(error instanceof Error&&error.message==='RESPONSE_TOO_LARGE')return fail(502,'BOOTH_RESPONSE_TOO_LARGE','BOOTHの応答が大きすぎるため取得を中止しました。');
  return fail(controller.signal.aborted?504:502,controller.signal.aborted?'BOOTH_FETCH_TIMEOUT':'BOOTH_FETCH_FAILED',controller.signal.aborted?'BOOTHへの接続が時間切れになりました。入力を保持しています。':'BOOTHの商品情報を取得できませんでした。手入力で続けてください。');
 }finally{clearTimeout(timer);request.signal.removeEventListener('abort',abort);}
}

// Cloudflare passes ExecutionContext as the third argument, so wrap rather than export the handler directly.
export default {fetch:(request:Request,env:BoothPreviewEnvironment)=>handleBoothPreview(request,env)};


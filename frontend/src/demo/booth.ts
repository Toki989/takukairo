import {ApiError} from '../api/client';

export function demoBoothEndpoint():string|null {
 const configured=import.meta.env.VITE_BOOTH_PREVIEW_URL?.trim();
 if(configured){try{const url=new URL(configured);if(url.protocol==='https:'&&!url.username&&!url.password)return url.href;}catch{/* Invalid deployment configuration is not used. */}return null;}
 return ['localhost','127.0.0.1','[::1]'].includes(location.hostname)?new URL('__booth/preview',new URL(import.meta.env.BASE_URL,location.href)).href:null;
}
export async function demoBoothPreview(value:unknown,signal?:AbortSignal):Promise<unknown> {
 const endpoint=demoBoothEndpoint();
 if(!endpoint)throw new ApiError(503,{code:'BOOTH_NOT_CONFIGURED',message:'この体験版ではBOOTHの商品情報の自動取得を利用できません。手入力で続けられます。',fieldErrors:[]});
 let canonical:string;
 try{
  if(typeof value!=='string'||value.length>2048)throw new Error();
  const input=new URL(value),host=input.hostname.toLowerCase(),id=input.pathname.match(/^\/(?:ja\/)?items\/([0-9]{1,15})$/)?.[1];
  if(!['http:','https:'].includes(input.protocol)||input.username||input.password||(host!=='booth.pm'&&!host.endsWith('.booth.pm'))||!id)throw new Error();
  canonical='https://booth.pm/ja/items/'+id;
 }catch{throw new ApiError(400,{code:'INVALID_BOOTH_URL',message:'BOOTHの商品URLを入力してください。',fieldErrors:[{path:'/url',code:'INVALID_BOOTH_URL',message:'BOOTHの商品URLを入力してください。'}]});}
 const target=new URL(endpoint);target.searchParams.set('url',canonical);
 const controller=new AbortController(),abort=()=>controller.abort();
 signal?.addEventListener('abort',abort,{once:true});if(signal?.aborted)controller.abort();
 const timer=setTimeout(abort,22000);
 try{
  const response=await fetch(target,{method:'GET',credentials:'omit',referrerPolicy:'no-referrer',headers:{Accept:'application/json'},signal:controller.signal});
  const body=await response.json();
  if(!response.ok)throw new ApiError(response.status,body&&typeof body.code==='string'&&Array.isArray(body.fieldErrors)?body:{code:'BOOTH_FETCH_FAILED',message:'BOOTHの商品情報を取得できませんでした。入力を保持しています。',fieldErrors:[]});
  return body;
 }catch(error){if(error instanceof ApiError||signal?.aborted)throw error;throw new ApiError(0,{code:'BOOTH_FETCH_FAILED',message:'BOOTHの商品情報を取得できませんでした。入力を保持しています。手入力でも続けられます。',fieldErrors:[]});}
 finally{clearTimeout(timer);signal?.removeEventListener('abort',abort);}
}

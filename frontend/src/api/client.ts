import type {ErrorBody} from './types';
export class ApiError extends Error {constructor(public status:number,public body:ErrorBody){super(body.message)}get unknownOutcome(){return this.status===0||this.status>=500||this.body.code==='PARSE_ERROR'}}
let csrf:{token:string;headerName:string;parameterName:string}|null=null;
export async function freshCsrf(){csrf=await apiClient<typeof csrf>('/csrf');return csrf}
export function clearCsrf(){csrf=null}
export async function apiClient<T>(path:string,options:RequestInit={}):Promise<T>{
 const method=options.method??'GET';const headers=new Headers(options.headers);if(options.body&&!(options.body instanceof FormData))headers.set('Content-Type','application/json');if(!['GET','HEAD','OPTIONS'].includes(method)){if(!csrf)await freshCsrf();if(csrf)headers.set(csrf.headerName,csrf.token)}
 const controller=new AbortController();let timedOut=false;const timer=setTimeout(()=>{timedOut=true;controller.abort()},45000);const cancelled=()=>controller.abort();options.signal?.addEventListener('abort',cancelled,{once:true});
 try{
 const response=await fetch('/api'+path,{...options,signal:controller.signal,headers,credentials:'include'});
 if(!response.ok){let body:ErrorBody;try{body=await response.json();if(!Array.isArray(body.fieldErrors)||!body.code)throw new Error()}catch{if(controller.signal.aborted)throw new Error('aborted');body={code:response.status===413?'PAYLOAD_TOO_LARGE':'HTTP_ERROR',message:response.status===413?'送信データが大きすぎます。':`処理を完了できませんでした（${response.status}）。`,fieldErrors:[]}}const e=new ApiError(response.status,body);if(response.status===401)window.dispatchEvent(new CustomEvent('session-expired'));throw e}
 if(response.status===204)return undefined as T;try{return await response.json() as T}catch{if(controller.signal.aborted)throw new Error('aborted');throw new ApiError(response.status,{code:'PARSE_ERROR',message:'応答を読み取れませんでした。',fieldErrors:[]})}
 }catch(e){if(e instanceof ApiError||options.signal?.aborted)throw e;throw new ApiError(0,{code:timedOut?'TRANSPORT_TIMEOUT':'TRANSPORT_ERROR',message:timedOut?'通信の応答を確認できませんでした。入力内容は保持しています。':'通信できませんでした。入力内容は保持しています。',fieldErrors:[]})}finally{clearTimeout(timer);options.signal?.removeEventListener('abort',cancelled)}
}
export const get=<T>(path:string,signal?:AbortSignal)=>apiClient<T>(path,{signal});
export const mutate=<T>(path:string,method:string,body?:unknown)=>apiClient<T>(path,{method,body:body instanceof FormData?body:body===undefined?undefined:JSON.stringify(body)});

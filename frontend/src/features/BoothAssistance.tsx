import {useContext,useEffect,useRef,useState} from 'react';
import {apiClient,ApiError} from '../api/client';
import {ConfirmContext,ErrorPanel,Field,fieldProps,useErrors} from '../components/common';

type Draft={name:string;authorName:string;gameSystem:string;sourceUrl:string;externalImageUrl:string};
type Candidate={name:string;authorName:string|null;gameSystem:string|null;sourceUrl:string;externalImageUrl:string|null};
const labels={name:'シナリオ名',authorName:'作者名',gameSystem:'ゲームシステム',sourceUrl:'シナリオURL'};
type Key=keyof typeof labels;
const keys=Object.keys(labels) as Key[];

export function BoothAssistance({draft,onApply}:{draft:Draft;onApply:(values:Partial<Draft>)=>void}){
 const [url,setUrl]=useState(''),[busy,setBusy]=useState(false),[candidate,setCandidate]=useState<Candidate|null>(null);
 const [selected,setSelected]=useState<Key[]>([]),[notice,setNotice]=useState('');
 const {error,report,clear}=useErrors(),confirm=useContext(ConfirmContext);
 const pending=useRef<AbortController|null>(null),latest=useRef(draft);latest.current=draft;
 useEffect(()=>()=>pending.current?.abort(),[]);
 function changeUrl(value:string){pending.current?.abort();pending.current=null;setBusy(false);setUrl(value);setCandidate(null);setNotice('');clear();}
 async function fetchCandidate(){
  pending.current?.abort();const request=new AbortController();pending.current=request;
  setBusy(true);setCandidate(null);setNotice('');clear();
  try{
   const result=await apiClient<Candidate>('/booth/preview',{method:'POST',body:JSON.stringify({url:url.trim()}),signal:request.signal});
   if(request.signal.aborted)return;
   if(!result||typeof result.name!=='string'||typeof result.sourceUrl!=='string')throw new ApiError(502,{code:'INVALID_BOOTH_RESPONSE',message:'商品情報を読み取れませんでした。手入力で続けてください。',fieldErrors:[]});
   setCandidate(result);setSelected(keys.filter(key=>!!result[key]&&!latest.current[key]));
  }catch(e){if(!request.signal.aborted)report(e);}finally{if(pending.current===request){pending.current=null;setBusy(false);}}
 }
 async function apply(image=false){
  if(!candidate)return;
  const values:Partial<Draft>=image?{externalImageUrl:candidate.externalImageUrl??''}:Object.fromEntries(selected.filter(key=>candidate[key]!=null).map(key=>[key,candidate[key]]));
  const replacing=Object.entries(values).some(([key,value])=>latest.current[key as keyof Draft]&&latest.current[key as keyof Draft]!==value);
  if(replacing&&!await confirm('入力済みの内容を、選択したBOOTHの候補で上書きしますか？'))return;
  onApply(values);setNotice(image?'画像候補を選択しました。保存するまでは登録されません。':'選択した情報を入力欄へ反映しました。内容を確認して保存してください。');
 }
 return <details className="booth-assistance"><summary>BOOTHから入力を補助</summary>
  <p>商品URLから情報を取得します。取得だけではシナリオを保存しません。</p>
  <ErrorPanel error={error}/>
  <Field label="BOOTHの商品URL" path="/url" error={error}><input {...fieldProps('/url',error)} type="text" inputMode="url" placeholder="https://booth.pm/ja/items/…" value={url} onChange={e=>changeUrl(e.target.value)}/></Field>
  <button type="button" disabled={busy||!url.trim()} onClick={()=>void fetchCandidate()}>{busy?'取得しています…':'情報を取得'}</button>
  {candidate&&<section aria-label="BOOTHの商品情報候補"><h2>取得した候補</h2><p>入力する項目を選んでください。作者名にはショップ名が表示される場合があります。</p>
   {keys.map(key=><label className="booth-candidate" key={key}><input type="checkbox" aria-label={labels[key]+'を反映'} checked={selected.includes(key)} disabled={!candidate[key]} onChange={e=>setSelected(current=>e.target.checked?[...current,key]:current.filter(value=>value!==key))}/><span><strong>{labels[key]}</strong><br/>{candidate[key]??'取得できませんでした'}</span></label>)}
   <button type="button" disabled={!selected.length} onClick={()=>void apply()}>選択した情報を入力欄へ反映</button>
   {candidate.externalImageUrl&&<details><summary>商品画像の候補を確認</summary><img className="booth-image-candidate" src={candidate.externalImageUrl} alt="BOOTHから取得した商品画像の候補" referrerPolicy="no-referrer"/><p>画像は配信元から表示します。アップロード画像がある場合は、そちらが優先されます。</p><button type="button" onClick={()=>void apply(true)}>この画像を使用</button></details>}
  </section>}
  {notice&&<p role="status">{notice}</p>}
 </details>;
}

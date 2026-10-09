import {request} from '@playwright/test';
import fs from 'node:fs';import path from 'node:path';import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const ctx=await request.newContext({baseURL:'http://localhost:5173/api'});
let csrf=await(await ctx.get('/api/csrf')).json();await ctx.post('/api/dev/session',{headers:{[csrf.headerName]:csrf.token}});csrf=await(await ctx.get('/api/csrf')).json();
for(const[kind,dir,count]of [['pcs','テスト用PCasset',10],['scenarios','シナリオトレーラー画像テスト用asset',9]]){const files=fs.readdirSync(path.join(root,dir)).filter(n=>n.endsWith('.png')).sort();for(let id=1;id<=count;id++){const item=await(await ctx.get('/api/'+kind+'/'+id)).json();if(item.image)continue;const name=files[(id-1)%files.length];const res=await ctx.put('/api/'+kind+'/'+id+'/image',{headers:{[csrf.headerName]:csrf.token},multipart:{file:{name,mimeType:'image/png',buffer:fs.readFileSync(path.join(root,dir,name))},expectedVersion:String(item.version),...(kind==='pcs'?{positionX:'0',positionY:'0',zoom:'1'}:{})}});console.log(kind,id,res.status(),res.ok()?'saved':await res.text());}}
await ctx.dispose();

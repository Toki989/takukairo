import {defineConfig,loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
import {handleBoothPreview} from './booth-preview';
function configureDemoBooth(server:Pick<import('vite').ViteDevServer,'middlewares'>){
 server.middlewares.use(async(req,res,next)=>{
  const message=req as unknown as {url?:string;method?:string};
  const url=new URL(message.url??'/','http://localhost');
  if(url.pathname!=='/__booth/preview')return next();
  try{
   const request=new Request('http://localhost/__booth/preview'+url.search,{method:message.method});
   const reply=await handleBoothPreview(request);res.statusCode=reply.status;
   reply.headers.forEach((value,key)=>res.setHeader(key,value));res.end(await reply.text());
  }catch{res.statusCode=502;res.setHeader('Content-Type','application/json; charset=utf-8');res.end('{"code":"BOOTH_FETCH_FAILED","message":"BOOTHの商品情報を取得できませんでした。","fieldErrors":[]}');}
 });
}
export default defineConfig(({mode})=>{
 const demo=mode==='demo';
 const env=loadEnv(mode,'.','TAKUKAIRO_');
 const target=env.TAKUKAIRO_BACKEND_ORIGIN??'http://127.0.0.1:8080';
 if(!demo&&!/^http:\/\/(localhost|127\.0\.0\.1):\d+$/.test(target))throw new Error('Local Backend origin required');
 return {
  base:demo?'./':'/',
  plugins:[react(),{name:'demo-page-metadata',transformIndexHtml(html){return demo?html.replace('卓回廊 — ローカルテスト','卓回廊 — ブラウザ体験版').replace('<head>','<head>\n    <meta name="description" content="TRPGの作品と、卓の記憶を並べる個人の回廊。ログイン不要でシナリオ・PC・卓の管理を試せるブラウザ体験版。">'):html}},...(demo?[{
   name:'local-demo-booth',
   configureServer:configureDemoBooth,
   configurePreviewServer:configureDemoBooth
  }]:[])],
  server:{host:'localhost',port:demo?5175:5173,strictPort:true,...(!demo?{proxy:{'/api':{target,changeOrigin:false}}}:{})},
  build:{outDir:demo?'dist-demo':'dist',sourcemap:!demo},
 };
});

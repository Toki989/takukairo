import {defineConfig,loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig(({mode})=>{const env=loadEnv(mode,'.','TAKUKAIRO_');const target=env.TAKUKAIRO_BACKEND_ORIGIN??'http://127.0.0.1:8080';if(!/^http:\/\/(localhost|127\.0\.0\.1):\d+$/.test(target))throw new Error('Local Backend origin required');return{plugins:[react()],server:{host:'localhost',port:5173,strictPort:true,proxy:{'/api':{target,changeOrigin:false}}},build:{sourcemap:true}}});

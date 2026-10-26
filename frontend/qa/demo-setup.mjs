import {startDemoServer} from './serve-demo.mjs';
export default async function () {
  const server = await startDemoServer();
  return async () => { server.closeAllConnections(); await new Promise(resolve => server.close(resolve)); };
}

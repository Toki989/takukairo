import {isDemo} from '../demo/mode';
const key = isDemo ? 'takukairo-demo-pc-density' : 'pc-grid-density';
export function readPcDensity() {
  try { const value = Number(localStorage.getItem(key)); return [2, 3, 4].includes(value) ? value : 2; }
  catch { return 2; }
}
export function savePcDensity(value: string) {
  try { localStorage.setItem(key, value); } catch { /* The current view still works when storage is blocked. */ }
}

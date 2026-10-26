import {defineConfig} from '@playwright/test';
export default defineConfig({
  testDir: 'qa', testMatch: 'demo.spec.ts', timeout: 30000, workers: 1,
  reporter: [['list'], ['html', {outputFolder: '../.runtime/demo-report', open: 'never'}]],
  outputDir: '../.runtime/demo-results',
  use: {baseURL: 'http://127.0.0.1:4175/takukairo/', viewport: {width: 1440, height: 1000}, screenshot: 'only-on-failure', trace: 'retain-on-failure'},
  globalSetup: './qa/demo-setup.mjs',
});

import {defineConfig} from '@playwright/test';
// Intentionally separate from the existing QA which connects to localhost:5173.
export default defineConfig({testDir:'qa',testMatch:'import-inference.spec.ts',timeout:45000,workers:1,retries:0,reporter:[['list'],['json',{outputFile:'../.runtime/import-inference-qa/browser-results.json'}]],use:{baseURL:'http://localhost:5175',viewport:{width:1440,height:900},screenshot:'only-on-failure',trace:'retain-on-failure'},outputDir:'../.runtime/import-inference-qa/browser-output'});

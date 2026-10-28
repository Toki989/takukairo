import {defineConfig} from '@playwright/test';
import demo from './playwright.demo.config';

// WebKit catches Safari engine differences; native Safari still needs a Mac check.
export default defineConfig({
  ...demo,
  projects: [
    {name: 'chromium', use: {browserName: 'chromium'}},
    {name: 'webkit', use: {browserName: 'webkit'}},
  ],
  reporter: [['list'], ['html', {outputFolder: '../.runtime/mac-browser-report', open: 'never'}]],
  outputDir: '../.runtime/mac-browser-results',
});

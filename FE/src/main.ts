import { bootstrapApplication } from '@angular/platform-browser';

import { App } from './app/app';
import { appConfig } from './app/app.config';

// The only allowed console call: if bootstrap fails there is no injector (no LoggerService).
bootstrapApplication(App, appConfig).catch((error: unknown) => console.error(error));

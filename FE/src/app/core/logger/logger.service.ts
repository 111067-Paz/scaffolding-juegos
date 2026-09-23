import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';

const SENSITIVE_KEYS: readonly string[] = ['password', 'token', 'secret'];
const MASK = '***';

/**
 * The ONLY place allowed to write to the console (console.log is forbidden elsewhere).
 * debug/info are silent in production; every argument is sanitized so passwords
 * or tokens never reach the browser console.
 */
@Injectable({ providedIn: 'root' })
export class LoggerService {
  private readonly verbose = !environment.production;

  debug(message: string, ...data: unknown[]): void {
    if (this.verbose) {
      console.debug(message, ...data.map((item) => this.sanitize(item)));
    }
  }

  info(message: string, ...data: unknown[]): void {
    if (this.verbose) {
      console.info(message, ...data.map((item) => this.sanitize(item)));
    }
  }

  warn(message: string, ...data: unknown[]): void {
    console.warn(message, ...data.map((item) => this.sanitize(item)));
  }

  error(message: string, ...data: unknown[]): void {
    console.error(message, ...data.map((item) => this.sanitize(item)));
  }

  /** Deep copy that masks sensitive keys; primitives pass through untouched. */
  sanitize(value: unknown): unknown {
    if (Array.isArray(value)) {
      return value.map((item) => this.sanitize(item));
    }
    if (typeof value !== 'object' || value === null) {
      return value;
    }
    const copy: Record<string, unknown> = {};
    for (const [key, item] of Object.entries(value)) {
      copy[key] = SENSITIVE_KEYS.includes(key.toLowerCase()) ? MASK : this.sanitize(item);
    }
    return copy;
  }
}

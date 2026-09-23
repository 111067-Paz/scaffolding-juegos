/**
 * Production environment (default build). The API is ALWAYS relative ('/api'):
 * Nginx proxies it in Docker and proxy.conf.json in dev. Never hardcode hosts.
 */
export const environment = {
  production: true,
  apiUrl: '/api',
};

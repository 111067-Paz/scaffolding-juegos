/** Mirrors the backend ErrorApi — the single error contract of the API. */
export interface ErrorApi {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

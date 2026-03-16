export interface LoginRequest {
  email: string,
  passwd: string
}

export interface RegisterRequest {
  name: string,
  email: string,
  passwd: string,
  lang: string,
  utcOffset?: number // optional with default 0
}
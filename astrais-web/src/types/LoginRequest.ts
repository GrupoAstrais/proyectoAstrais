export interface LoginRequest {
  email: string,
  passwd: string

}

export interface RegisterRequest {
  name: string,
  email: string,
  passwd: string,
  lang: string, //en-EN
  utcOffset?: number // optional with default 0
}
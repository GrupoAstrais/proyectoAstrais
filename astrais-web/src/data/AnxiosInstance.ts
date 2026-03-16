/* eso para tener en cuenta que es lo que espera el servidor

export interface LoginRequest {
  email: string;
  passwd: string;
}

export interface LoginResponse {
  jwtAccessToken: string;
  jwtRefreshToken: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  passwd: string;
  lang: string;
  utcOffset?: number; // optional with default 0
}

export interface MailVerifierRequest {
  email: string;
  code: string;
}

export interface RegenAccessResponse {
  newAccessToken: string;
}


PARTE QUE HAY QUE DESCOMENTAR

import axios from 'axios';

export default axios.create({
  baseURL: `url servidor`
});


*/

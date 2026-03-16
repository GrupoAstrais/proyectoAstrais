import axios from 'axios';
import type { LoginRequest } from '../types/LoginRequest';

export const API_BASE_URL = 'http://localhost:5684'

let jwtToken: string | null = null

const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' }
})

// Injecta el token si lo tiene
axios.interceptors.request.use(config => {
    if (jwtToken) {
        config.headers.Authorization = `Bearer ${jwtToken}`
    }
    return config
})

export async function performLogin(req: LoginRequest) {
    try {
        const data = await instance.post("/auth/login", req);
        if (data.status == 0) {
            jwtToken = data.data["JwtAccessToken"]
        } else {
            console.error("Error en el log! " + data.data["error"]);
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            // Error de axios
            console.error("Error interno de axios!!")
        } else {
            console.error("Error de la peticion!")
        }
    }
}

/*
import API from '../AxiosInstance.ts'

export function CheckUser (email: string, passwd: string) : {response: number} {
  handleSubmit = async event => {
    event.preventDefault();

    const response = API.post('/auth/login', email, passwd);

    return response;
  }
}
*/


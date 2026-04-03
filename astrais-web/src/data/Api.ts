import axios from 'axios';
import type { LoginRequest, RegisterRequest } from '../types/LoginRequest';

export const API_BASE_URL = 'http://192.168.56.1:5684'

//let jwtToken: string | null = null

const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' }
})

// Injecta el token si lo tiene
// axios.interceptors.request.use(config => {
//     if (jwtToken) {
//         config.headers.Authorization = `Bearer ${jwtToken}`
//     }
//     return config
// })

export async function performLogin(req: LoginRequest) : Promise<void> {
    try {
        const data = await instance.post("/auth/login", req);
        if (data.status >= 200 && data.status < 300) {
            //jwtToken = data.data["JwtAccessToken"]
            console.error("Successful login! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            // Error de axios
            console.error("Error interno de axios!!")
        } else {
            console.error("Error de la peticion!")
        }
        return Promise.reject();
    }
}

export async function createUser(req: RegisterRequest) : Promise<void> {
    try {
        const data = await instance.post("/auth/register", req);
        if (data.status >= 200 && data.status < 300) {
            //jwtToken = data.data["JwtAccessToken"]
            console.error("Successful user profile set up! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            // Error de axios
            console.error("Error interno de axios!!")
        } else {
            console.error("Error de la peticion!")
        }
        return Promise.reject();
    }
}


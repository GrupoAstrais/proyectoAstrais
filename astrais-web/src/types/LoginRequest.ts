export interface LoginRequest {
    email: string,
    password: string,
    rememberMe: boolean
}

export interface CreateUserRequest {
    email: string,
    password: string,
    rememberMe: boolean
}
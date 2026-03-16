export interface LoginRequest {
    email: string,
    password: string
}

export interface CreateUserRequest {
    email: string,
    password: string,
    rememberMe: boolean
}
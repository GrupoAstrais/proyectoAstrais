import axios from 'axios';
import type { AddUserToGroup, CreateGroup, CreateTask, DeleteOauthRequest, EditGroup, EditTask, EditUser, EventosGrupos, GroupInvitacion, GroupInvitacionRespuesta, LoginRequest, MembersResponse, PassOwnershipGroup, RegisterRequest, RevokeGroupInvit, SetEmailLogin, SetMemberRole, SetOauthRequest, UserData, UserGroups, UserGroupsResponse, UserTasksResponse, VerifyRequest } from '../types/LoginRequest';
import type { IGroup, ITarea } from '../types/Interfaces';
import { applyThemeColors } from '../styles/theme';


//export const API_BASE_URL = 'http://192.168.3.148:5684' //url desde las practicas
// export const API_BASE_URL = 'http://192.168.56.1:5684' //url desde casa

export const API_BASE_URL = `http://${window.location.hostname}:5684`; //'http://127.0.0.1:5684' //variable de entorno para la url, con fallback a localhost



// TOKENS

let jwtToken: string | null = null

let jwtRefreshToken: string | null = null

const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' }
})

const refreshInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10_000,
    
    headers: { 'Content-Type': 'application/json' }
});

instance.interceptors.request.use(config => {
    if (localStorage.getItem('jwtToken') && !config.url?.includes('/auth/regenAccess')) {
        config.headers.Authorization = `Bearer ${localStorage.getItem('jwtToken')}` // 
    }
    return config
})

let isRefreshing = false;
let failedQueue: Array<{
    resolve: (token: string) => void;
    reject: (err: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve(token!);
    });
    failedQueue = [];
};

export interface StoreItemResponse {
    id: number;
    name: string;
    desc: string;
    type: string;
    price: number;
    assetRef: string;
    theme: string;
    coleccion: string;
    rarity?: string;
    owned: boolean;
    equipped: boolean;
}

instance.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        const status = error.response?.status;

        if (status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        // 👇 Añade esto: si hay params de OAuth en la URL, no interceptes
        if (window.location.search.includes('jwtAccessToken')) {
            return Promise.reject(error);
        }

        if (isRefreshing) {
            // Ставим в очередь, ждём пока первый обновит токен
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            }).then(token => {
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return instance(originalRequest);
            }).catch(err => Promise.reject(err));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        const refreshToken = localStorage.getItem('jwtRefreshToken');

        try {
            const res = await refreshInstance.post('/auth/regenAccess', {}, {
                headers: {
                    Authorization: `Bearer ${refreshToken}`
                }
            });
            const newToken = res.data.newAccessToken;

            localStorage.setItem('jwtToken', newToken);
            originalRequest.headers.Authorization = `Bearer ${newToken}`;

            processQueue(null, newToken);
            return instance(originalRequest);
        } catch (e) {
            processQueue(e, null);
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('jwtRefreshToken');
            window.location.href = '/login';
            return Promise.reject(e);
        } finally {
            isRefreshing = false;
        }
    }
);


//AUTH DE USUARIO 

export async function performLogin(req: LoginRequest) : Promise<void> {
    try {
        const data = await instance.post("/auth/login", req);
        if (data.status >= 200 && data.status < 300) {
            jwtToken = data.data["jwtAccessToken"];
            jwtRefreshToken = data.data["jwtRefreshToken"];

            if (typeof window !== 'undefined') {
                localStorage.setItem('jwtToken', jwtToken!)
                localStorage.setItem('jwtRefreshToken', jwtRefreshToken!)
            }

            
            console.log("TOKEN: "+jwtToken);
            console.log("REFRESH TOKEN: "+jwtRefreshToken);
            
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
        console.log("VERIFY PAYLOAD:", req);
        const data = await instance.post("/auth/register", req);
        if (data.status >= 200 && data.status < 300) {
            
            console.error("Successful user profile set up! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function confirmRegister(req: VerifyRequest) : Promise<void> {
    try {
        console.log("VERIFY PAYLOAD VERIFY:", req);
        const data = await instance.post("/auth/verify", req);
        if (data.status >= 200 && data.status < 300) {
            
            console.error("Successful confirmation! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

// Inicia el flujo OAuth con Google redirigiendo al usuario
export function loginWithGoogle(): void {
    window.location.href = `http://${window.location.hostname}:5684/auth/google/login`;
}

// Maneja el callback de Google OAuth (tokens en la respuesta)
export async function handleGoogleCallback(_uid: number, hadToRegister: boolean, jwtAccessToken: string, jwtRefreshTokenValue: string): Promise<{ hadToRegister: boolean }> {
    jwtToken = jwtAccessToken;
    jwtRefreshToken = jwtRefreshTokenValue;

    if (typeof window !== 'undefined') {
        localStorage.setItem('jwtToken', jwtToken!)
        localStorage.setItem('jwtRefreshToken', jwtRefreshToken!)
    }

    window.history.replaceState({}, document.title, window.location.pathname)

    console.log("TOKEN: " + jwtToken);
    console.log("REFRESH TOKEN: " + jwtRefreshToken);
    console.error("Successful Google login!");

    return { hadToRegister };
}


// Vincula un proveedor OAuth a la cuenta del usuario autenticado
export async function setOauth(req: SetOauthRequest): Promise<void> {
    try {
        const data = await instance.post("/auth/setOauth", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful OAuth link!");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

// Desvincula un proveedor OAuth de la cuenta del usuario autenticado
export async function deleteOauth(req: DeleteOauthRequest): Promise<void> {
    try {
        const data = await instance.post("/auth/deleteOauth", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful OAuth unlink!");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function editUser(req: EditUser) : Promise<void> {
    try {
        console.log("editUser PAYLOAD:", JSON.stringify(req)); 
        const data = await instance.patch("/auth/editUser", req);
        if (data.status >= 200 && data.status < 300) {
            
            console.error("Successful user perfil edit! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function deleteUser() : Promise<void> {
    try {
        const data = await instance.delete("/auth/deleteUser");
        if (data.status >= 200 && data.status < 300) {
            
            console.error("Successful user delete! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function setEmailLogin(req: SetEmailLogin) : Promise<void> {
    try {
        const data = await instance.patch("/auth/setEmailLogin", req);
        if (data.status >= 200 && data.status < 300) {
            
            console.error("Successful email login edit! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function getUserData() : Promise<UserData> {
    try {
        const response = await instance.get<UserData>("/auth/me");
        console.error("Successful user data retrieval! ");
        console.log(response.data);
        const result = response.data;
        applyThemeColors(result.themeColors);
        return result;
    } catch (err) {
        if (axios.isAxiosError(err)) {
            const status = err.response?.status;
            const responseData = err.response?.data as { errorText?: string; error?: string } | undefined;
            const message =
                responseData?.errorText ??
                responseData?.error ??
                err.message ??
                "Error interno de axios";
            console.error("STATUS:", status);
            console.error("DATA:", err.response?.data);
            throw new Error(`No se pudo obtener /auth/me (${status ?? "sin status"}): ${message}`);
        }

        console.error("Error de la peticion!", err);
        throw new Error("No se pudo obtener /auth/me por un error inesperado.");
    }
}

export async function getStoreItems() : Promise<StoreItemResponse[]> {
    try {
        const response = await instance.get<StoreItemResponse[]>("/store/items");
        return response.data;
    } catch (err) {
        if (axios.isAxiosError(err)) {
            const status = err.response?.status;
            const responseData = err.response?.data as { errorText?: string; error?: string } | undefined;
            const message =
                responseData?.errorText ??
                responseData?.error ??
                err.message ??
                "Error interno de axios";
            console.error("STATUS:", status);
            console.error("DATA:", err.response?.data);
            throw new Error(`No se pudo obtener /store/items (${status ?? "sin status"}): ${message}`);
        }

        console.error(err);
        throw new Error("No se pudo obtener /store/items por un error inesperado.");
    }
}

export async function buyStoreItem(id: number) : Promise<UserData> {
    try {
        const data = await instance.post("/store/buy/" + id);
        if (data.status >= 200 && data.status < 300) {
            /*return Promise.resolve();*/

            console.error("Successful user data retrieval! ");
            const result = data.data as UserData;

            console.log(data);
            return result;
            
        } else {
            console.error("Error en la compra! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function equipStoreItem(id: number) : Promise<void> {
    try {
        const data = await instance.post("/store/equip/" + id);
        if (data.status >= 200 && data.status < 300) {
            return Promise.resolve();
        } else {
            console.error("Error al equipar! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

// GRUPOS

export async function getUserGroup() : Promise<UserGroups[]> {
    try {

        const data = await instance.get("/group/userGroups");
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user group retrieval! ");

            const res = data.data as UserGroupsResponse;

            return res.groupList;
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

export async function createGroup(req: CreateGroup) : Promise<number> {
    try {

        const data = await instance.post("/groups/createGroup", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful group creation! ");
            return data.data["groupId"] as number;
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

export async function deleteGroup(gid: number, role: number) : Promise<void> {
    if(role == 2) {
        try {
            const data = await instance.delete("/groups/deleteGroup", {
                data: { gid: gid }
            });

            if (data.status >= 200 && data.status < 300) {
                console.error("Successful group deletion! ");
                return Promise.resolve();
            } else {
                console.error("Error en el log! " + data.data["error"]);
                return Promise.reject();
            }
        } catch (err) {
            if (axios.isAxiosError(err)) {
                console.error("STATUS:", err.response?.status);
                console.error("DATA:", err.response?.data);
            } else {
                console.error(err);
            }
            return Promise.reject();
        }
    } else {
        console.error("User is not owner of the group");
        return Promise.reject();
    }
}

export async function editGroup(req: EditGroup) : Promise<void> {
    try {

        const data = await instance.patch("/groups/editGroup", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful group edit! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
            if (axios.isAxiosError(err)) {
                console.error("STATUS:", err.response?.status);
                console.error("DATA:", err.response?.data);
            } else {
                console.error(err);
            }
        return Promise.reject();
    }
}


export async function addUserToGroup(req: AddUserToGroup) : Promise<void> {
    try {
        const data = await instance.post("/groups/addUser", JSON.stringify({
            gid: req.gid,
            userId: req.userId
        }));
        if (data.status >= 200 && data.status < 300) {

            console.error("Successful user add to group! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function userLeaveGroup(uid: number) : Promise<void> {
    try {
        const data = await instance.post("/groups/leave",  { gid: uid });
        if (data.status >= 200 && data.status < 300) {

            console.error("Successfuly left group! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function removeUserFromGroup(req: AddUserToGroup) : Promise<void> {
    try {
        const data = await instance.post("/groups/removeUser", req);
        if (data.status >= 200 && data.status < 300) {

            console.error("Successful user remove from group! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function membersGroups(gid: number) : Promise<MembersResponse[]> {
    try {
        const data = await instance.get("/groups/"+gid+"/members");
        if (data.status >= 200 && data.status < 300) {

            const res = data.data["members"] as MembersResponse[];
            console.error("Successful members group array! "+JSON.stringify(res));

            return res;
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}


export async function setMemberRole(req: SetMemberRole) : Promise<void> {
    try {
        const data = await instance.patch("/groups/setMemberRole", req);

        if (data.status >= 200 && data.status < 300) {

            console.error("Successful user role assign! ");
            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function eventosGroup(gid: number) : Promise<EventosGrupos[]> {
    try {
        const data = await instance.get("/groups/"+gid+"/audit");
        if (data.status >= 200 && data.status < 300) {

            const res = data.data as EventosGrupos[];
            console.error("Successful eventos array! ");

            return res;
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function groupInviteLink(req: number) : Promise<string> {
    try {
        const data = await instance.post("/groups/inviteUrl", {
                gid: req
            });

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful inivite link! ");

            return data.data["inviteUrl"] as string;
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function groupJoinByLink(req: string) : Promise<void> {
    try {
        const data = await instance.post("/groups/joinByUrl", {
                inviteUrl: req 
            });

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful join link! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function groupJoinByCode(req: string) : Promise<void> {
    try {
        const data = await instance.post("/groups/joinByCode", {
                code: req 
            });

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful join code! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function groupInvitacion(req: GroupInvitacion) : Promise<GroupInvitacionRespuesta> {
    try {
        const data = await instance.post("/groups/invites", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful crear invitacion ");
            const payload = data.data as unknown;

            if (payload && typeof payload === "object") {
                const obj = payload as Partial<GroupInvitacionRespuesta>;
                return {
                    code: typeof obj.code === "string" ? obj.code : "",
                    inviteUrl: typeof obj.inviteUrl === "string" ? obj.inviteUrl : "",
                    expiresAt: typeof obj.expiresAt === "string" ? obj.expiresAt : null,
                    maxUses: typeof obj.maxUses === "number" ? obj.maxUses : 10,
                    usesCount: typeof obj.usesCount === "number" ? obj.usesCount : 0,
                    revokedAt: typeof obj.revokedAt === "string" ? obj.revokedAt : null
                };
            }

            return {
                code: "",
                inviteUrl: "",
                expiresAt: null,
                maxUses: 10,
                usesCount: 0,
                revokedAt: null
            };
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function groupInvitacionLista(gid: number) : Promise<GroupInvitacionRespuesta[]> {
    try {
        const data = await instance.get("/groups/"+gid+"/invites");
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful lista  invitaciones! ");
            const payload = data.data as unknown;

            const mapInvite = (item: unknown): GroupInvitacionRespuesta => {
                const invite = (item && typeof item === "object" ? item : {}) as Partial<GroupInvitacionRespuesta>;
                return {
                    code: typeof invite.code === "string" ? invite.code : "",
                    inviteUrl: typeof invite.inviteUrl === "string" ? invite.inviteUrl : "",
                    expiresAt: typeof invite.expiresAt === "string" ? invite.expiresAt : null,
                    maxUses: typeof invite.maxUses === "number" ? invite.maxUses : 10,
                    usesCount: typeof invite.usesCount === "number" ? invite.usesCount : 0,
                    revokedAt: typeof invite.revokedAt === "string" ? invite.revokedAt : null
                };
            };

            if (Array.isArray(payload)) {
                return payload.map(mapInvite);
            }

            if (payload && typeof payload === "object") {
                const obj = payload as Record<string, unknown>;
                const candidates = [obj.invites, obj.inviteList, obj.items];
                const firstArray = candidates.find((candidate) => Array.isArray(candidate)) as unknown[] | undefined;

                if (firstArray) {
                    return firstArray.map(mapInvite);
                }
            }

            return [];
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function revokeGroupInvit(req: RevokeGroupInvit) : Promise<void> {
    try {
        const data = await instance.post("/groups/invites/revoke", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful lista  invitaciones! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

//sinceramente no entendi que es lo que hace, revisar
export async function redirectInvite(req: RevokeGroupInvit) : Promise<void> {
    try {
        const data = await instance.post("/groups/redirectInvite", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful lista  invitaciones! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function passOwnershipGroup(req: PassOwnershipGroup) : Promise<void> {
    try {
        const data = await instance.patch("/groups/passOwnership", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful ownership pasado! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function createTask(req: CreateTask) : Promise<number> {
    try {
        console.error(req);

        const data = await instance.post("/tasks", req);
        if (data.status >= 200 && data.status < 300) {
            console.log("createTask REQUEST:", JSON.stringify(req))
            console.error("Successful task created! ");
            return data.data["id"] as number;
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function getTasksFromGroup(gid: number) : Promise<ITarea[]> {
    try {

        const data = await instance.post("/tasks/"+gid);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user tasks page retrieval! ");

            const res = data.data as UserTasksResponse;
            
            console.error(data.data);
            return res.taskList;
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function editTask(tid: number, req: EditTask) : Promise<void> {
    try {
        console.log(tid);
        const data = await instance.patch("/tasks/"+tid+"/edit", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user task edit! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function completeTask(tid: number) : Promise<void> {
    try {
        const data = await instance.patch("/tasks/"+tid+"/complete");

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user task complete! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function uncompleteTask(tid: number) : Promise<void> {
    try {
        const data = await instance.patch("/tasks/"+tid+"/uncomplete");

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user task uncomplete! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}

export async function deleteTask(tid: number) : Promise<void> {
    try {
        const data = await instance.delete("/tasks/"+tid+"/delete");

        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user task delete! ");

            return Promise.resolve();
        } else {
            console.error("Error en el log! " + data.data["error"]);
            return Promise.reject();
        }
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error("STATUS:", err.response?.status);
            console.error("DATA:", err.response?.data);
        } else {
            console.error(err);
        }
        return Promise.reject();
    }
}


export type TTaskTimeFilter = "Today" | "Tomorrow" | "All";
export type TTaskPriority = 0 | 1 | 2;
export type TTaskFormType = "HABITO" | "UNICO" | "OBJETIVO";
export type THabitFrequency = "daily" | "weekly" | "monthly" | "hourly" | "yearly";

export interface ITaskFormSubtask {
    id: number | string;
    name: string;
}

export interface ITaskFormData {
    name: string;
    description: string;
    difficulty: TTaskPriority;
    taskType: TTaskFormType;
    habitFrequency: THabitFrequency | null;
    taskDate: string;
    idObjetivo?: number
}

interface ITaskCompletedFilters {
    completed: boolean;
    pending: boolean;
}

const normalizeDate = (date: Date): Date => {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

const DEFAULT_TASK_PRIORITY: TTaskPriority = 1;

export const getTaskXpReward = (priority: number): number => {
    switch (priority) {
        case 0:
            return 20;
        case 2:
            return 50;
        default:
            return 35;
    }
}

export const normalizeTaskPriority = (priority: unknown): TTaskPriority => {
    const parsedPriority = Number(priority);

    if (parsedPriority === 0 || parsedPriority === 1 || parsedPriority === 2) {
        return parsedPriority;
    }

    return DEFAULT_TASK_PRIORITY;
}

export const getTaskPriorityLabel = (priority: number): string => {
    switch (normalizeTaskPriority(priority)) {
        case 0:
            return "Easy";
        case 2:
            return "Hard";
        default:
            return "Medium";
    }
}

const mapUiFrequencyToServer = (frequency: THabitFrequency | null): THabitFrequency => {
    switch (frequency) {
        case "weekly":
            return "WEEKLY" as THabitFrequency;
        case "monthly":
            return "MONTHLY" as THabitFrequency;
        case "hourly":
            return "HOURLY" as THabitFrequency;
        case "yearly":
            return "YEARLY" as THabitFrequency;
        default:
            return "DAILY" as THabitFrequency;
    }
}

export const mapServerFrequencyToUi = (frequency?: string): THabitFrequency | null => {
    switch (frequency) {
        case "daily":
            return "daily";
        case "weekly":
            return "weekly";
        case "monthly":
            return "monthly";
        case "hourly":
            return "hourly";
        case "yearly":
            return "yearly";
        case "WEEKLY":
            return "weekly";
        case "MONTHLY":
            return "monthly";
        case "DAILY":
            return "daily";
        case "HOURLY":
            return "hourly";
        case "YEARLY":
            return "yearly";
        default:
            return null;
    }
}

export const formatTaskDate = (date: Date): string => {
    const normalizedDate = normalizeDate(date);
    const year = normalizedDate.getFullYear();
    const month = `${normalizedDate.getMonth() + 1}`.padStart(2, "0");
    const day = `${normalizedDate.getDate()}`.padStart(2, "0");

    return `${year}-${month}-${day}`;
}

export const normalizeTaskDateString = (date?: string): string => {
    if (!date) {
        return formatTaskDate(new Date());
    }

    const parsedDate = new Date(date);

    if (!Number.isNaN(parsedDate.getTime())) {
        return formatTaskDate(parsedDate);
    }

    const [year, month, day] = date.slice(0, 10).split("-").map(Number);

    if (!year || !month || !day) {
        return formatTaskDate(new Date());
    }

    return formatTaskDate(new Date(year, month - 1, day));
}

const parseTaskDate = (date?: string): Date => {
    return parseTaskDateString(normalizeTaskDateString(date));
}

const parseTaskDateString = (date: string): Date => {
    const [year, month, day] = date.split("-").map(Number);
    return new Date(year, (month || 1) - 1, day || 1);
}

const formatTaskDateAsIso = (date?: string): string => {
    return new Date(normalizeTaskDateString(date)).toISOString();
}

const isSameDate = (firstDate: Date, secondDate: Date): boolean => {
    return formatTaskDate(firstDate) === formatTaskDate(secondDate);
}

const getDiffInDays = (firstDate: Date, secondDate: Date): number => {
    const millisecondsPerDay = 1000 * 60 * 60 * 24;
    const normalizedFirstDate = normalizeDate(firstDate).getTime();
    const normalizedSecondDate = normalizeDate(secondDate).getTime();

    return Math.floor((normalizedFirstDate - normalizedSecondDate) / millisecondsPerDay);
}

export const getTaskDate = (task: ITarea): string => {
    if (Array.isArray(task.extraUnico) && typeof task.extraUnico[0] === "string") {
        return normalizeTaskDateString(task.extraUnico[0]);
    }

    if (!Array.isArray(task.extraUnico) && typeof task.extraUnico?.fechaLimite === "string") {
        return normalizeTaskDateString(task.extraUnico.fechaLimite);
    }

    // Берём реальную дату создания хабита как базу для подсчёта дней
    if (task.tipo === "HABITO" && task.fecha_creacion) {
        return normalizeTaskDateString(task.fecha_creacion);
    }

    return formatTaskDate(new Date());
}

const getTaskStartDate = (task: ITarea): string => {
    if (task.fecha_creacion) {
        return normalizeTaskDateString(task.fecha_creacion);
    }

    return formatTaskDate(new Date());
}

export const getTaskHabitFrequency = (task: ITarea): THabitFrequency | null => {
    if (!task.extraHabito) {
        return task.tipo === "HABITO" ? "daily" : null;
    }

    // С сервера приходит объект, не массив
    if (!Array.isArray(task.extraHabito)) {
        return mapServerFrequencyToUi((task.extraHabito as any).frequency);
    }

    // Локально созданная задача — массив [numeroFrecuencia, frequency]
    if (typeof task.extraHabito[1] === "string") {
        const localFrequency = task.extraHabito[1].toLowerCase();
        if (localFrequency === "daily" || localFrequency === "weekly" || localFrequency === "monthly" || localFrequency === "hourly" || localFrequency === "yearly") {
            return localFrequency as THabitFrequency;
        }
        return mapServerFrequencyToUi(task.extraHabito[1]);
    }

    return "daily";
}

export const isTaskCompleted = (task: ITarea): boolean => {
    return task.estado === "COMPLETE";
}

export const isTaskVisibleInDefaultList = (task: ITarea, referenceDate: Date = new Date()): boolean => {
    const today = normalizeDate(referenceDate);

    if (task.tipo === "UNICO" && parseTaskDate(getTaskDate(task)) < today) {
        return false;
    }

    if (!isTaskCompleted(task)) {
        return true;
    }

    const completedDate = parseTaskDate(task.fecha_actualizado ?? getTaskDate(task));
    return normalizeDate(completedDate) >= today;
}

export const isTaskSubtask = (task: ITarea): boolean => {
    return typeof task.idObjetivo === "number" && Number.isFinite(task.idObjetivo);
}

export const getTaskSubtasks = (tasks: ITarea[], parentTaskId: number): ITarea[] => {
    return tasks.filter((task) => task.idObjetivo === parentTaskId);
}

export const isComposedTask = (task: ITarea, tasks: ITarea[]): boolean => {
    return task.tipo === "OBJETIVO" || getTaskSubtasks(tasks, task.id).length > 0;
}


export const getDailyTasks = (tasks: ITarea[]): ITarea[] => {
    return tasks.filter((task) => task.tipo !== "HABITO");
}

export const getHabitTasks = (tasks: ITarea[]): ITarea[] => {
    return tasks.filter((task) => task.tipo === "HABITO");
}

export const buildTaskFormData = (task: ITarea): ITaskFormData => {
    return {
        name: task.titulo,
        description: task.descripcion,
        difficulty: normalizeTaskPriority(task.prioridad),
        taskType: task.tipo === "HABITO" ? "HABITO" : task.tipo == "UNICO" ? "UNICO" : "OBJETIVO" ,
        idObjetivo: typeof task.idObjetivo === "number" ? task.idObjetivo : undefined,
        habitFrequency: getTaskHabitFrequency(task),
        taskDate: getTaskDate(task)
    };
}

export const buildCreateTaskRequest = (gid: number, data: ITaskFormData, parentTaskId?: number): CreateTask => {
    const resolvedParentId = parentTaskId ?? data.idObjetivo; // ← добавь это

    const taskType: 'UNICO' | 'HABITO' | 'OBJETIVO' =
        data.taskType === "HABITO"
                ? 'HABITO'
                : data.taskType === "UNICO" ? 'UNICO' : 'OBJETIVO';

    const request: CreateTask = {
        gid,
        titulo: data.name.trim(),
        descripcion: data.description.trim(),
        tipo: taskType,
        prioridad: normalizeTaskPriority(data.difficulty)
    };

    if (taskType === "HABITO") {
        request.extraHabito = {
            numeroFrecuencia: data.habitFrequency === 'daily' ? 1 : data.habitFrequency === 'monthly' ? 30 : 7,
            frequency: mapUiFrequencyToServer(data.habitFrequency) as 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
        };
    } else if (taskType === "UNICO") {
        request.extraUnico = {
            fechaLimite: formatTaskDateAsIso(data.taskDate)
        };
    }

    if (typeof resolvedParentId === "number") {
        request.idObjetivo = resolvedParentId; // ← теперь отправляется
    }

    return request;
}

export const buildEditTaskRequest = (data: ITaskFormData): EditTask => {
    return {
        titulo: data.name.trim(),
        descripcion: data.description.trim(),
        prioridad: `${normalizeTaskPriority(data.difficulty)}`
    };
}


export const createLocalTask = (
    data: ITaskFormData,
    options: {
        gid: number;
        id: number;
        idObjetivo?: number;
        estado?: ITarea["estado"];
        tipo?: ITarea["tipo"];
    }
): ITarea => {
    const priority = normalizeTaskPriority(data.difficulty);
    const taskType =
        options.tipo ??
        (data.taskType === "HABITO"
            ? "HABITO" : data.taskType === "OBJETIVO"
                    ? "OBJETIVO" : "UNICO");

    return {
        id: options.id,
        gid: options.gid,
        titulo: data.name.trim(),
        descripcion: data.description.trim(),
        tipo: taskType,
        prioridad: priority,
        extraUnico: taskType === "UNICO" ? { fechaLimite: formatTaskDateAsIso(data.taskDate) } : undefined,
        extraHabito: taskType === "HABITO"
            ? [
                data.habitFrequency === "monthly"
                    ? 30
                    : data.habitFrequency === "weekly"
                        ? 7
                        : 1,
                mapUiFrequencyToServer(data.habitFrequency) as "HOURLY" | "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY"
            ]
            : undefined,
        idObjetivo: options.idObjetivo,
        estado: options.estado ?? "ACTIVE",
        recompensaXp: getTaskXpReward(priority),
        recompensaLudion: 0,
        fecha_creacion: new Date().toISOString()
    };
}

export const createNewGroup = (data: any, gid: number) : IGroup => {
    return {
        gid: gid,
        name: data.name,
        description: data.description,
        photoUrl: data.photo ? URL.createObjectURL(data.photo) : null,
        members: [],
        tasks: [],
        role: 2
    }
}

export const toggleTaskCompleted = (tasks: ITarea[], taskId: string): ITarea[] => {
    const taskIdAsNumber = Number(taskId);
    const targetTask = tasks.find((task) => task.id === taskIdAsNumber);
    const updatedAt = new Date().toISOString();

    if (!targetTask) {
        return tasks;
    }

    const taskSubtasks = getTaskSubtasks(tasks, taskIdAsNumber);
    const shouldComplete =
        taskSubtasks.length > 0
            ? !taskSubtasks.every((subtask) => isTaskCompleted(subtask))
            : !isTaskCompleted(targetTask);
    const nextState: ITarea["estado"] = shouldComplete ? "COMPLETE" : "ACTIVE";

    return tasks.map((task) => {
        if (task.id === taskIdAsNumber || task.idObjetivo === taskIdAsNumber) {
            return {
                ...task,
                estado: nextState,
                fecha_actualizado: updatedAt,
                fecha_completado: nextState === "COMPLETE" ? (task.fecha_completado ?? updatedAt) : task.fecha_completado
            };
        }

        return task;
    });
}

export const toggleSubtaskCompleted = (tasks: ITarea[], taskId: string, subtaskId: string): ITarea[] => {
    const taskIdAsNumber = Number(taskId);
    const subtaskIdAsNumber = Number(subtaskId);
    const updatedAt = new Date().toISOString();

    const toggledTasks: ITarea[] = tasks.map((task) => {
        if (task.id !== subtaskIdAsNumber) {
            return task;
        }

        const nextSubtaskState: ITarea["estado"] = isTaskCompleted(task) ? "ACTIVE" : "COMPLETE";

        return {
            ...task,
            estado: nextSubtaskState,
            fecha_actualizado: updatedAt,
            fecha_completado: nextSubtaskState === "COMPLETE" ? (task.fecha_completado ?? updatedAt) : task.fecha_completado
        };
    });

    const subtasks = getTaskSubtasks(toggledTasks, taskIdAsNumber);

    return toggledTasks.map((task) => {
        if (task.id !== taskIdAsNumber) {
            return task;
        }

        const nextParentState: ITarea["estado"] =
            subtasks.length > 0 && subtasks.every((subtask) => isTaskCompleted(subtask)) ? "COMPLETE" : "ACTIVE";

        return {
            ...task,
            estado: nextParentState,
            fecha_actualizado: task.estado === nextParentState ? task.fecha_actualizado : updatedAt,
            fecha_completado:
                nextParentState === "COMPLETE"
                    ? (task.fecha_completado ?? updatedAt)
                    : task.fecha_completado
        };
    });
}

export const removeTaskWithSubtasks = (tasks: ITarea[], taskId: number): ITarea[] => {
    return tasks.filter((task) => task.id !== taskId && task.idObjetivo !== taskId);
}

export const replaceTaskWithChildren = (tasks: ITarea[], updatedTasks: ITarea[]): ITarea[] => {
    const updatedTaskIds = new Set(updatedTasks.map((task) => task.id));
    const parentIds = new Set(updatedTasks.filter((task) => typeof task.idObjetivo !== "number").map((task) => task.id));

    return [
        ...tasks.filter((task) => !updatedTaskIds.has(task.id) && !parentIds.has(task.idObjetivo ?? -1)),
        ...updatedTasks
    ];
}

export const filterTasksByCompleted = (tasks: ITarea[], filters: ITaskCompletedFilters): ITarea[] => {
    if ((!filters.completed && !filters.pending) || (filters.completed && filters.pending)) {
        return tasks;
    }

    return tasks.filter((task) => {
        if (filters.completed) {
            return isTaskCompleted(task);
        }

        return !isTaskCompleted(task);
    });
}

export const sortTasksByCompleted = (tasks: ITarea[]): ITarea[] => {
    return [...tasks].sort((firstTask, secondTask) => Number(isTaskCompleted(firstTask)) - Number(isTaskCompleted(secondTask)));
}

export const isTaskAvailableOnDate = (task: ITarea, date: Date): boolean => {
    const selectedDate = normalizeDate(date);
    const taskBaseDate = parseTaskDate(getTaskDate(task));

    if (task.tipo === "UNICO") {
        const taskStartDate = parseTaskDate(getTaskStartDate(task));
        return selectedDate >= taskStartDate && selectedDate <= taskBaseDate;
    }

    if (selectedDate < taskBaseDate) {
        return false;
    }

    if (task.tipo !== "HABITO") {
        return isSameDate(selectedDate, taskBaseDate);
    }

    const habitFrequency = getTaskHabitFrequency(task);

    if (habitFrequency === "weekly") {
        return getDiffInDays(selectedDate, taskBaseDate) % 7 === 0;
    }

    if (habitFrequency === "monthly") {
        return taskBaseDate.getDate() === selectedDate.getDate();
    }

    return true;
}

export const filterTasksByTime = (
    tasks: ITarea[],
    activeFilter: TTaskTimeFilter,
    selectedCalendarDate?: Date | null
): ITarea[] => {
    if (selectedCalendarDate) {
        return tasks.filter((task) => isTaskAvailableOnDate(task, selectedCalendarDate));
    }

    const today = normalizeDate(new Date());
    const tomorrow = normalizeDate(new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1));
    const visibleTasks = tasks.filter((task) => isTaskVisibleInDefaultList(task, today));

    if (activeFilter === "Tomorrow") {
        return visibleTasks.filter((task) => isTaskAvailableOnDate(task, tomorrow));
    }

    if (activeFilter === "All") {
        return visibleTasks;
    }

    return visibleTasks.filter((task) => isTaskAvailableOnDate(task, today));
}

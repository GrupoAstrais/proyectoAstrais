import axios, { type InternalAxiosRequestConfig } from 'axios';
import type { AddUserToGroup, CreateGroup, EditGroup, LoginRequest, RegisterRequest, UserData, UserGroups, UserGroupsResponse, VerifyRequest } from '../types/LoginRequest';
import type { IGroup, ITarea } from '../types/Interfaces';

export const API_BASE_URL = 'http://192.168.3.148:5684'

let jwtToken: string | null = null

let jwtRefreshToken: string | null = null

type EditGroupPayload = EditGroup & CreateGroup;

type RefreshAccessResponse = {
    newAccessToken: string;
}

type RetryableRequestConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
}

const getStoredAccessToken = (): string | null => {
    if (typeof window === 'undefined') {
        return jwtToken;
    }

    return localStorage.getItem('jwtToken');
}

const getStoredRefreshToken = (): string | null => {
    if (typeof window === 'undefined') {
        return jwtRefreshToken;
    }

    return localStorage.getItem('jwtRefreshToken');
}

const setStoredTokens = (accessToken: string | null, refreshToken: string | null): void => {
    jwtToken = accessToken;
    jwtRefreshToken = refreshToken;

    if (typeof window === 'undefined') {
        return;
    }

    if (accessToken) {
        localStorage.setItem('jwtToken', accessToken);
    } else {
        localStorage.removeItem('jwtToken');
    }

    if (refreshToken) {
        localStorage.setItem('jwtRefreshToken', refreshToken);
    } else {
        localStorage.removeItem('jwtRefreshToken');
    }
}

const parseJwtPayload = (token: string): { exp?: number } | null => {
    try {
        const [, payload] = token.split('.');

        if (!payload) {
            return null;
        }

        const normalizedPayload = payload
            .replace(/-/g, '+')
            .replace(/_/g, '/')
            .padEnd(Math.ceil(payload.length / 4) * 4, '=');

        return JSON.parse(atob(normalizedPayload)) as { exp?: number };
    } catch {
        return null;
    }
}

export const isTokenExpired = (token: string | null): boolean => {
    if (!token) {
        return true;
    }

    const payload = parseJwtPayload(token);

    if (!payload?.exp) {
        return true;
    }

    return Date.now() >= payload.exp * 1000;
}

let refreshTokenRequest: Promise<string | null> | null = null;

export async function regenerateAccessToken(): Promise<string | null> {
    if (refreshTokenRequest) {
        return refreshTokenRequest;
    }

    refreshTokenRequest = (async () => {
        const refreshToken = getStoredRefreshToken();

        if (!refreshToken) {
            return null;
        }

        try {
            const data = await axios.post(
                `${API_BASE_URL}/auth/regenAccess`,
                {},
                {
                    timeout: 10_000,
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${refreshToken}`
                    }
                }
            );

            if (data.status < 200 || data.status >= 300) {
                return null;
            }

            const newAccessToken = (data.data as RefreshAccessResponse).newAccessToken;

            if (!newAccessToken) {
                return null;
            }

            setStoredTokens(newAccessToken, refreshToken);
            return newAccessToken;
        } catch (err) {
            if (axios.isAxiosError(err) && err.response?.status === 401) {
                setStoredTokens(null, null);
            }

            if (axios.isAxiosError(err)) {
                console.error('Error regenerando el access token');
            } else {
                console.error('Error inesperado regenerando el access token');
            }

            return null;
        } finally {
            refreshTokenRequest = null;
        }
    })();

    return refreshTokenRequest;
}

const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' }
})

instance.interceptors.request.use(async (config) => {
    const isRefreshRequest = config.url?.includes('/auth/regenAccess') === true;

    if (isRefreshRequest) {
        const refreshToken = getStoredRefreshToken();

        if (refreshToken) {
            config.headers.Authorization = `Bearer ${refreshToken}`;
        }

        return config;
    }

    let token = getStoredAccessToken();

    if (!token || isTokenExpired(token)) {
        token = await regenerateAccessToken();
    }

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
})

instance.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (!axios.isAxiosError(error)) {
            return Promise.reject(error);
        }

        const originalRequest = error.config as RetryableRequestConfig | undefined;
        const refreshToken = getStoredRefreshToken();

        if (
            error.response?.status !== 401 ||
            !originalRequest ||
            originalRequest._retry ||
            originalRequest.url?.includes('/auth/regenAccess') ||
            !refreshToken
        ) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        const newAccessToken = await regenerateAccessToken();

        if (!newAccessToken) {
            return Promise.reject(error);
        }

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return instance(originalRequest);
    }
)

export async function performLogin(req: LoginRequest) : Promise<void> {
    try {
        const data = await instance.post("/auth/login", req);
        if (data.status >= 200 && data.status < 300) {
            jwtToken = data.data["jwtAccessToken"];
            jwtRefreshToken = data.data["jwtRefreshToken"];

            setStoredTokens(jwtToken, jwtRefreshToken);
            
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

export async function confirmRegister(req: VerifyRequest) : Promise<void> {
    try {
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
            // Error de axios
            console.error("Error interno de axios!!")
        } else {
            console.error("Error de la peticion!")
        }
        return Promise.reject();
    }
}

export async function getUserData() : Promise<UserData> {
    try {

        const data = await instance.get("/auth/me");
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful user data retrieval! ");
            const result = data.data as UserData;


            return result;
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

export async function createGroup(req: CreateGroup) : Promise<void> {
    try {

        const data = await instance.post("/groups/createGroup", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful group creation! ");
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

export async function editGroup(req: EditGroupPayload) : Promise<void> {
    try {

        const data = await instance.post("/groups/editGroup", req);
        if (data.status >= 200 && data.status < 300) {
            console.error("Successful group edit! ");
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

export async function addUserToGroup(req: AddUserToGroup) : Promise<void> {
    try {
        const data = await instance.post("/groups/addUser", req);
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

export type TTaskTimeFilter = "Today" | "Tomorrow" | "All";

interface ITaskCompletedFilters {
    completed: boolean;
    pending: boolean;
}

const normalizeDate = (date: Date): Date => {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

export const formatTaskDate = (date: Date): string => {
    const normalizedDate = normalizeDate(date);
    const year = normalizedDate.getFullYear();
    const month = `${normalizedDate.getMonth() + 1}`.padStart(2, "0");
    const day = `${normalizedDate.getDate()}`.padStart(2, "0");

    return `${year}-${month}-${day}`;
}

const parseTaskDate = (date?: string): Date => {
    if (!date) return normalizeDate(new Date());

    const [year, month, day] = date.split("-").map(Number);

    return new Date(year, (month || 1) - 1, day || 1);
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

export const createLocalTask = (data: any): ITarea => {
    return {
        id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        title: data.name,
        dificultad: data.difficulty,
        recompensa: data.difficulty === "EASY" ? 20 : data.difficulty === "MEDIUM" ? 35 : 50,
        taskType: data.taskType,
        tags: data.tags || [],
        isComposed: data.isComposed,
        subtasks: data.subtasks || [],
        habitFrequency: data.habitFrequency,
        completed: false,
        taskDate: data.taskDate || formatTaskDate(new Date())
    };
}

export const createNewGroup = (data: any) : IGroup => {
    return {
        id: Date.now(),
        name: data.name,
        description: data.description,
        photoUrl: data.photo ? URL.createObjectURL(data.photo) : null,
        members: [],
        tasks: [],
    }
}

export const toggleTaskCompleted = (tasks: ITarea[], taskId: string): ITarea[] => {
    return tasks.map((task) =>
        task.id === taskId
            ? {
                ...task,
                completed: !task.completed,
                subtasks: task.subtasks.map((subtask) => ({
                    ...subtask,
                    completed: !task.completed
                }))
            }
            : task
    );
}

export const toggleSubtaskCompleted = (tasks: ITarea[], taskId: string, subtaskId: string): ITarea[] => {
    return tasks.map((task) => {
        if (task.id !== taskId) {
            return task;
        }

        const subtasks = task.subtasks.map((subtask) =>
            subtask.id === subtaskId
                ? { ...subtask, completed: !subtask.completed }
                : subtask
        );

        return {
            ...task,
            subtasks,
            completed: subtasks.length > 0 && subtasks.every((subtask) => subtask.completed)
        };
    });
}

export const filterTasksByCompleted = (tasks: ITarea[], filters: ITaskCompletedFilters): ITarea[] => {
    if ((!filters.completed && !filters.pending) || (filters.completed && filters.pending)) {
        return tasks;
    }

    return tasks.filter((task) => {
        if (filters.completed) {
            return task.completed === true;
        }

        return task.completed !== true;
    });
}

export const sortTasksByCompleted = (tasks: ITarea[]): ITarea[] => {
    return [...tasks].sort((firstTask, secondTask) => Number(firstTask.completed === true) - Number(secondTask.completed === true));
}

export const isTaskAvailableOnDate = (task: ITarea, date: Date): boolean => {
    const selectedDate = normalizeDate(date);
    const taskBaseDate = parseTaskDate(task.taskDate);

    if (selectedDate < taskBaseDate) {
        return false;
    }

    if (task.taskType === "diary") {
        return isSameDate(selectedDate, taskBaseDate);
    }

    if (task.habitFrequency === "weekly") {
        return getDiffInDays(selectedDate, taskBaseDate) % 7 === 0;
    }

    if (task.habitFrequency === "monthly") {
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

    if (activeFilter === "Tomorrow") {
        return tasks.filter((task) => isTaskAvailableOnDate(task, tomorrow));
    }

    if (activeFilter === "All") {
        return tasks.filter((task) => {
            return isTaskAvailableOnDate(task, today) || isTaskAvailableOnDate(task, tomorrow);
        });
    }

    return tasks.filter((task) => isTaskAvailableOnDate(task, today));
}

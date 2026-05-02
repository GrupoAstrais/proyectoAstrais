import axios from 'axios';
import type { AddUserToGroup, CreateGroup, CreateTask, EditGroup, EditTask, LoginRequest, RegisterRequest, UserData, UserGroups, UserGroupsResponse, UserTasksResponse, VerifyRequest } from '../types/LoginRequest';
import type { IGroup, ITarea } from '../types/Interfaces';


//export const API_BASE_URL = 'http://192.168.3.148:5684' //url desde las practicas
export const API_BASE_URL = 'http://192.168.56.1:5684' //url desde casa


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

instance.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        const status = error.response?.status;

        if (status !== 401 || originalRequest._retry) {
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
        const data = await instance.post("/groups/addUser", req);
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
        case "WEEKLY":
            return "weekly";
        case "MONTHLY":
            return "monthly";
        case "DAILY":
        case "HOURLY":
        case "YEARLY":
            return "daily";
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
        idObjetivo: task.idObjetivo,
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

export const shouldRecreateTaskOnEdit = (task: ITarea, data: ITaskFormData): boolean => {
    const nextTaskType =
        data.taskType === "HABITO"
            ? "HABITO"
            : data.taskType === "OBJETIVO"
                ? "OBJETIVO"
                : "UNICO";

    if (task.tipo !== nextTaskType) {
        return true;
    }

    if (nextTaskType === "HABITO") {
        return getTaskHabitFrequency(task) !== data.habitFrequency;
    }

    if (getTaskDate(task) !== normalizeTaskDateString(data.taskDate)) {
        return true;
    }

    if (data.idObjetivo || task.tipo === "OBJETIVO") {
        return true;
    }

    return false;
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
        extraHabito: taskType === "HABITO" ? [data.habitFrequency === 'daily' ? 1 : data.habitFrequency === 'monthly' ? 30 : 7, data.habitFrequency as "HOURLY" | "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY" | undefined] : undefined,
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
                fecha_actualizado: updatedAt
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
            fecha_actualizado: updatedAt
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
            fecha_actualizado: task.estado === nextParentState ? task.fecha_actualizado : updatedAt
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

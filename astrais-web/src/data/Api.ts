import axios from 'axios';
import type { LoginRequest, RegisterRequest, VerifyRequest } from '../types/LoginRequest';
import type { IGroup, ITarea } from '../types/Interfaces';

export const API_BASE_URL = 'http://192.168.3.148:5684'

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

export async function confirmRegister(req: VerifyRequest) : Promise<void> {
    try {
        const data = await instance.post("/auth/verify", req);
        if (data.status >= 200 && data.status < 300) {
            //jwtToken = data.data["JwtAccessToken"]
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
        photoUrl: URL.createObjectURL(data.photo),
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


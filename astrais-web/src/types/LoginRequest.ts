import type { ITarea } from "./Interfaces";

export interface LoginRequest {
  email: string,
  passwd: string
}

export interface VerifyRequest {
  email: string,
  code: number
}

export interface RegisterRequest {
  name: string,
  email: string,
  passwd: string,
  lang: string, //en-EN
  utcOffset?: number // optional with default 0
}

export interface UserData {
  id: number;
  nombre: string;
  nivel: number;
  xpActual: number;
  xpTotal: number; 
  ludiones: number;
  personalGid: number;
  equippedPetRef: string | null;
  themeColors: string | null;
  isAdmin: boolean;
}

export interface UserGroups {
  gid?: number;
  id?: number;
  name?: string;
  nombre?: string;
  description: string;
  role: number;
}

export interface CreateGroup {
  name: string;
  desc: string;
}

export interface UserGroupsResponse {
    groupList: UserGroups[];
};

export interface EditGroup {
  gid: number,
  name: string,
  desc: string
}

export interface AddUserToGroup {
  guid: number;
  userid: number;
}

export interface CreateTask {
  gid: number;
  titulo: string;
  descripcion: string;
  tipo: 'UNICO' | 'HABITO' | 'OBJETIVO';
  prioridad: number;
  extraUnico?: {
    fechaLimite: string, // Formato ISO (date.toISOString()
  }
  extraHabito?: {
    numeroFrecuencia: number,
    frequency: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
  }
  idObjetivo?: number,
}

export interface UserTasksResponse {
  taskList: ITarea[];
};

export interface EditTask {
  titulo: string,
  descripcion: string,
  prioridad: string
}

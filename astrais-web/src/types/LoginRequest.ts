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
  personalGid: number;
  equipperPetRef: number; 
  themeColors: number | null; 
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
  gid: number;
  userId: number;
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

export interface EditUser {
  uid: string,
  nombreusu: string,
  lang: string,
  utcOffset: number
}

export interface SetEmailLogin {
  email: string,
  passwd: string
}

export interface MembersResponse {
  uid: number,
  name: string,
  role: number,
  joinedAt: string // "ISO 8601 | null"
}

export interface SetMemberRole {
  gid: number,
  userId: number,
  role: number
}

export interface EventosGrupos {
  id: number,
  actorUid: number,
  eventType: string
  payloadJson: string | null,
  createdAt: string,
}

export interface GroupInvitacion {
  gid: number,
  expiresInSeconds?: number,
  maxUses?: number
}

export interface GroupInvitacionRespuesta {
  code: string,
  inviteUrl: string,
  expiresAt: string | null,
  maxUses: number,
  usesCount: number,
  revokedAt: string | null,
}

export interface RevokeGroupInvit {
  gid: number,
  code: string
}

export interface RevokeGroupInvit {
  code: string,
  gid: number
}

export interface PassOwnershipGroup {
  gid: number,
  newOwnerUserId: number
}

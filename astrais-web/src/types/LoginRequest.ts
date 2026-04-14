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
  id: number;
  nombre: string;
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
  guid: number;
  userid: number;
}

export interface AddUserToGroup {
  guid: number;
  userid: number;
}


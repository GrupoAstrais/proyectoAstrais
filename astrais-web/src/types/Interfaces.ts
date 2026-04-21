export interface ITarea {
  id: number,
  gid: number;
  titulo: string;
  descripcion: string;
  tipo: 'UNIQUE' | 'HABIT' | 'OBJECTIVE';
  prioridad: number; //dificultad
  extraUnico?: [
    fechaLimite: string, // Formato ISO (date.toISOString()
    ]
  extraHabito?: [
    numeroFrecuencia?: number,
    frequency?: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
  ]
  idObjetivo?: number,
  estado: 'COMPLETE' | 'ACTIVE'
  recompensaXp: number,
  recompensaLudion: number
}

export interface IGroup {
  gid: number;
  name: string;
  description: string;
  photoUrl?: string | null;
  members: Array<{ id: number; name: string; avatar?: string }>;
  tasks: ITarea[];
  role: number;
}

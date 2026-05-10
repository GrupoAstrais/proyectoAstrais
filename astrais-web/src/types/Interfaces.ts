// Modelos base compartidos entre tareas, grupos y componentes.
export interface ITarea {
  id: number,
  gid: number;
  uid?: number | null;
  titulo: string;
  descripcion: string;
  tipo: 'UNICO' | 'HABITO' | 'OBJETIVO';
  prioridad: number; // dificultad numerica recibida del backend
  extraUnico?: {
    fechaLimite: string, // fecha limite en formato ISO
  } | [
    fechaLimite: string, // compatibilidad con tareas locales antiguas
  ]
  extraHabito?: {
    numeroFrecuencia: number;
    frequency: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  } | [
    numeroFrecuencia?: number,
    frequency?: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
  ];
  idObjetivo?: number | null,
  estado: 'COMPLETE' | 'ACTIVE'
  recompensaXp: number,
  recompensaLudion: number,
  fecha_actualizado?: string,
  fecha_creacion?: string,
  fecha_completado?: string | null,
  fechaValida?: string | null
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

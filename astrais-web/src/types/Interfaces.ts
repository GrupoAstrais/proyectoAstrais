export interface ITarea {
  id?: string;
  title: string;
  dificultad: "EASY" | "MEDIUM" | "HARD";
  recompensa: number;
  taskType: "habit" | "diary"; 
  tags?: { name: string; color?: string }[];
  isComposed: boolean; 
  subtasks: { id: string; name: string; completed: boolean }[]; 
  habitFrequency?: "daily" | "weekly" | "monthly"; 
  completed?: boolean;
  taskDate?: string;
}

export interface IGroup {
    id: number;
    name: string;
    description: string;
    photoUrl?: string | null;
    members: Array<{ id: number; name: string; avatar?: string }>;
    tasks: ITarea[];
}

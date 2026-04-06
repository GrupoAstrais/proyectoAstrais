export interface ITarea {
  title: string;
  dificultad: "EASY" | "MEDIUM" | "HARD";
  recompensa: number;
  taskType: "habit" | "diary"; 
  tags?: { name: string; color?: string }[];
  isComposed: boolean; 
  subtasks: { id: string; name: string; completed: boolean }[]; 
  habitFrequency?: "daily" | "weekly" | "monthly"; 
}
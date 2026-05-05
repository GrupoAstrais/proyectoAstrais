import { Trophy } from 'lucide-react';

interface AchievementCardProps {
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
}

export function AchievementCard({ rarity }: AchievementCardProps) {
  const getRarityStyles = () => {
    switch (rarity) {
      case 'common':
        return {
          bg: 'bg-gradient-to-br from-[#D1D5DB] to-[#9CA3AF]',
          border: 'border-[#6B7280]',
          label: 'Comun',
          labelBg: 'bg-[#F3F4F6]'
        };
      case 'rare':
        return {
          bg: 'bg-gradient-to-br from-[#60A5FA] to-[#3B82F6]',
          border: 'border-[#1E40AF]',
          label: 'Rara',
          labelBg: 'bg-[#DBEAFE]'
        };
      case 'epic':
        return {
          bg: 'bg-gradient-to-br from-[#C084FC] to-[#9333EA]',
          border: 'border-[#6B21A8]',
          label: 'Epica',
          labelBg: 'bg-[#F3E8FF]'
        };
      case 'legendary':
        return {
          bg: 'bg-gradient-to-br from-[#FCD34D] via-[#F59E0B] to-[#D97706]',
          border: 'border-[#92400E]',
          label: 'Legendaria',
          labelBg: 'bg-[#FEF3C7]'
        };
    }
  };

  const styles = getRarityStyles();

  return (
    <div className={`relative border-2 ${styles.border} rounded-xl ${styles.bg} overflow-hidden p-6 text-white`}>
      <div className="absolute top-2 right-2">
        <span className={`px-2 py-0.5 text-xs font-mono ${styles.labelBg} ${styles.border} border rounded text-gray-900`}>
          {styles.label}
        </span>
      </div>
      
      <div className="flex flex-col items-center text-center gap-3">
        <div className="w-16 h-16 bg-white/20 border-2 border-white rounded-lg flex items-center justify-center backdrop-blur-sm">
          <Trophy className="w-8 h-8" />
        </div>
        <div>
          <h4 className="font-mono text-xs mb-2">Explorador Estelar</h4>
          <p className="text-xs opacity-90">Completa 10 tareas espaciales</p>
        </div>
      </div>
    </div>
  );
}

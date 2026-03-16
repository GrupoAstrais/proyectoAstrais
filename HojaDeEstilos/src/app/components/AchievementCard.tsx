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
          glow: 'shadow-[0_0_8px_rgba(107,114,128,0.3)]',
          label: 'Común',
          labelBg: 'bg-[#F3F4F6]'
        };
      case 'rare':
        return {
          bg: 'bg-gradient-to-br from-[#60A5FA] to-[#3B82F6]',
          border: 'border-[#1E40AF]',
          glow: 'shadow-[0_0_12px_rgba(59,130,246,0.4)]',
          label: 'Rara',
          labelBg: 'bg-[#DBEAFE]'
        };
      case 'epic':
        return {
          bg: 'bg-gradient-to-br from-[#C084FC] to-[#9333EA]',
          border: 'border-[#6B21A8]',
          glow: 'shadow-[0_0_16px_rgba(147,51,234,0.5)]',
          label: 'Épica',
          labelBg: 'bg-[#F3E8FF]'
        };
      case 'legendary':
        return {
          bg: 'bg-gradient-to-br from-[#FCD34D] via-[#F59E0B] to-[#D97706]',
          border: 'border-[#92400E]',
          glow: 'shadow-[0_0_20px_rgba(245,158,11,0.6)]',
          label: 'Legendaria',
          labelBg: 'bg-[#FEF3C7]'
        };
    }
  };

  const styles = getRarityStyles();

  return (
    <div className={`relative border-4 ${styles.border} rounded-lg ${styles.bg} ${styles.glow} shadow-[6px_6px_0px_0px_rgba(30,30,46,1)] overflow-hidden p-6 text-white`}>
      <div className="absolute top-2 right-2">
        <span className={`px-2 py-0.5 text-xs font-['Press_Start_2P'] ${styles.labelBg} ${styles.border} border-2 rounded text-gray-900`}>
          {styles.label}
        </span>
      </div>
      
      <div className="flex flex-col items-center text-center gap-3">
        <div className="w-16 h-16 bg-white/20 border-4 border-white rounded-lg flex items-center justify-center backdrop-blur-sm">
          <Trophy className="w-8 h-8" />
        </div>
        <div>
          <h4 className="font-['Press_Start_2P'] text-xs mb-2">Explorador Estelar</h4>
          <p className="text-xs font-['Space_Grotesk'] opacity-90">Completa 10 tareas espaciales</p>
        </div>
      </div>
    </div>
  );
}
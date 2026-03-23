import { useState } from 'react';
import astra from '../../assets/astra.png'

export default function Calendar({ className = '' }: { className?: string }) {
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  const monthNames = [
    'ENERO', 'FEBRERO', 'MARZO', 'ABRIL', 'MAYO', 'JUNIO',
    'JULIO', 'AGOSTO', 'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'
  ];

  const dayNames = ['lun', 'mar', 'mie', 'jue', 'vie', 'sab', 'dom'];
  
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();
  const firstDayOfMonth = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startDayOfWeek = firstDayOfMonth.getDay(); 

  const getAdjustedDayIndex = (dayOfWeek: number) => (dayOfWeek + 6) % 7;

  const emptyCellsBefore = getAdjustedDayIndex(startDayOfWeek); 

  const days: (number | null)[] = Array(emptyCellsBefore).fill(null);
  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day);
  }

  const goToPreviousMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  const goToNextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  const handleDayClick = (day: number | null) => {
    if (day === null) return;
    const date = new Date(year, month, day);
    setSelectedDate(date);
    console.log('Selected:', date.toISOString().split('T')[0]);
  };

  return (
    <div >
      <div className='flex flex-row relative p-5'>
        <img src={astra} className='w-2/3 absolute -bottom-3 z-50 left-20 sm:left-0'/> 
      </div>
      <div className={`relative rounded-xl border border-white/15  overflow-hidden font-['Space_Grotesk'] ${className}`}>
      

        <div
          className="absolute inset-0 bg-linear-to-br from-primary-500/80 to-primary-700/85
          80"
        />


        <div className="relative z-10 p-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-white uppercase tracking-wider font-['Press_Start_2P']">
                  {monthNames[month]}
            </h2>
        </div>


          <div className='grid grid-cols-7 gap-1 mb-2 border-b border-white'>
            {dayNames.map((day) => (
              <div
                key={day}
                className="text-center font-['Press_Start_2P'] py-2 text-xs font-medium text-white uppercase tracking-wide"
              >
                {day}
              </div>
            ))}
          </div>


          <div className="grid grid-cols-7 gap-1">
            {days.map((day, index) => (
              <button
                key={index}
                onClick={() => handleDayClick(day)}
                disabled={day === null}
                className={`
                  aspect-square flex items-center justify-center
                  rounded-lg text-sm font-medium transition-all
                  ${day === null
                    ? 'invisible'
                    : 'bg-black/20 hover:bg-purple-900/50 text-white cursor-pointer'}
                  ${selectedDate?.getDate() === day &&
                  selectedDate?.getMonth() === month &&
                  selectedDate?.getFullYear() === year
                    ? 'bg-secondary-600 ring-2 ring-purple-400'
                    : ''}
                `}
              >
                {day}
              </button>
            ))}
          </div>
        </div>
      </div>
      <div className="flex gap-2 justify-between pt-2">
        <button
          onClick={goToPreviousMonth}
          className="w-10 h-10 rounded-full bg-secondary-700 hover:bg-secondary-500 flex items-center justify-center text-white transition-colors"
          aria-label="Mes anterior">
          <span className="text-lg">‹</span>
        </button>
        <button
          onClick={goToNextMonth}
          className="w-10 h-10 rounded-full bg-secondary-700 hover:bg-secondary-500 flex items-center justify-center text-white transition-colors"
          aria-label="Mes siguiente">
          <span className="text-lg">›</span>
        </button>
      </div>
    </div>
  );
};
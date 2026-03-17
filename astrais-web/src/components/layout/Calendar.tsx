import React, { useState, useEffect } from 'react';

const Calendar = ({ className = '' }: { className?: string }) => {
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  // Получаем название месяца и года (на испанском)
  const monthNames = [
    'ENERO', 'FEBRERO', 'MARZO', 'ABRIL', 'MAYO', 'JUNIO',
    'JULIO', 'AGOSTO', 'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'
  ];

  const dayNames = ['lun', 'mar', 'mie', 'jue', 'vie', 'sab', 'dom'];

  // Устанавливаем первый день месяца и количество дней
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startDayOfWeek = firstDay.getDay(); // 0 = domingo → но мы хотим lun=0 → сдвигнем

  // В Tailwind используем `grid-cols-7`, поэтому нужно смещение:
  // В нашем случае `lun` — это индекс 0, значит: если день недели == 0 (воскресенье), то он должен быть в позиции 6
  // Пересчитываем: `adjustedDay = (dayOfWeek + 6) % 7`
  const getAdjustedDayIndex = (dayOfWeek: number) => (dayOfWeek + 6) % 7;

  // Генерируем массив дней (включая пустые ячейки до 1-го числа)
  const emptyCellsBefore = getAdjustedDayIndex(firstDay.getDay()); // сколько пустых ячеек до 1-го числа

    const days: (number | null)[] = Array(emptyCellsBefore).fill(null);
  for (let i = 0; i < emptyCellsBefore; i++) {
    days.push(null);
  }
  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day);
  }

  // Навигация
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
    <div className={`relative rounded-xl overflow-hidden ${className}`}>
      {/* Фоновое изображение / градиент — можно заменить на bg-gradient или bg-[url] */}
      <div
        className="absolute inset-0 bg-linear-to-br from-primary-500/50 to-primary-700/50"
      />

      {/* Содержимое календаря — поверх фона */}
      <div className="relative z-10 p-4">
        {/* Заголовок месяца */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-white uppercase tracking-wider">
            {monthNames[month]}
          </h2>
          <div className="flex gap-2">
            <button
              onClick={goToPreviousMonth}
              className="w-10 h-10 rounded-full bg-purple-700 hover:bg-purple-600 flex items-center justify-center text-white transition-colors"
              aria-label="Mes anterior"
            >
              <span className="text-lg">‹</span>
            </button>
            <button
              onClick={goToNextMonth}
              className="w-10 h-10 rounded-full bg-purple-700 hover:bg-purple-600 flex items-center justify-center text-white transition-colors"
              aria-label="Mes siguiente"
            >
              <span className="text-lg">›</span>
            </button>
          </div>
        </div>

        {/* Дни недели */}
        <div className='grid grid-cols-7 gap-1 mb-2'>
          {dayNames.map((day) => (
            <div
              key={day}
              className="text-center py-2 text-xs font-medium text-purple-200 uppercase tracking-wide"
            >
              {day}
            </div>
          ))}
        </div>

        {/* Сетка дней */}
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
                  ? 'bg-purple-600 ring-2 ring-purple-400'
                  : ''}
              `}
            >
              {day}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Calendar;
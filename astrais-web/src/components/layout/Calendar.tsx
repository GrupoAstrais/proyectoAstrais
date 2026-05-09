import { useState } from 'react'
import astra from '../../assets/astra.png'

interface CalendarProps {
  className?: string
  selectedDate?: Date | null
  onSelectDate?: (date: Date) => void
}

function getMonthStart(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

export default function Calendar({ className = '', selectedDate, onSelectDate }: CalendarProps) {
  const [currentDate, setCurrentDate] = useState<Date>(() => getMonthStart(selectedDate ?? new Date()))
  const [localSelectedDate, setLocalSelectedDate] = useState<Date | null>(null)
  const activeSelectedDate = selectedDate ?? localSelectedDate

  const monthNames = [
    'ENERO', 'FEBRERO', 'MARZO', 'ABRIL', 'MAYO', 'JUNIO',
    'JULIO', 'AGOSTO', 'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'
  ]

  const dayNames = ['lun', 'mar', 'mie', 'jue', 'vie', 'sab', 'dom']

  const year = currentDate.getFullYear()
  const month = currentDate.getMonth()
  const firstDayOfMonth = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()
  const startDayOfWeek = firstDayOfMonth.getDay()

  const getAdjustedDayIndex = (dayOfWeek: number) => (dayOfWeek + 6) % 7

  const emptyCellsBefore = getAdjustedDayIndex(startDayOfWeek)

  const days: (number | null)[] = Array(emptyCellsBefore).fill(null)
  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day)
  }

  const goToPreviousMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1))
  }

  const goToNextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1))
  }

  const handleDayClick = (day: number | null) => {
    if (day === null) {
      return
    }

    const newSelectedDate = new Date(year, month, day)

    if (onSelectDate) {
      onSelectDate(newSelectedDate)
      return
    }

    setLocalSelectedDate(newSelectedDate)
  }

  return (
    <div>
      <div className="relative flex flex-row p-5">
        <img src={astra} alt="Mascota Astrais" className="absolute -bottom-3 left-20 z-50 w-2/3 sm:left-0" />
      </div>

      <div className={`relative overflow-hidden rounded-xl border border-white/15 font-['Space_Grotesk'] ${className}`}>
        <div className="absolute inset-0 bg-linear-to-br from-primary-500/80 to-primary-700/85" />

        <div className="relative z-10 p-4">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-['Press_Start_2P'] text-xl font-bold uppercase tracking-wider text-white">
              {monthNames[month]}
            </h2>
          </div>

          <div className="mb-2 grid grid-cols-7 gap-1 border-b border-white">
            {dayNames.map((day) => (
              <div
                key={day}
                className="py-2 text-center font-['Press_Start_2P'] text-xs font-medium uppercase tracking-wide text-white"
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
                    : 'cursor-pointer bg-black/20 text-white hover:bg-primary-900/50'}
                  ${activeSelectedDate?.getDate() === day &&
                  activeSelectedDate?.getMonth() === month &&
                  activeSelectedDate?.getFullYear() === year
                    ? 'bg-secondary-600 ring-2 ring-primary-500'
                    : ''}
                `}
              >
                {day}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="flex justify-between gap-2 pt-2">
        <button
          onClick={goToPreviousMonth}
          className="flex h-10 w-10 items-center justify-center rounded-full bg-secondary-700 text-white transition-colors hover:bg-secondary-500"
          aria-label="Mes anterior"
        >
          <span className="text-lg">&lt;</span>
        </button>
        <button
          onClick={goToNextMonth}
          className="flex h-10 w-10 items-center justify-center rounded-full bg-secondary-700 text-white transition-colors hover:bg-secondary-500"
          aria-label="Mes siguiente"
        >
          <span className="text-lg">&gt;</span>
        </button>
      </div>
    </div>
  )
}

import { useState } from 'react';
import { Rocket, Palette, Type, Layers, Sparkles, Grid3x3, Image as ImageIcon, Plus, Check, Clock, Repeat, Flag, FolderOpen, Star, Zap, TrendingUp, Gift, Award, Flame, Hexagon, Home, User, Package, Trophy, Settings, Bell } from 'lucide-react';
import { ColorSwatch } from './components/ColorSwatch';
import { TypographyExample } from './components/TypographyExample';
import { ButtonShowcase } from './components/ButtonShowcase';
import { TaskCard } from './components/TaskCard';
import { AchievementCard } from './components/AchievementCard';
import { XPBar } from './components/XPBar';
import { BadgeShowcase } from './components/BadgeShowcase';
import { IconShowcase } from './components/IconShowcase';
import { IconSizeDemo } from './components/IconSizeDemo';

export default function App() {
  const [activeSection, setActiveSection] = useState('intro');

  const scrollToSection = (id: string) => {
    setActiveSection(id);
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-[#0D1117]">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-[#1A1D2D] border-b border-[#11161D] shadow-lg">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 flex items-center justify-center">
                <svg viewBox="0 0 12.7 12.7" className="w-10 h-10" xmlns="http://www.w3.org/2000/svg"><path d="M 7.0720789,9.9099764 C 6.8911307,9.2500607 6.7419323,8.5979205 6.3560626,8.0024439 5.5819659,9.4888227 5.4245552,11.082023 5.254698,12.676277 5.157837,12.575277 5.164323,12.457229 5.1498718,12.343663 4.9901137,11.088181 4.6623193,9.874108 3.9658233,8.7525712 3.1774514,7.4830912 1.9913249,6.6776066 0.34059812,6.417131 0.2420478,6.4015827 0.14462375,6.3806299 0,6.3534061 0.17068965,6.275493 0.31660633,6.2750326 0.45276028,6.2526662 2.0327201,5.9931144 3.1496668,5.1939156 3.9266094,4.002326 4.5387479,3.0634954 4.8424109,2.0362133 5.058689,0.98382581 5.1177102,0.69662298 5.1445944,0.40697496 5.1876589,0.11861459 5.1930288,0.08264367 5.1856406,0.04267915 5.2687061,0 5.4281047,1.6037071 5.5769338,3.1943477 6.332032,4.7099575 6.4979519,4.546602 6.5595082,4.3803212 6.6256319,4.217046 6.8754036,3.6002866 7.0369158,2.9651933 7.1609106,2.3209674 7.2968641,1.6146069 7.3792134,0.90413704 7.4228731,0.18963555 c 0.00303,-0.0492199 -0.011525,-0.10268544 0.065989,-0.16546412 0.0797,0.15154681 0.065035,0.30757117 0.088581,0.45181452 C 7.7791192,1.7115417 8.092474,2.9182007 8.8090837,4.0166175 9.6304282,5.275569 10.830699,6.0761261 12.50911,6.2840241 c 0.06232,0.00772 0.132847,0.00202 0.19089,0.071377 -0.254258,0.051289 -0.49923,0.098144 -0.742713,0.1502477 C 10.639281,6.7876882 9.6654133,7.4724578 8.958442,8.459503 8.2327455,9.4726924 7.8819231,10.598858 7.6669686,11.764069 7.6163305,12.038562 7.5493841,12.311025 7.5368,12.590022 7.5350507,12.628317 7.5262592,12.663353 7.4363763,12.7 7.3754682,11.755575 7.2676755,10.836132 7.0720799,9.9099779 M 7.3515184,6.0008697 C 6.92238,5.7236249 6.6485147,5.335183 6.3627342,4.9162453 5.9573064,5.5449793 5.4893455,6.0762833 4.704278,6.3490314 5.4885395,6.6138334 5.9514087,7.1414236 6.3584899,7.7711392 6.7777977,7.1434133 7.2418892,6.5990158 8.0699592,6.3633323 7.8185775,6.231527 7.5724089,6.1615952 7.3515184,6.0008697 Z" fill="#ffffff" /></svg>
              </div>
              <div>
                <h1 className="font-mono text-lg text-[#F8FAFC] font-bold">ASTRAIS</h1>
                <p className="text-xs text-[#D1D5DB] mt-1">Design System</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="sticky top-[76px] z-40 bg-[#11161D] border-b border-[#1A1D2D] shadow-md">
        <div className="container mx-auto px-6">
          <div className="flex gap-1 overflow-x-auto py-2 scrollbar-hide">
            {[
              { id: 'intro', label: 'Intro', icon: Rocket },
              { id: 'colors', label: 'Colores', icon: Palette },
              { id: 'typography', label: 'Tipografia', icon: Type },
              { id: 'iconography', label: 'Iconografia', icon: ImageIcon },
              { id: 'components', label: 'Componentes', icon: Layers },
              { id: 'gamification', label: 'Gamificacion', icon: Sparkles },
              { id: 'spacing', label: 'Espaciado', icon: Grid3x3 },
              { id: 'platforms', label: 'Plataformas', icon: Grid3x3 },
            ].map((item) => (
              <button
                key={item.id}
                onClick={() => scrollToSection(item.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-all whitespace-nowrap ${
                  activeSection === item.id
                    ? 'bg-[#8B5CF6] text-white'
                    : 'bg-[#1A1D2D] text-[#D1D5DB] hover:bg-[#8B5CF6]/20 hover:text-[#F8FAFC]'
                }`}
              >
                <item.icon className="w-4 h-4" />
                <span className="text-xs font-medium">{item.label}</span>
              </button>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="container mx-auto px-6 py-12">
        <div className="max-w-6xl mx-auto space-y-16">
          
          {/* Intro Section */}
          <section id="intro" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] border border-[#11161D] rounded-2xl p-8">
              <div className="text-center mb-8">
                <h2 className="font-mono text-2xl text-[#F8FAFC] font-bold mb-4">ASTRAIS Design System</h2>
                <p className="text-lg text-[#D1D5DB] max-w-2xl mx-auto">
                  Sistema de diseno unificado para Android (Jetpack Compose) y Web (React + TailwindCSS). Define tokens, componentes y guias de estilo compartidos.
                </p>
              </div>
              
              <div className="grid md:grid-cols-3 gap-6 mt-8">
                <div className="bg-[#0D1117] border border-[#8B5CF6]/30 rounded-xl p-6 text-center">
                  <h3 className="font-mono text-sm text-[#8B5CF6] mb-2">Gamificacion</h3>
                  <p className="text-sm text-[#D1D5DB]">XP, niveles, logros y recompensas</p>
                </div>
                
                <div className="bg-[#0D1117] border border-[#38BDF8]/30 rounded-xl p-6 text-center">
                  <h3 className="font-mono text-sm text-[#38BDF8] mb-2">Cross-Platform</h3>
                  <p className="text-sm text-[#D1D5DB]">Mismos tokens y estilos en Android y Web</p>
                </div>
                
                <div className="bg-[#0D1117] border border-[#10B981]/30 rounded-xl p-6 text-center">
                  <h3 className="font-mono text-sm text-[#10B981] mb-2">Productividad</h3>
                  <p className="text-sm text-[#D1D5DB]">Organizacion de tareas con grupos y prioridades</p>
                </div>
              </div>
            </div>
          </section>

          {/* Colors Section */}
          <section id="colors" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Palette className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Paleta de Colores</h2>
              </div>

              

              {/* Primary Colors */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Colores Primarios</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Primary" hex="#8B5CF6" token="color.primary" description="Color principal de la marca" large />
                  <ColorSwatch name="On Primary" hex="#FFFFFF" token="color.onPrimary" description="Texto/iconos sobre primary" large />
                </div>
              </div>

              {/* Secondary Colors */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#38BDF8]">Colores Secundarios</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Secondary" hex="#38BDF8" token="color.secondary" description="Color secundario" large />
                  <ColorSwatch name="On Secondary" hex="#FFFFFF" token="color.onSecondary" description="Texto/iconos sobre secondary" large />
                </div>
              </div>

              {/* Tertiary Colors */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#10B981]">Colores Terciarios</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Tertiary" hex="#10B981" token="color.tertiary" description="Color de acento" large />
                  <ColorSwatch name="On Tertiary" hex="#FFFFFF" token="color.onTertiary" description="Texto/iconos sobre tertiary" large />
                </div>
              </div>

              {/* Background & Surface */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4">Fondos y Superficies</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Background" hex="#0D1117" token="color.background" description="Fondo principal" />
                  <ColorSwatch name="Background Alt" hex="#11161D" token="color.backgroundAlt" description="Fondo alternativo" />
                  <ColorSwatch name="Surface" hex="#1A1D2D" token="color.surface" description="Superficie de tarjetas" />
                  <ColorSwatch name="Text" hex="#F8FAFC" token="color.text" description="Texto principal" />
                </div>
              </div>

              {/* State Colors */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4">Estados</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Error" hex="#F43F5E" token="color.error" description="Errores y alertas" />
                  <ColorSwatch name="On Error" hex="#FFFFFF" token="color.onError" description="Texto sobre error" />
                </div>
              </div>

              {/* Grayscale */}
              <div>
                <h3 className="font-mono text-sm mb-4">Escala de Grises</h3>
                <div className="grid grid-cols-3 md:grid-cols-6 gap-4">
                  <ColorSwatch name="Gray 300" hex="#D1D5DB" token="color.gray.300" />
                  <ColorSwatch name="Gray 500" hex="#6B7280" token="color.gray.500" />
                  <ColorSwatch name="Gray 700" hex="#374151" token="color.gray.700" />
                  <ColorSwatch name="Gray 900" hex="#111827" token="color.gray.900" />
                </div>
              </div>
            </div>
          </section>

          {/* Typography Section */}
          <section id="typography" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Type className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Tipografia</h2>
              </div>


              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Escala Tipografica</h3>
                <div className="space-y-4">
                  <TypographyExample
                    tag="Display"
                    font="Monospace"
                    webSize="2.125rem (34px)"
                    androidSize="34sp"
                    weight="Black (900)"
                    lineHeight="1.12"
                    example="ASTRAIS"
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '34px', fontWeight: 900, lineHeight: '1.12', color: '#F8FAFC' }}
                  />
                  <TypographyExample
                    tag="Headline"
                    font="Monospace"
                    webSize="1.625rem (26px)"
                    androidSize="26sp"
                    weight="Bold (700)"
                    lineHeight="1.15"
                    example="Tareas de hoy"
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '26px', fontWeight: 700, lineHeight: '1.15', color: '#F8FAFC' }}
                  />
                  <TypographyExample
                    tag="Title Large"
                    font="Monospace"
                    webSize="1.25rem (20px)"
                    androidSize="20sp"
                    weight="Bold (700)"
                    lineHeight="1.2"
                    example="Grupo: Trabajo"
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '20px', fontWeight: 700, lineHeight: '1.2', color: '#F8FAFC' }}
                  />
                  <TypographyExample
                    tag="Title Medium"
                    font="Monospace"
                    webSize="1rem (16px)"
                    androidSize="16sp"
                    weight="SemiBold (600)"
                    lineHeight="1.25"
                    example="Detalles de tarea"
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '16px', fontWeight: 600, lineHeight: '1.25', color: '#F8FAFC' }}
                  />
                  <TypographyExample
                    tag="Body Large"
                    font="Monospace"
                    webSize="1rem (16px)"
                    androidSize="16sp"
                    weight="Normal (400)"
                    lineHeight="1.375"
                    example="Texto principal de la aplicacion con buena legibilidad para descripciones y contenido."
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '16px', fontWeight: 400, lineHeight: '1.375', color: '#D1D5DB' }}
                  />
                  <TypographyExample
                    tag="Body Medium"
                    font="Monospace"
                    webSize="0.875rem (14px)"
                    androidSize="14sp"
                    weight="Normal (400)"
                    lineHeight="1.43"
                    example="Texto secundario para notas y metadata."
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '14px', fontWeight: 400, lineHeight: '1.43', color: '#D1D5DB' }}
                  />
                  <TypographyExample
                    tag="Label"
                    font="Monospace"
                    webSize="0.75rem (12px)"
                    androidSize="12sp"
                    weight="Medium (500)"
                    lineHeight="1.33"
                    example="Etiquetas y botones"
                    style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: '12px', fontWeight: 500, lineHeight: '1.33', color: '#D1D5DB' }}
                  />
                </div>
              </div>
            </div>
          </section>

          {/* Iconography Section */}
          <section id="iconography" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <ImageIcon className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Iconografia</h2>
              </div>

              

              {/* Icon Sizes */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Sistema de Tamanos</h3>
                <IconSizeDemo />
              </div>

              {/* Task Icons */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Iconos de Tareas</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Plus size={24} />} name="Anadir Tarea" token="icon.task.add" library="Lucide" size={24} />
                  <IconShowcase icon={<Check size={24} />} name="Completar" token="icon.task.complete" library="Lucide" size={24} />
                  <IconShowcase icon={<Clock size={24} />} name="En Progreso" token="icon.task.progress" library="Lucide" size={24} />
                  <IconShowcase icon={<Repeat size={24} />} name="Repetitiva" token="icon.task.recurring" library="Lucide" size={24} />
                  <IconShowcase icon={<Flag size={24} />} name="Prioridad Alta" token="icon.task.priority" library="Lucide" size={24} />
                  <IconShowcase icon={<FolderOpen size={24} />} name="Categoria" token="icon.task.category" library="Lucide" size={24} />
                </div>
              </div>

              {/* Gamification Icons */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#38BDF8]">Iconos de Gamificacion</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Star size={24} />} name="Estrella" token="icon.gamification.star" library="Lucide" size={24} />
                  <IconShowcase icon={<Zap size={24} />} name="XP" token="icon.gamification.xp" library="Lucide" size={24} />
                  <IconShowcase icon={<TrendingUp size={24} />} name="Nivel" token="icon.gamification.level" library="Lucide" size={24} />
                  <IconShowcase icon={<Gift size={24} />} name="Cofre" token="icon.gamification.chest" library="Lucide" size={24} />
                  <IconShowcase icon={<Award size={24} />} name="Logro" token="icon.gamification.achievement" library="Lucide" size={24} />
                  <IconShowcase icon={<Flame size={24} />} name="Racha Diaria" token="icon.gamification.streak" library="Lucide" size={24} />
                  <IconShowcase icon={<Hexagon size={24} />} name="Constelacion" token="icon.gamification.constellation" library="Lucide" size={24} />
                </div>
              </div>

              {/* Navigation Icons */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#10B981]">Iconos de Navegacion</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Home size={24} />} name="Home" token="icon.navigation.home" library="Lucide" size={24} />
                  <IconShowcase icon={<User size={24} />} name="Perfil" token="icon.navigation.profile" library="Lucide" size={24} />
                  <IconShowcase icon={<Package size={24} />} name="Inventario" token="icon.navigation.inventory" library="Lucide" size={24} />
                  <IconShowcase icon={<Trophy size={24} />} name="Recompensas" token="icon.navigation.rewards" library="Lucide" size={24} />
                  <IconShowcase icon={<Settings size={24} />} name="Ajustes" token="icon.navigation.settings" library="Lucide" size={24} />
                  <IconShowcase icon={<Bell size={24} />} name="Notificaciones" token="icon.navigation.notifications" library="Lucide" size={24} />
                </div>
              </div>
            </div>
          </section>

          {/* Components Section */}
          <section id="components" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Layers className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Componentes</h2>
              </div>

              {/* Buttons */}
              <div className="mb-12">
                <h3 className="font-mono text-sm mb-2 text-[#8B5CF6]">Botones</h3>
                <p className="text-[#D1D5DB] mb-6">
                  Tres variantes con estados claros: default, hover/pressed, disabled. Bordes redondeados consistentes con el sistema de shapes.
                </p>
                <div className="space-y-6">
                  <div>
                    <h4 className="font-semibold mb-3 text-[#F8FAFC]">Boton Primario</h4>
                    <ButtonShowcase variant="primary" label="Completar Tarea" />
                  </div>
                  <div>
                    <h4 className="font-semibold mb-3 text-[#F8FAFC]">Boton Secundario</h4>
                    <ButtonShowcase variant="secondary" label="Ver Detalles" />
                  </div>
                  <div>
                    <h4 className="font-semibold mb-3 text-[#F8FAFC]">Boton Terciario</h4>
                    <ButtonShowcase variant="tertiary" label="Explorar" />
                  </div>
                </div>
              </div>

              {/* Task Card */}
              <div className="mb-12">
                <h3 className="font-mono text-sm mb-2 text-[#8B5CF6]">Tarjeta de Tarea</h3>
                <p className="text-[#D1D5DB] mb-6">
                  Tarjetas con fondo surface, bordes sutiles y tags de prioridad/XP.
                </p>
                <TaskCard />
              </div>

              {/* XP Bar */}
              <div className="mb-12">
                <h3 className="font-mono text-sm mb-2 text-[#8B5CF6]">Barra de Progreso XP</h3>
                <p className="text-[#D1D5DB] mb-6">
                  Barra de progreso con gradiente primary-secondary.
                </p>
                <XPBar />
              </div>

              {/* Badges */}
              <div className="mb-12">
                <h3 className="font-mono text-sm mb-2 text-[#8B5CF6]">Sistema de Badges</h3>
                <p className="text-[#D1D5DB] mb-6">
                  Badges circulares con colores de rareza para logros y recompensas.
                </p>
                <div className="bg-[#0D1117] rounded-xl p-6">
                  <BadgeShowcase />
                </div>
              </div>
            </div>
          </section>

          {/* Gamification Section */}
          <section id="gamification" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Sparkles className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Sistema de Gamificacion</h2>
              </div>

              {/* Levels */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Sistema de Niveles</h3>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="border border-[#D1D5DB] rounded-xl p-4 bg-[#0D1117]">
                    <div className="font-mono text-xs mb-2 text-[#D1D5DB]">Nivel 1-5</div>
                    <div className="text-sm text-[#F8FAFC]">Explorador Novato</div>
                  </div>
                  <div className="border border-[#38BDF8] rounded-xl p-4 bg-[#0D1117]">
                    <div className="font-mono text-xs mb-2 text-[#38BDF8]">Nivel 6-15</div>
                    <div className="text-sm text-[#F8FAFC]">Navegante Cosmico</div>
                  </div>
                  <div className="border border-[#8B5CF6] rounded-xl p-4 bg-[#0D1117]">
                    <div className="font-mono text-xs mb-2 text-[#8B5CF6]">Nivel 16-30</div>
                    <div className="text-sm text-[#F8FAFC]">Guardian Galactico</div>
                  </div>
                  <div className="border border-[#F59E0B] rounded-xl p-4 bg-[#0D1117]">
                    <div className="font-mono text-xs mb-2 text-[#F59E0B]">Nivel 31+</div>
                    <div className="text-sm text-[#F8FAFC]">Maestro del Universo</div>
                  </div>
                </div>
              </div>

              {/* Rarity System */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#38BDF8]">Rareza de Recompensas</h3>
                <div className="grid md:grid-cols-2 gap-6">
                  <AchievementCard rarity="common" />
                  <AchievementCard rarity="rare" />
                  <AchievementCard rarity="epic" />
                  <AchievementCard rarity="legendary" />
                </div>
              </div>
            </div>
          </section>

          {/* Spacing Section */}
          <section id="spacing" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Grid3x3 className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Sistema de Espaciado</h2>
              </div>

              <div className="mb-6 p-4 bg-[#0D1117] rounded-lg border border-[#11161D]">
                <p className="text-sm text-[#D1D5DB]">
                  <strong className="text-[#F8FAFC]">Unidades:</strong> Android usa <code className="px-1 py-0.5 bg-[#1A1D2D] rounded text-[#8B5CF6]">dp</code> (density-independent), Web usa <code className="px-1 py-0.5 bg-[#1A1D2D] rounded text-[#8B5CF6]">px</code>/<code className="px-1 py-0.5 bg-[#1A1D2D] rounded text-[#8B5CF6]">rem</code>. Los valores numericos son equivalentes (1dp = 1px a 160dpi).
                </p>
              </div>

              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Spacing Scale</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    { name: 'xs', web: '4px', android: '4dp' },
                    { name: 'sm', web: '8px', android: '8dp' },
                    { name: 'md', web: '12px', android: '12dp' },
                    { name: 'lg', web: '16px', android: '16dp' },
                    { name: 'xl', web: '24px', android: '24dp' },
                    { name: 'xxl', web: '32px', android: '32dp' },
                  ].map((item) => (
                    <div key={item.name} className="border border-[#11161D] rounded-lg p-4 bg-[#0D1117]">
                      <div className="font-mono text-xs mb-2 text-[#F8FAFC]">{item.name}</div>
                      <div className="text-sm text-[#D1D5DB]">Web: {item.web}</div>
                      <div className="text-xs text-[#6B7280]">Android: {item.android}</div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#38BDF8]">Border Radius</h3>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                  {[
                    { name: 'xs', web: '6px', android: '6dp' },
                    { name: 'sm', web: '10px', android: '10dp' },
                    { name: 'md', web: '14px', android: '14dp' },
                    { name: 'lg', web: '20px', android: '20dp' },
                    { name: 'xl', web: '28px', android: '28dp' },
                  ].map((item) => (
                    <div key={item.name} className="text-center">
                      <div className="h-16 bg-[#8B5CF6] mb-2" style={{ borderRadius: item.web }} />
                      <div className="font-mono text-xs mb-1 text-[#F8FAFC]">{item.name}</div>
                      <div className="text-xs text-[#D1D5DB]">Web: {item.web}</div>
                      <div className="text-xs text-[#6B7280]">Android: {item.android}</div>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="font-mono text-sm mb-4 text-[#10B981]">Tamanos de Iconos</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    { name: 'sm', value: '16px', size: 'w-4 h-4' },
                    { name: 'md', value: '24px', size: 'w-6 h-6' },
                    { name: 'lg', value: '32px', size: 'w-8 h-8' },
                    { name: 'xl', value: '48px', size: 'w-12 h-12' },
                  ].map((item) => (
                    <div key={item.name} className="border border-[#11161D] rounded-lg p-4 bg-[#0D1117] flex flex-col items-center gap-2">
                      <Sparkles className={`${item.size} text-[#8B5CF6]`} />
                      <div className="font-mono text-xs text-[#F8FAFC]">{item.name}</div>
                      <div className="text-xs text-[#D1D5DB]">{item.value}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>

          {/* Platform Mapping Section */}
          <section id="platforms" className="scroll-mt-32">
            <div className="bg-[#1A1D2D] rounded-2xl border border-[#11161D] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Grid3x3 className="w-8 h-8 text-[#8B5CF6]" />
                <h2 className="font-mono text-xl text-[#F8FAFC] font-bold">Mapeo por Plataforma</h2>
              </div>

              {/* Color Token Mapping */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#8B5CF6]">Tokens de Color</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-[#11161D]">
                        <th className="text-left py-2 px-3 text-[#D1D5DB] font-mono">Token</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Android (Color.kt)</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Web (colors.css)</th>
                      </tr>
                    </thead>
                    <tbody className="text-[#F8FAFC]">
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.primary</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#8B5CF6]">Primary</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#8B5CF6]">--astrais-primary</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.secondary</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#38BDF8]">Secondary</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#38BDF8]">--astrais-secondary</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.tertiary</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#10B981]">Tertiary</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#10B981]">--astrais-tertiary</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.background</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">Background</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">--astrais-background</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.backgroundAlt</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">BackgroundAlt</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">--astrais-background-alt</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.surface</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">Surface</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">--astrais-surface</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">color.text</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">TextPrimary</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">--astrais-text</code></td>
                      </tr>
                      <tr>
                        <td className="py-2 px-3 font-mono text-xs">color.error</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#F43F5E]">ErrorCustom</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded text-[#F43F5E]">--astrais-error</code></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Typography Token Mapping */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#38BDF8]">Tokens de Tipografia</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-[#11161D]">
                        <th className="text-left py-2 px-3 text-[#D1D5DB] font-mono">Token</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Android (Type.kt)</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Web (theme.css)</th>
                      </tr>
                    </thead>
                    <tbody className="text-[#F8FAFC]">
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.display</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">displaySmall</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">h1</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.headline</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">headlineSmall</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">h2</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.titleLarge</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">titleLarge</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">h3</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.titleMedium</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">titleMedium</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">h4</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.bodyLarge</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">bodyLarge</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">typography.bodyMedium</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">bodyMedium</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">small</code></td>
                      </tr>
                      <tr>
                        <td className="py-2 px-3 font-mono text-xs">typography.label</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">labelMedium</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">label</code></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Spacing Token Mapping */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4 text-[#10B981]">Tokens de Espaciado</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-[#11161D]">
                        <th className="text-left py-2 px-3 text-[#D1D5DB] font-mono">Token</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Android (Spacing.kt)</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Web (Tailwind)</th>
                      </tr>
                    </thead>
                    <tbody className="text-[#F8FAFC]">
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">spacing.xs</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">xs (4.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-1 (4px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">spacing.sm</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">sm (8.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-2 (8px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">spacing.md</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">md (12.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-3 (12px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">spacing.lg</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">lg (16.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-4 (16px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">spacing.xl</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">xl (24.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-6 (24px)</code></td>
                      </tr>
                      <tr>
                        <td className="py-2 px-3 font-mono text-xs">spacing.xxl</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">xxl (32.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">p-8 (32px)</code></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Shape Token Mapping */}
              <div className="mb-8">
                <h3 className="font-mono text-sm mb-4">Tokens de Formas</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-[#11161D]">
                        <th className="text-left py-2 px-3 text-[#D1D5DB] font-mono">Token</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Android (Shapes.kt)</th>
                        <th className="text-left py-2 px-3 text-[#D1D5DB]">Web (Tailwind)</th>
                      </tr>
                    </thead>
                    <tbody className="text-[#F8FAFC]">
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">shape.xs</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">extraSmall (6.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">rounded (6px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">shape.sm</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">small (10.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">rounded-md (10px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">shape.md</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">medium (14.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">rounded-lg (14px)</code></td>
                      </tr>
                      <tr className="border-b border-[#11161D]/50">
                        <td className="py-2 px-3 font-mono text-xs">shape.lg</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">large (20.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">rounded-xl (20px)</code></td>
                      </tr>
                      <tr>
                        <td className="py-2 px-3 font-mono text-xs">shape.xl</td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">extraLarge (28.dp)</code></td>
                        <td className="py-2 px-3"><code className="px-1 py-0.5 bg-[#0D1117] rounded">rounded-2xl (28px)</code></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Framework Summary */}
              <div className="mt-8 p-4 bg-[#0D1117] rounded-lg border border-[#11161D]">
                <h3 className="font-mono text-sm mb-3 text-[#F8FAFC]">Resumen de Implementacion</h3>
                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="font-mono text-xs text-[#8B5CF6] mb-2">Android</h4>
                    <ul className="text-sm text-[#D1D5DB] space-y-1">
                      <li>Framework: Jetpack Compose</li>
                      <li>UI: Material 3 (MaterialTheme)</li>
                      <li>Colores: Color.kt</li>
                      <li>Tipografia: Type.kt</li>
                      <li>Espaciado: Spacing.kt</li>
                      <li>Formas: Shapes.kt</li>
                      <li>Iconos: Lucide Compose / ImageVector</li>
                    </ul>
                  </div>
                  <div>
                    <h4 className="font-mono text-xs text-[#38BDF8] mb-2">Web</h4>
                    <ul className="text-sm text-[#D1D5DB] space-y-1">
                      <li>Framework: React + Vite</li>
                      <li>UI: TailwindCSS v4</li>
                      <li>Colores: colors.css (CSS custom properties)</li>
                      <li>Tipografia: theme.css (@layer base)</li>
                      <li>Espaciado: Tailwind spacing scale</li>
                      <li>Formas: Tailwind rounded utilities</li>
                      <li>Iconos: Lucide React</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </section>

        </div>
      </main>
    </div>
  );
}

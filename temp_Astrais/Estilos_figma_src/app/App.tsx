import { useState } from 'react';
import { Rocket, Palette, Type, Layers, Sparkles, Grid3x3, Heart, Image as ImageIcon, Plus, Check, Clock, Repeat, Flag, FolderOpen, Star, Zap, TrendingUp, Gift, Award, Flame, Hexagon, Home, User, Package, Trophy, Settings, Bell, ChevronRight } from 'lucide-react';
import { ColorSwatch } from './components/ColorSwatch';
import { TypographyExample } from './components/TypographyExample';
import { ButtonShowcase } from './components/ButtonShowcase';
import { TaskCard } from './components/TaskCard';
import { AchievementCard } from './components/AchievementCard';
import { XPBar } from './components/XPBar';
import { BadgeShowcase } from './components/BadgeShowcase';
import { IconShowcase } from './components/IconShowcase';
import { PixelIconDemo } from './components/PixelIconDemo';
import { IconSizeDemo } from './components/IconSizeDemo';

export default function App() {
  const [activeSection, setActiveSection] = useState('intro');

  const scrollToSection = (id: string) => {
    setActiveSection(id);
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#4752C4] via-[#5d37aa] to-[#10254a]">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-[#1E1E2E] border-b-4 border-[#FFD700] shadow-lg">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-gradient-to-br from-[#8B5CF6] to-[#5865F2] border-4 border-[#FFD700] rounded-lg flex items-center justify-center shadow-[0_0_16px_rgba(255,215,0,0.4)]">
                <Rocket className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="font-['Press_Start_2P'] text-sm text-white">ASTRAIS</h1>
                <p className="text-xs text-gray-400 font-['Space_Grotesk'] mt-1">Design System Guide</p>
              </div>
            </div>
            <div className="text-xs text-gray-400 font-['Space_Grotesk']">v1.0</div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="sticky top-[76px] z-40 bg-[#2D2D3D] border-b-4 border-[#1E1E2E] shadow-md">
        <div className="container mx-auto px-6">
          <div className="flex gap-1 overflow-x-auto py-2 scrollbar-hide">
            {[
              { id: 'intro', label: 'Intro', icon: Rocket },
              { id: 'colors', label: 'Colores', icon: Palette },
              { id: 'typography', label: 'Tipografía', icon: Type },
              { id: 'iconography', label: 'Iconografía', icon: ImageIcon },
              { id: 'components', label: 'Componentes', icon: Layers },
              { id: 'gamification', label: 'Gamificación', icon: Sparkles },
              { id: 'spacing', label: 'Espaciado', icon: Grid3x3 },
              { id: 'personality', label: 'Personalidad', icon: Heart },
            ].map((item) => (
              <button
                key={item.id}
                onClick={() => scrollToSection(item.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded border-2 transition-all whitespace-nowrap ${
                  activeSection === item.id
                    ? 'bg-[#5865F2] border-[#FFD700] text-white shadow-[0_0_8px_rgba(255,215,0,0.3)]'
                    : 'bg-[#1E1E2E] border-[#3D3D4D] text-gray-400 hover:border-[#5865F2] hover:text-white'
                }`}
              >
                <item.icon className="w-4 h-4" />
                <span className="text-xs font-['Space_Grotesk'] font-medium">{item.label}</span>
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
            <div className="bg-gradient-to-br from-[#1E1E2E] to-[#2D2D3D] border-4 border-[#FFD700] rounded-2xl p-8 shadow-[8px_8px_0px_0px_rgba(255,215,0,0.3)]">
              <div className="text-center mb-8">
                <h2 className="font-['Press_Start_2P'] text-2xl text-white mb-4">🪐 ASTRAIS</h2>
                <p className="text-lg text-gray-300 font-['Space_Grotesk'] max-w-2xl mx-auto">
                  Una app de tareas que gamifica tu vida con una experiencia motivadora, mágica y progresiva inspirada en el universo.
                </p>
              </div>
              
              <div className="grid md:grid-cols-3 gap-6 mt-8">
                <div className="bg-[#0F3547] border-4 border-[#4ECDC4] rounded-lg p-6 text-center">
                  <div className="text-3xl mb-2">🎮</div>
                  <h3 className="font-['Press_Start_2P'] text-xs text-[#4ECDC4] mb-2">Gamificación</h3>
                  <p className="text-sm text-gray-400 font-['Space_Grotesk']">XP, niveles, logros y recompensas cósmicas</p>
                </div>
                
                <div className="bg-[#1E4A63] border-4 border-[#8B5CF6] rounded-lg p-6 text-center">
                  <div className="text-3xl mb-2">✨</div>
                  <h3 className="font-['Press_Start_2P'] text-xs text-[#8B5CF6] mb-2">Pixel Art</h3>
                  <p className="text-sm text-gray-400 font-['Space_Grotesk']">Estilo retro moderno inspirado en 8-bit/16-bit</p>
                </div>
                
                <div className="bg-[#2D5F7F] border-4 border-[#9FE8C5] rounded-lg p-6 text-center">
                  <div className="text-3xl mb-2">🌌</div>
                  <h3 className="font-['Press_Start_2P'] text-xs text-[#9FE8C5] mb-2">Espacial</h3>
                  <p className="text-sm text-gray-400 font-['Space_Grotesk']">Universo, estrellas y exploración galáctica</p>
                </div>
              </div>
            </div>
          </section>

          {/* Colors Section */}
          <section id="colors" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Palette className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Paleta de Colores</h2>
              </div>

              {/* Primary Colors */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">Colores Primarios</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Índigo Galaxia" hex="#5865F2" token="Color/Primary/500" large />
                  <ColorSwatch name="Índigo Medio" hex="#4752C4" token="Color/Primary/600" large />
                  <ColorSwatch name="Índigo Oscuro" hex="#363F96" token="Color/Primary/700" large />
                  <ColorSwatch name="Petróleo Profundo" hex="#1E4A63" token="Color/Primary/900" large />
                </div>
              </div>

              {/* Secondary Colors */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Colores Secundarios</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Morado Nebulosa" hex="#8B5CF6" token="Color/Secondary/500" large />
                  <ColorSwatch name="Morado Medio" hex="#7C3AED" token="Color/Secondary/600" large />
                  <ColorSwatch name="Morado Oscuro" hex="#6D28D9" token="Color/Secondary/700" large />
                  <ColorSwatch name="Turquesa Cósmico" hex="#4ECDC4" token="Color/Accent/500" large />
                </div>
              </div>

              {/* Accent Colors */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#22A39A]">Colores de Acento</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Verde Menta" hex="#9FE8C5" token="Color/Accent/Mint/300" />
                  <ColorSwatch name="Verde Medio" hex="#7DD8A8" token="Color/Accent/Mint/400" />
                  <ColorSwatch name="Verde Intenso" hex="#5BC88B" token="Color/Accent/Mint/500" />
                  <ColorSwatch name="Beige Estelar" hex="#E8DCC4" token="Color/Accent/Beige/300" />
                </div>
              </div>

              {/* State Colors */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Estados</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Success" hex="#10B981" token="Color/State/Success" />
                  <ColorSwatch name="Warning" hex="#F59E0B" token="Color/State/Warning" />
                  <ColorSwatch name="Error" hex="#EF4444" token="Color/State/Error" />
                  <ColorSwatch name="Info" hex="#3B82F6" token="Color/State/Info" />
                </div>
              </div>

              {/* Backgrounds */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Fondos</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <ColorSwatch name="Fondo Claro" hex="#F8F9FA" token="Color/Background/Light" />
                  <ColorSwatch name="Fondo Oscuro" hex="#0F3547" token="Color/Background/Dark" />
                  <ColorSwatch name="Surface Light" hex="#FFFFFF" token="Color/Surface/Light" />
                  <ColorSwatch name="Surface Dark" hex="#1E1E2E" token="Color/Surface/Dark" />
                </div>
              </div>

              {/* Grayscale */}
              <div>
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Escala de Grises</h3>
                <div className="grid grid-cols-3 md:grid-cols-6 gap-4">
                  <ColorSwatch name="Gray 50" hex="#F9FAFB" token="Color/Gray/50" />
                  <ColorSwatch name="Gray 100" hex="#F3F4F6" token="Color/Gray/100" />
                  <ColorSwatch name="Gray 300" hex="#D1D5DB" token="Color/Gray/300" />
                  <ColorSwatch name="Gray 500" hex="#6B7280" token="Color/Gray/500" />
                  <ColorSwatch name="Gray 700" hex="#374151" token="Color/Gray/700" />
                  <ColorSwatch name="Gray 900" hex="#111827" token="Color/Gray/900" />
                </div>
              </div>
            </div>
          </section>

          {/* Typography Section */}
          <section id="typography" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Type className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Tipografía</h2>
              </div>

              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">Fuente Principal: Press Start 2P</h3>
                <p className="text-gray-600 mb-4 font-['Space_Grotesk']">
                  Tipografía pixel art para títulos, números de XP, niveles y elementos de gamificación. Evoca nostalgia retro.
                </p>
                <TypographyExample
                  tag="H1 / Display"
                  font="Press Start 2P"
                  size="24px"
                  weight="400"
                  lineHeight="1.4"
                  example="NIVEL 12: NAVEGANTE CÓSMICO"
                  style={{ fontFamily: "'Press Start 2P'", fontSize: '24px', lineHeight: '1.4' }}
                />
              </div>

              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Fuente Secundaria: Space Grotesk</h3>
                <p className="text-gray-600 mb-4 font-['Space_Grotesk']">
                  Tipografía moderna y legible para UI, body text y elementos que requieren alta legibilidad.
                </p>
                <div className="space-y-4">
                  <TypographyExample
                    tag="H2"
                    font="Space Grotesk"
                    size="20px"
                    weight="600"
                    lineHeight="1.5"
                    example="Títulos de sección"
                    style={{ fontFamily: 'Space Grotesk', fontSize: '20px', fontWeight: 600, lineHeight: '1.5' }}
                  />
                  <TypographyExample
                    tag="H3"
                    font="Space Grotesk"
                    size="18px"
                    weight="600"
                    lineHeight="1.5"
                    example="Subtítulos importantes"
                    style={{ fontFamily: 'Space Grotesk', fontSize: '18px', fontWeight: 600, lineHeight: '1.5' }}
                  />
                  <TypographyExample
                    tag="Body Large"
                    font="Space Grotesk"
                    size="16px"
                    weight="400"
                    lineHeight="1.6"
                    example="Texto principal de la aplicación con buena legibilidad para párrafos largos y descripciones."
                    style={{ fontFamily: 'Space Grotesk', fontSize: '16px', fontWeight: 400, lineHeight: '1.6' }}
                  />
                  <TypographyExample
                    tag="Body Regular"
                    font="Space Grotesk"
                    size="14px"
                    weight="400"
                    lineHeight="1.5"
                    example="Texto secundario para descripciones y contenido de tarjetas."
                    style={{ fontFamily: 'Space Grotesk', fontSize: '14px', fontWeight: 400, lineHeight: '1.5' }}
                  />
                  <TypographyExample
                    tag="Caption"
                    font="Space Grotesk"
                    size="12px"
                    weight="400"
                    lineHeight="1.4"
                    example="Texto auxiliar, etiquetas y metadatos"
                    style={{ fontFamily: 'Space Grotesk', fontSize: '12px', fontWeight: 400, lineHeight: '1.4' }}
                  />
                </div>
              </div>
            </div>
          </section>

          {/* Iconography Section */}
          <section id="iconography" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <ImageIcon className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Iconografía</h2>
              </div>

              {/* Icon Library Strategy */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">Estrategia de Librerías</h3>
                <div className="grid md:grid-cols-2 gap-4 mb-6">
                  <div className="border-4 border-[#5865F2] rounded-lg p-6 bg-gradient-to-br from-[#EEF2FF] to-[#E0E7FF] shadow-[4px_4px_0px_0px_rgba(88,101,242,0.2)]">
                    <div className="flex items-center gap-2 mb-3">
                      <div className="w-8 h-8 bg-[#5865F2] rounded flex items-center justify-center">
                        <Check className="w-5 h-5 text-white" />
                      </div>
                      <h4 className="font-['Space_Grotesk'] font-bold text-base">Lucide Icons</h4>
                    </div>
                    <p className="text-sm text-gray-700 mb-2 font-['Space_Grotesk']"><strong>Librería Base Principal</strong></p>
                    <ul className="text-xs text-gray-600 font-['Space_Grotesk'] space-y-1">
                      <li>✓ 1,400+ iconos consistentes</li>
                      <li>✓ Estilo limpio y moderno</li>
                      <li>✓ Optimizados para pixel art</li>
                      <li>✓ Totalmente open source</li>
                      <li>✓ React-friendly</li>
                    </ul>
                  </div>
                  
                  <div className="border-4 border-[#8B5CF6] rounded-lg p-6 bg-gradient-to-br from-[#FAF5FF] to-[#F3E8FF]">
                    <div className="flex items-center gap-2 mb-3">
                      <div className="w-8 h-8 bg-[#8B5CF6] rounded flex items-center justify-center">
                        <Sparkles className="w-5 h-5 text-white" />
                      </div>
                      <h4 className="font-['Space_Grotesk'] font-bold text-base">Phosphor Icons</h4>
                    </div>
                    <p className="text-sm text-gray-700 mb-2 font-['Space_Grotesk']"><strong>Librería Complementaria</strong></p>
                    <ul className="text-xs text-gray-600 font-['Space_Grotesk'] space-y-1">
                      <li>✓ Para casos especiales</li>
                      <li>✓ Variantes filled/outline</li>
                      <li>✓ Iconos gamificación única</li>
                      <li>✓ 6 pesos de línea</li>
                    </ul>
                  </div>
                </div>
                
                <div className="bg-[#FEF3C7] border-2 border-[#F59E0B] rounded-lg p-4">
                  <p className="text-sm font-['Space_Grotesk'] text-gray-800">
                    <strong className="text-[#92400E]">Por qué no usar otras:</strong> Heroicons (limitado), Ionicons (estilo iOS inconsistente), Tabler (demasiado minimalista). Lucide + Phosphor cubren el 100% de necesidades con coherencia visual.
                  </p>
                </div>
              </div>

              {/* Pixel Art Adaptation */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Adaptación a Pixel Art</h3>
                <PixelIconDemo />
              </div>

              {/* Icon Sizes */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Sistema de Tamaños</h3>
                <IconSizeDemo />
              </div>

              {/* Technical Specs */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#22A39A]">Especificaciones Técnicas</h3>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB]">
                    <h4 className="font-['Space_Grotesk'] font-semibold text-sm mb-3">Tamaños Base</h4>
                    <div className="space-y-2 text-sm font-['Space_Grotesk']">
                      <div className="flex justify-between">
                        <span className="text-gray-600">SM:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">16px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">MD:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">24px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">LG:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">32px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">XL:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">48px</code>
                      </div>
                    </div>
                  </div>
                  
                  <div className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB]">
                    <h4 className="font-['Space_Grotesk'] font-semibold text-sm mb-3">Grosor de Línea</h4>
                    <div className="space-y-2 text-sm font-['Space_Grotesk']">
                      <div className="flex justify-between">
                        <span className="text-gray-600">16px icons:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">2px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">24px icons:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">2.5px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">32px icons:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">3px</code>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">48px icons:</span>
                        <code className="px-2 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">3.5px</code>
                      </div>
                    </div>
                  </div>
                  
                  <div className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB]">
                    <h4 className="font-['Space_Grotesk'] font-semibold text-sm mb-3">Versiones</h4>
                    <div className="space-y-2 text-sm font-['Space_Grotesk']">
                      <p className="text-gray-700"><strong>Outline:</strong> Para navegación y acciones generales</p>
                      <p className="text-gray-700"><strong>Filled:</strong> Para estados activos, gamificación, recompensas</p>
                    </div>
                  </div>
                  
                  <div className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB]">
                    <h4 className="font-['Space_Grotesk'] font-semibold text-sm mb-3">Padding Interno</h4>
                    <div className="space-y-2 text-sm font-['Space_Grotesk']">
                      <p className="text-gray-700">Mantener <code className="px-1 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">2px</code> de padding interno mínimo</p>
                      <p className="text-gray-700">Asegura que el icono no toque los bordes del contenedor</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Task Icons */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">📋 Iconos de Tareas</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Plus size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Añadir Tarea" token="Icon/Task/Add/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Check size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Completar" token="Icon/Task/Complete/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Clock size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="En Progreso" token="Icon/Task/Progress/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Repeat size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Repetitiva" token="Icon/Task/Recurring/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Flag size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Prioridad Alta" token="Icon/Task/Priority/24" library="Lucide" size={24} />
                  <IconShowcase icon={<FolderOpen size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Categoría" token="Icon/Task/Category/24" library="Lucide" size={24} />
                </div>
              </div>

              {/* Gamification Icons */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">⭐ Iconos de Gamificación</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Star size={24} strokeWidth={2.5} fill="currentColor" style={{ shapeRendering: 'crispEdges' }} />} name="Estrella" token="Icon/Gamification/Star/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Zap size={24} strokeWidth={2.5} fill="currentColor" style={{ shapeRendering: 'crispEdges' }} />} name="XP" token="Icon/Gamification/XP/24" library="Lucide" size={24} />
                  <IconShowcase icon={<TrendingUp size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Nivel" token="Icon/Gamification/Level/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Gift size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Cofre" token="Icon/Gamification/Chest/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Award size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Logro" token="Icon/Gamification/Achievement/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Flame size={24} strokeWidth={2.5} fill="currentColor" style={{ shapeRendering: 'crispEdges' }} />} name="Racha Diaria" token="Icon/Gamification/Streak/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Hexagon size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Constelación" token="Icon/Gamification/Constellation/24" library="Lucide" size={24} />
                </div>
              </div>

              {/* Navigation Icons */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#22A39A]">🌌 Iconos de Navegación</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <IconShowcase icon={<Home size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Home" token="Icon/Navigation/Home/24" library="Lucide" size={24} />
                  <IconShowcase icon={<User size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Perfil" token="Icon/Navigation/Profile/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Package size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Inventario" token="Icon/Navigation/Inventory/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Trophy size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Recompensas" token="Icon/Navigation/Rewards/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Settings size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Ajustes" token="Icon/Navigation/Settings/24" library="Lucide" size={24} />
                  <IconShowcase icon={<Bell size={24} strokeWidth={2.5} style={{ shapeRendering: 'crispEdges' }} />} name="Notificaciones" token="Icon/Navigation/Notifications/24" library="Lucide" size={24} />
                </div>
              </div>

              {/* Token Naming */}
              <div>
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Sistema de Tokens</h3>
                <div className="border-4 border-[#1E1E2E] rounded-lg p-6 bg-gradient-to-br from-[#F9FAFB] to-[#F3F4F6]">
                  <div className="mb-4">
                    <h4 className="font-['Space_Grotesk'] font-semibold mb-2">Estructura de Naming</h4>
                    <code className="block px-4 py-3 bg-[#1E1E2E] text-[#9FE8C5] rounded font-mono text-sm">
                      Icon/[Category]/[Name]/[Size]
                    </code>
                  </div>
                  
                  <div className="grid md:grid-cols-2 gap-4 text-sm font-['Space_Grotesk']">
                    <div>
                      <h5 className="font-semibold mb-2">Ejemplos:</h5>
                      <ul className="space-y-1 text-gray-700">
                        <li><code className="px-1 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">Icon/Task/Add/24</code></li>
                        <li><code className="px-1 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">Icon/Gamification/Star/32</code></li>
                        <li><code className="px-1 py-0.5 bg-white border border-gray-300 rounded font-mono text-xs">Icon/Navigation/Home/24</code></li>
                      </ul>
                    </div>
                    
                    <div>
                      <h5 className="font-semibold mb-2">Categorías:</h5>
                      <ul className="space-y-1 text-gray-700">
                        <li>• <strong>Task:</strong> Gestión de tareas</li>
                        <li>• <strong>Gamification:</strong> XP, logros, recompensas</li>
                        <li>• <strong>Navigation:</strong> Navegación principal</li>
                        <li>• <strong>Action:</strong> Acciones generales</li>
                        <li>• <strong>Status:</strong> Estados y notificaciones</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* Components Section */}
          <section id="components" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Layers className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Componentes</h2>
              </div>

              {/* Buttons */}
              <div className="mb-12">
                <h3 className="font-['Press_Start_2P'] text-sm mb-2 text-[#5865F2]">Botones</h3>
                <p className="text-gray-600 mb-6 font-['Space_Grotesk']">
                  Bordes sólidos de 4px, sombras estilo sprite (shadow dura), y estados interactivos claros.
                </p>
                <div className="space-y-6">
                  <div>
                    <h4 className="font-semibold mb-3 font-['Space_Grotesk']">Botón Primario</h4>
                    <ButtonShowcase variant="primary" label="Completar Tarea" />
                  </div>
                  <div>
                    <h4 className="font-semibold mb-3 font-['Space_Grotesk']">Botón Secundario</h4>
                    <ButtonShowcase variant="secondary" label="Ver Detalles" />
                  </div>
                  <div>
                    <h4 className="font-semibold mb-3 font-['Space_Grotesk']">Botón Recompensa</h4>
                    <ButtonShowcase variant="reward" label="Abrir Cofre" />
                  </div>
                </div>
              </div>

              {/* Task Card */}
              <div className="mb-12">
                <h3 className="font-['Press_Start_2P'] text-sm mb-2 text-[#5865F2]">Tarjeta de Tarea</h3>
                <p className="text-gray-600 mb-6 font-['Space_Grotesk']">
                  Incluye progreso visual, tags, y recompensa XP destacada.
                </p>
                <TaskCard />
              </div>

              {/* XP Bar */}
              <div className="mb-12">
                <h3 className="font-['Press_Start_2P'] text-sm mb-2 text-[#5865F2]">Barra de Progreso XP</h3>
                <p className="text-gray-600 mb-6 font-['Space_Grotesk']">
                  Gradiente animado con brillo y microanimaciones al ganar XP.
                </p>
                <XPBar />
              </div>

              {/* Badges */}
              <div className="mb-12">
                <h3 className="font-['Press_Start_2P'] text-sm mb-2 text-[#5865F2]">Sistema de Badges</h3>
                <p className="text-gray-600 mb-6 font-['Space_Grotesk']">
                  Badges circulares con borde grueso y glow effect para destacar.
                </p>
                <div className="border-4 border-[#1E1E2E] rounded-lg bg-gradient-to-br from-[#0F3547] to-[#1E4A63] p-6">
                  <BadgeShowcase />
                </div>
              </div>
            </div>
          </section>

          {/* Gamification Section */}
          <section id="gamification" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Sparkles className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Sistema de Gamificación</h2>
              </div>

              {/* Levels */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">Sistema de Niveles</h3>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="border-4 border-[#1E1E2E] rounded-lg p-4 bg-gradient-to-br from-[#F3F4F6] to-[#E5E7EB]">
                    <div className="font-['Press_Start_2P'] text-xs mb-2 text-[#6B7280]">Nivel 1-5</div>
                    <div className="font-['Space_Grotesk'] font-semibold text-sm">Explorador Novato</div>
                  </div>
                  <div className="border-4 border-[#3B82F6] rounded-lg p-4 bg-gradient-to-br from-[#DBEAFE] to-[#93C5FD] shadow-[0_0_8px_rgba(59,130,246,0.3)]">
                    <div className="font-['Press_Start_2P'] text-xs mb-2 text-[#1E40AF]">Nivel 6-15</div>
                    <div className="font-['Space_Grotesk'] font-semibold text-sm">Navegante Cósmico</div>
                  </div>
                  <div className="border-4 border-[#8B5CF6] rounded-lg p-4 bg-gradient-to-br from-[#F3E8FF] to-[#DDD6FE] shadow-[0_0_12px_rgba(139,92,246,0.4)]">
                    <div className="font-['Press_Start_2P'] text-xs mb-2 text-[#6B21A8]">Nivel 16-30</div>
                    <div className="font-['Space_Grotesk'] font-semibold text-sm">Guardián Galáctico</div>
                  </div>
                  <div className="border-4 border-[#F59E0B] rounded-lg p-4 bg-gradient-to-br from-[#FEF3C7] to-[#FDE68A] shadow-[0_0_16px_rgba(245,158,11,0.5)]">
                    <div className="font-['Press_Start_2P'] text-xs mb-2 text-[#92400E]">Nivel 31+</div>
                    <div className="font-['Space_Grotesk'] font-semibold text-sm">Maestro del Universo</div>
                  </div>
                </div>
              </div>

              {/* Rarity System */}
              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Rareza de Recompensas</h3>
                <div className="grid md:grid-cols-2 gap-6">
                  <AchievementCard rarity="common" />
                  <AchievementCard rarity="rare" />
                  <AchievementCard rarity="epic" />
                  <AchievementCard rarity="legendary" />
                </div>
              </div>

              {/* Animations */}
              <div>
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Animaciones Sugeridas</h3>
                <div className="space-y-3">
                  {[
                    { title: 'Completar Tarea', desc: 'Confetti de estrellas + pulse scale en la tarjeta + sonido "ding"' },
                    { title: 'Subir de Nivel', desc: 'Modal con explosión de partículas + texto animado tipo "LEVEL UP!"' },
                    { title: 'Desbloquear Logro', desc: 'Tarjeta gira en 3D + glow pulsante + shake suave' },
                    { title: 'Ganar XP', desc: 'Barra se llena con animación fluida + números que incrementan' },
                    { title: 'Abrir Cofre', desc: 'Cofre se abre con bounce + items aparecen con fade-in stagger' },
                  ].map((item, i) => (
                    <div key={i} className="border-2 border-[#E5E7EB] rounded-lg p-4 bg-[#F9FAFB]">
                      <div className="font-semibold text-sm mb-1 font-['Space_Grotesk']">{item.title}</div>
                      <div className="text-sm text-gray-600 font-['Space_Grotesk']">{item.desc}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>

          {/* Spacing Section */}
          <section id="spacing" className="scroll-mt-32">
            <div className="bg-white rounded-2xl border-4 border-[#1E1E2E] shadow-[8px_8px_0px_0px_rgba(30,30,46,1)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Grid3x3 className="w-8 h-8 text-[#5865F2]" />
                <h2 className="font-['Press_Start_2P'] text-xl">Sistema de Espaciado</h2>
              </div>

              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#5865F2]">Grid Base: 8pt</h3>
                <p className="text-gray-600 mb-4 font-['Space_Grotesk']">
                  Sistema de 8pt para coherencia visual. Todos los espacios y tamaños son múltiplos de 8.
                </p>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    { name: 'XXS', value: '4px', pixels: '0.5' },
                    { name: 'XS', value: '8px', pixels: '1' },
                    { name: 'SM', value: '16px', pixels: '2' },
                    { name: 'MD', value: '24px', pixels: '3' },
                    { name: 'LG', value: '32px', pixels: '4' },
                    { name: 'XL', value: '48px', pixels: '6' },
                    { name: '2XL', value: '64px', pixels: '8' },
                    { name: '3XL', value: '96px', pixels: '12' },
                  ].map((item) => (
                    <div key={item.name} className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB]">
                      <div className="font-['Press_Start_2P'] text-xs mb-2">{item.name}</div>
                      <div className="font-['Space_Grotesk'] text-sm font-semibold">{item.value}</div>
                      <div className="font-['Space_Grotesk'] text-xs text-gray-500">Spacing/{item.pixels}</div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Border Radius</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    { name: 'None', value: '0px', demo: 'rounded-none' },
                    { name: 'SM', value: '4px', demo: 'rounded' },
                    { name: 'MD', value: '8px', demo: 'rounded-lg' },
                    { name: 'LG', value: '16px', demo: 'rounded-2xl' },
                  ].map((item) => (
                    <div key={item.name} className="text-center">
                      <div className={`h-16 border-4 border-[#1E1E2E] bg-[#5865F2] ${item.demo} mb-2`} />
                      <div className="font-['Press_Start_2P'] text-xs mb-1">{item.name}</div>
                      <div className="font-['Space_Grotesk'] text-xs text-gray-600">{item.value}</div>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="font-['Press_Start_2P'] text-sm mb-4">Tamaños de Iconos</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    { name: 'SM', value: '16px', size: 'w-4 h-4' },
                    { name: 'MD', value: '24px', size: 'w-6 h-6' },
                    { name: 'LG', value: '32px', size: 'w-8 h-8' },
                    { name: 'XL', value: '48px', size: 'w-12 h-12' },
                  ].map((item) => (
                    <div key={item.name} className="border-2 border-[#1E1E2E] rounded-lg p-4 bg-[#F9FAFB] flex flex-col items-center gap-2">
                      <Sparkles className={`${item.size} text-[#5865F2]`} />
                      <div className="font-['Press_Start_2P'] text-xs">{item.name}</div>
                      <div className="font-['Space_Grotesk'] text-xs text-gray-600">{item.value}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>

          {/* Personality Section */}
          <section id="personality" className="scroll-mt-32">
            <div className="bg-gradient-to-br from-[#1E1E2E] to-[#2D2D3D] border-4 border-[#FFD700] rounded-2xl shadow-[8px_8px_0px_0px_rgba(255,215,0,0.3)] p-8">
              <div className="flex items-center gap-3 mb-6">
                <Heart className="w-8 h-8 text-[#FFD700]" />
                <h2 className="font-['Press_Start_2P'] text-xl text-white">Personalidad Visual</h2>
              </div>

              <div className="grid md:grid-cols-2 gap-8 mb-8">
                <div>
                  <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#9FE8C5]">5 Palabras Clave</h3>
                  <div className="space-y-2">
                    {['Nostálgico', 'Motivador', 'Espacial', 'Progresivo', 'Mágico'].map((word, i) => (
                      <div key={i} className="bg-[#0F3547] border-2 border-[#4ECDC4] rounded-lg p-3 text-white font-['Space_Grotesk']">
                        {word}
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#8B5CF6]">Mood Visual</h3>
                  <div className="bg-[#0F3547] border-2 border-[#8B5CF6] rounded-lg p-4 text-gray-300 font-['Space_Grotesk'] space-y-2 text-sm">
                    <p>✨ Sentimiento de logro constante</p>
                    <p>🌌 Exploración sin límites</p>
                    <p>🎮 Diversión retro moderna</p>
                    <p>🚀 Impulso hacia el progreso</p>
                    <p>⭐ Recompensas visuales satisfactorias</p>
                  </div>
                </div>
              </div>

              <div className="mb-8">
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#EF4444]">❌ Qué Evitar</h3>
                <div className="grid md:grid-cols-2 gap-3">
                  {[
                    'Bordes suaves sin personalidad',
                    'Sombras difuminadas (solo sombras duras)',
                    'Gradientes sutiles sin contraste',
                    'Tipografía sans-serif genérica para títulos',
                    'Animaciones lentas o flojas',
                    'Colores pasteles sin saturación',
                  ].map((item, i) => (
                    <div key={i} className="bg-[#450a0a] border-2 border-[#EF4444] rounded-lg p-3 text-[#FCA5A5] font-['Space_Grotesk'] text-sm">
                      {item}
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="font-['Press_Start_2P'] text-sm mb-4 text-[#4ECDC4]">💡 Coherencia Pixel Art</h3>
                <div className="bg-[#0F3547] border-2 border-[#4ECDC4] rounded-lg p-6 text-gray-300 font-['Space_Grotesk'] space-y-3">
                  <p className="text-sm">
                    <strong className="text-[#4ECDC4]">Bordes afilados:</strong> Usar border-radius mínimo o ninguno en elementos pixel art. Aplicar rounded solo en contenedores generales.
                  </p>
                  <p className="text-sm">
                    <strong className="text-[#4ECDC4]">Sombras duras:</strong> Sombras con offset definido (4px, 6px, 8px) sin blur. Estilo "sprite shadow".
                  </p>
                  <p className="text-sm">
                    <strong className="text-[#4ECDC4]">Contraste alto:</strong> Bordes oscuros (#1E1E2E) sobre fondos claros y viceversa para máxima legibilidad.
                  </p>
                  <p className="text-sm">
                    <strong className="text-[#4ECDC4]">Tipografía balanceada:</strong> Press Start 2P para impacto, Space Grotesk para legibilidad. Nunca mezclar en el mismo elemento.
                  </p>
                  <p className="text-sm">
                    <strong className="text-[#4ECDC4]">Animaciones rápidas:</strong> Transiciones de 100-200ms. Microanimaciones inmediatas. Sentir de juego retro responsivo.
                  </p>
                </div>
              </div>
            </div>
          </section>

          {/* Footer */}
          <footer className="text-center py-8">
            <div className="inline-block bg-[#1E1E2E] border-4 border-[#5865F2] rounded-lg px-6 py-4 shadow-[4px_4px_0px_0px_rgba(88,101,242,0.4)]">
              <p className="font-['Press_Start_2P'] text-xs text-white mb-2">ASTRAIS DESIGN SYSTEM</p>
              <p className="font-['Space_Grotesk'] text-sm text-gray-400">Creado para gamificar tu vida con estilo 🚀✨</p>
            </div>
          </footer>

        </div>
      </main>
    </div>
  );
}
export type ShopCategory = 'Todos' | 'Eventos' | 'Temas' | 'Mascotas' | 'Especiales'
export type ShopSlot = 'theme' | 'companion' | 'aura'
export type ShopRarity = 'Comun' | 'Raro' | 'Epico' | 'Legendario'

export interface ShopItem {
  id: string
  name: string
  category: Exclude<ShopCategory, 'Todos'>
  slot: ShopSlot
  price: number
  rarity: ShopRarity
  badge?: string
  shortDescription: string
  detail: string
  perk: string
  accentFrom: string
  accentTo: string
}

export const SHOP_CATEGORIES: ShopCategory[] = ['Todos', 'Eventos', 'Temas', 'Mascotas', 'Especiales']

export const SHOP_ITEMS: ShopItem[] = [
  {
    id: 'theme-aurora',
    name: 'Aurora Pulse',
    category: 'Temas',
    slot: 'theme',
    price: 120,
    rarity: 'Raro',
    badge: 'NUEVO',
    shortDescription: 'Interfaz celeste con halos reactivos.',
    detail: 'Una piel espacial con azules electricos y brillos suaves para transformar toda la cabina.',
    perk: 'Tema luminoso con contraste limpio para dashboards nocturnos.',
    accentFrom: '#38bdf8',
    accentTo: '#8b5cf6',
  },
  {
    id: 'theme-solaris',
    name: 'Solaris Grid',
    category: 'Temas',
    slot: 'theme',
    price: 160,
    rarity: 'Epico',
    badge: 'HOT',
    shortDescription: 'Calor estelar con grid retro y neones.',
    detail: 'Fusiona naranjas solares y magentas para una interfaz mas arcade y cargada de energia.',
    perk: 'Tema premium para sesiones de foco con presencia fuerte.',
    accentFrom: '#f97316',
    accentTo: '#ec4899',
  },
  {
    id: 'pet-nova',
    name: 'Nova Buddy',
    category: 'Mascotas',
    slot: 'companion',
    price: 95,
    rarity: 'Comun',
    shortDescription: 'Mascota exploradora para acompanarte en tus retos.',
    detail: 'Un companero orbital compacto con animacion sugerida y look amable para el panel principal.',
    perk: 'Companion ligero ideal para empezar tu coleccion.',
    accentFrom: '#22c55e',
    accentTo: '#06b6d4',
  },
  {
    id: 'pet-cosmo',
    name: 'Cosmo Lynx',
    category: 'Mascotas',
    slot: 'companion',
    price: 180,
    rarity: 'Legendario',
    badge: 'LIMIT',
    shortDescription: 'Felino astral de rango alto.',
    detail: 'Una criatura premium con presencia elegante, ojos intensos y silueta de guardian espacial.',
    perk: 'Companion legendario para vitrinas avanzadas.',
    accentFrom: '#f59e0b',
    accentTo: '#ef4444',
  },
  {
    id: 'event-comet',
    name: 'Comet Trail',
    category: 'Eventos',
    slot: 'aura',
    price: 110,
    rarity: 'Raro',
    shortDescription: 'Efecto fugaz con estela para eventos especiales.',
    detail: 'Una cola cometaria para acentuar cabeceras, premios y zonas de accion en fechas de evento.',
    perk: 'Aura dinamica con sensacion de velocidad.',
    accentFrom: '#a78bfa',
    accentTo: '#38bdf8',
  },
  {
    id: 'event-lantern',
    name: 'Nebula Lanterns',
    category: 'Eventos',
    slot: 'aura',
    price: 140,
    rarity: 'Epico',
    shortDescription: 'Faroles cosmicos suspendidos en el fondo.',
    detail: 'Decora la experiencia con destellos suaves y pequeñas luces volumetricas flotando sobre el panel.',
    perk: 'Aura celebracion con brillo ambiental refinado.',
    accentFrom: '#fb7185',
    accentTo: '#8b5cf6',
  },
  {
    id: 'special-archive',
    name: 'Archive Frame',
    category: 'Especiales',
    slot: 'aura',
    price: 130,
    rarity: 'Raro',
    shortDescription: 'Marco tactico para vitrinas y perfiles.',
    detail: 'Reforzado con vidrio sintetico y lineas holograficas para escenas de logro y coleccion.',
    perk: 'Aura tecnica con lectura muy clara.',
    accentFrom: '#14b8a6',
    accentTo: '#3b82f6',
  },
  {
    id: 'special-vault',
    name: 'Vault Signature',
    category: 'Especiales',
    slot: 'aura',
    price: 210,
    rarity: 'Legendario',
    badge: 'ELITE',
    shortDescription: 'Firma visual para cuentas premium.',
    detail: 'Una firma de alto rango con brillos metalicos, capas de cristal y contraste muy marcado.',
    perk: 'Aura insignia para perfiles de coleccion maxima.',
    accentFrom: '#f59e0b',
    accentTo: '#8b5cf6',
  },
]

export function getItemsForCategory(category: ShopCategory) {
  if (category === 'Todos') {
    return SHOP_ITEMS
  }

  return SHOP_ITEMS.filter((item) => item.category === category)
}

export function getRarityClasses(rarity: ShopRarity) {
  if (rarity === 'Legendario') {
    return 'border-[#f59e0b]/45 bg-[#f59e0b]/10 text-[#f8d089]'
  }

  if (rarity === 'Epico') {
    return 'border-[#ec4899]/35 bg-[#ec4899]/10 text-[#f4b4d8]'
  }

  if (rarity === 'Raro') {
    return 'border-accent-mint-300/35 bg-accent-mint-300/10 text-accent-mint-300'
  }

  return 'border-white/15 bg-white/8 text-slate-200'
}

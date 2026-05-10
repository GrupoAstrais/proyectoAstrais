import React from 'react'
import { NavLink } from 'react-router'
import Navbar from '../../components/layout/Navbar'
import logo from '../../assets/logo_w.svg'
import {
  buyStoreItem,
  equipStoreItem,
  getStoreItems,
  getUserData,
  resolveStoreAssetUrl,
  type StoreItemResponse,
} from '../../data/Api'
import { applyThemeColors, parseThemeConfig } from '../../styles/theme'
import AstraisMascot from '../../components/ui/AstraisMascot'
import { useVisualPreferences } from '../../context/VisualPreferencesContext'

const PAGE_SIZE = 4

type ShopCategory = 'Todos' | 'Eventos' | 'Temas' | 'Mascotas' | 'Especiales'
type ShopSlot = 'theme' | 'companion' | 'aura'
type ShopRarity = 'Comun' | 'Raro' | 'Epico' | 'Legendario'

interface ShopDisplayItem extends StoreItemResponse {
  category: Exclude<ShopCategory, 'Todos'>
  slot: ShopSlot
  rarityLabel: ShopRarity
  shortDescription: string
  detail: string
  perk: string
  accentFrom: string
  accentTo: string
  assetUrl: string | null
}

const SHOP_CATEGORIES: ShopCategory[] = ['Todos', 'Eventos', 'Temas', 'Mascotas', 'Especiales']

function normalizeRarity(rarity?: string): ShopRarity {
  switch (rarity?.toUpperCase()) {
    case 'RARO':
    case 'RARE':
      return 'Raro'
    case 'EPICO':
    case 'EPIC':
      return 'Epico'
    case 'LEGENDARIO':
    case 'LEGENDARY':
      return 'Legendario'
    default:
      return 'Comun'
  }
}

function getRarityClasses(rarity: ShopRarity) {
  if (rarity === 'Legendario') {
    return 'border-[color-mix(in_srgb,var(--astrais-rarity-legendary)_45%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-legendary)_10%,transparent)] text-[var(--astrais-rarity-legendary)]'
  }

  if (rarity === 'Epico') {
    return 'border-[color-mix(in_srgb,var(--astrais-rarity-epic)_35%,transparent)] bg-[color-mix(in_srgb,var(--astrais-rarity-epic)_10%,transparent)] text-[var(--astrais-rarity-epic)]'
  }

  if (rarity === 'Raro') {
    return 'border-accent-mint-300/35 bg-accent-mint-300/10 text-accent-mint-300'
  }

  return 'border-white/15 bg-white/8 text-slate-200'
}

function getItemCategory(item: StoreItemResponse): Exclude<ShopCategory, 'Todos'> {
  if (item.type === 'APP_THEME') {
    return 'Temas'
  }

  if (item.type === 'PET') {
    return 'Mascotas'
  }

  if (item.coleccion.toLowerCase().includes('event')) {
    return 'Eventos'
  }

  return 'Especiales'
}

function getItemSlot(item: StoreItemResponse): ShopSlot {
  if (item.type === 'APP_THEME') {
    return 'theme'
  }

  if (item.type === 'PET') {
    return 'companion'
  }

  return 'aura'
}

function getItemAccent(item: StoreItemResponse) {
  if (item.type === 'APP_THEME') {
    const theme = parseThemeConfig(item.theme)
    return { accentFrom: theme.primary, accentTo: theme.secondary }
  }

  if (item.type === 'PET') {
    return { accentFrom: 'var(--astrais-tertiary)', accentTo: 'var(--astrais-secondary)' }
  }

  return { accentFrom: 'var(--astrais-primary)', accentTo: 'var(--astrais-error)' }
}

function toDisplayItem(item: StoreItemResponse): ShopDisplayItem {
  const { accentFrom, accentTo } = getItemAccent(item)

  return {
    ...item,
    category: getItemCategory(item),
    slot: getItemSlot(item),
    rarityLabel: normalizeRarity(item.rarity),
    shortDescription: item.desc,
    detail: item.desc,
    perk: item.type === 'APP_THEME'
      ? 'Tema visual equipado desde tu cuenta para sincronizar colores entre web y app.'
      : `Cosmetico de la coleccion ${item.coleccion || 'DEFAULT'}.`,
    accentFrom,
    accentTo,
    assetUrl: resolveStoreAssetUrl(item.type, item.assetRef),
  }
}

function getSoftGradient(from: string, to: string, fromWeight = '20%', toWeight = '14%') {
  return `linear-gradient(145deg, color-mix(in srgb, ${from} ${fromWeight}, transparent), color-mix(in srgb, ${to} ${toWeight}, transparent))`
}

function ShopItemVisual({ item, className = '' }: { item: ShopDisplayItem; className?: string }) {
  if (item.type === 'APP_THEME') {
    const theme = parseThemeConfig(item.theme)

    return (
      <div
        className={`grid place-items-center rounded-2xl border border-white/12 p-1 ${className}`}
        style={{
          background: `linear-gradient(145deg, ${theme.primary}, ${theme.secondary}, ${theme.tertiary})`,
        }}
      >
        <div className="grid h-full w-full grid-cols-2 gap-1 rounded-xl bg-black/18 p-1">
          {[theme.primary, theme.secondary, theme.tertiary, theme.backgroundAlt].map((color) => (
            <span key={color} className="rounded-lg border border-white/15" style={{ backgroundColor: color }} />
          ))}
        </div>
      </div>
    )
  }

  if (item.type === 'PET') {
    return (
      <AstraisMascot
        assetUrl={item.assetUrl}
        fallback="primary"
        alt={item.name}
        className={`object-contain ${className}`}
      />
    )
  }

  if (item.assetUrl) {
    return <img src={item.assetUrl} alt={item.name} className={`object-contain ${className}`} />
  }

  return <img src={logo} alt="Astrais logo" className={`object-contain ${className}`} />
}

export default function Shop() {
  const { refreshVisualPreferences } = useVisualPreferences()
  const [items, setItems] = React.useState<ShopDisplayItem[]>([])
  const [availableBalance, setAvailableBalance] = React.useState(0)
  const [activeCategory, setActiveCategory] = React.useState<ShopCategory>('Todos')
  const [currentPage, setCurrentPage] = React.useState(0)
  const [selectedItemId, setSelectedItemId] = React.useState<number | null>(null)
  const [loading, setLoading] = React.useState(true)
  const [error, setError] = React.useState<string | null>(null)
  const [mutationItemId, setMutationItemId] = React.useState<number | null>(null)

  const loadShopData = React.useCallback(async () => {
    const [userData, storeItems] = await Promise.all([getUserData(), getStoreItems()])

    applyThemeColors(userData.themeColors, storeItems)
    setAvailableBalance(userData.ludiones)
    setItems(storeItems.map(toDisplayItem))
  }, [])

  React.useEffect(() => {
    const loadInitialShopData = async () => {
      try {
        setLoading(true)
        setError(null)
        await loadShopData()
      } catch {
        setError('No se pudo conectar la tienda con el servidor.')
      } finally {
        setLoading(false)
      }
    }

    void loadInitialShopData()
  }, [loadShopData])

  const filteredItems = React.useMemo(() => {
    if (activeCategory === 'Todos') {
      return items
    }

    return items.filter((item) => item.category === activeCategory)
  }, [activeCategory, items])

  const pageCount = Math.max(1, Math.ceil(filteredItems.length / PAGE_SIZE))
  const visibleItems = React.useMemo(
    () => filteredItems.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE),
    [currentPage, filteredItems],
  )

  const selectedItem =
    filteredItems.find((item) => item.id === selectedItemId) ??
    visibleItems[0] ??
    filteredItems[0] ??
    null

  const spentLudions = React.useMemo(
    () => items.reduce((total, item) => total + (item.owned ? item.price : 0), 0),
    [items],
  )
  const ownedCount = items.filter((item) => item.owned).length
  const isOwned = selectedItem?.owned ?? false
  const isEquipped = selectedItem?.equipped ?? false
  const isMutatingSelected = selectedItem ? mutationItemId === selectedItem.id : false

  const equippedTheme = items.find((item) => item.slot === 'theme' && item.equipped)
  const equippedCompanion = items.find((item) => item.slot === 'companion' && item.equipped)
  const equippedAura = items.find((item) => item.slot === 'aura' && item.equipped)

  React.useEffect(() => {
    setCurrentPage(0)
  }, [activeCategory])

  React.useEffect(() => {
    if (currentPage > pageCount - 1) {
      setCurrentPage(pageCount - 1)
    }
  }, [currentPage, pageCount])

  React.useEffect(() => {
    if (!visibleItems.length) {
      setSelectedItemId(null)
      return
    }

    const selectedIsVisible = visibleItems.some((item) => item.id === selectedItemId)

    if (!selectedIsVisible) {
      setSelectedItemId(visibleItems[0].id)
    }
  }, [selectedItemId, visibleItems])

  const refreshAfterMutation = async () => {
    await loadShopData()
    await refreshVisualPreferences()
    setError(null)
  }

  const handleBuySelected = async () => {
    if (!selectedItem || isOwned || availableBalance < selectedItem.price || mutationItemId !== null) {
      return
    }

    try {
      setMutationItemId(selectedItem.id)
      await buyStoreItem(selectedItem.id)
      await refreshAfterMutation()
    } catch {
      setError('No se pudo completar la compra.')
    } finally {
      setMutationItemId(null)
    }
  }

  const handleEquipSelected = async () => {
    if (!selectedItem || !isOwned || isEquipped || mutationItemId !== null) {
      return
    }

    try {
      setMutationItemId(selectedItem.id)
      await equipStoreItem(selectedItem.id)

      if (selectedItem.type === 'APP_THEME') {
        applyThemeColors(selectedItem.theme)
      }

      await refreshAfterMutation()
    } catch {
      setError('No se pudo equipar el cosmetico.')
    } finally {
      setMutationItemId(null)
    }
  }

  return (
    <div
      className="relative h-screen overflow-hidden font-['Space_Grotesk'] text-(--astrais-text)"
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,var(--astrais-primary),transparent_34%),radial-gradient(circle_at_bottom_right,var(--astrais-secondary),transparent_38%),radial-gradient(circle_at_center,var(--astrais-tertiary),transparent_45%)] opacity-20" />
      <div
        className="pointer-events-none absolute inset-0 opacity-55"
        style={{ backgroundColor: 'color-mix(in srgb, var(--astrais-background) 58%, black 42%)' }}
      />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-25" />

      <div className="relative z-10 flex h-full min-h-0 flex-col">
        <Navbar />

        <main className="flex min-h-0 flex-1 px-3 pb-3 pt-1 md:px-4 md:pb-4 xl:px-6 xl:pb-5">
          <section className="mx-auto hidden h-full w-full gap-3 lg:grid lg:grid-cols-12 min-[1400px]:grid-cols-[15.75rem_minmax(0,1.24fr)_20.25rem] min-[1400px]:gap-4">
            <aside className="panel-glow relative col-span-2 grid min-h-0 grid-rows-[auto_auto_auto_minmax(0,1fr)] overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(170deg,color-mix(in_srgb,var(--astrais-background)_90%,var(--astrais-background-alt)_10%),color-mix(in_srgb,var(--astrais-background-alt)_82%,var(--astrais-secondary)_18%))] p-3.5 shadow-[0_20px_56px_color-mix(in_srgb,var(--astrais-background)_50%,transparent)] min-[1400px]:p-5">
              <div className="pointer-events-none absolute -left-12 top-3 h-36 w-36 rounded-full bg-secondary-500/16 blur-3xl" />
              <div className="relative z-10 flex items-start justify-between gap-3">
                <div>
                  <p className="text-[0.64rem] uppercase tracking-[0.28em] text-accent-beige-300">Astrais Store</p>
                  <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(0.92rem,1.4vw,1.2rem)] leading-tight text-white">
                    Tienda orbital
                  </h1>
                  <p className="mt-2 text-left text-[0.8rem] leading-5 text-slate-300 xl:text-[0.86rem] xl:leading-6">
                    Explora, compra y equipa cosmeticos desde tu cuenta.
                  </p>
                </div>
                <img src={logo} alt="Astrais logo" className="h-9 w-9 opacity-85 min-[1400px]:h-12 min-[1400px]:w-12" />
              </div>

              <div className="relative z-10 mt-4 grid grid-cols-2 gap-2.5 min-[1400px]:gap-3">
                <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                  <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Saldo</p>
                  <p className="mt-2 text-[1rem] font-semibold text-accent-mint-300 xl:text-[1.12rem]">{availableBalance}</p>
                </div>
                <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                  <p className="text-[0.56rem] uppercase tracking-[0.18em] text-slate-400">Gastado</p>
                  <p className="mt-2 text-[1rem] font-semibold text-[var(--astrais-reward)] xl:text-[1.12rem]">{spentLudions}</p>
                </div>
              </div>

              <div className="relative z-10 mt-4 rounded-3xl border border-white/10 bg-black/18 p-3">
                <div className="flex items-center justify-between">
                  <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">Conexion backend</p>
                  <span className={`rounded-full border px-2 py-1 text-[0.54rem] uppercase tracking-[0.14em] ${
                    error
                      ? 'border-(--astrais-error)/35 bg-(--astrais-error)/10 text-(--astrais-error)'
                      : 'border-accent-mint-300/25 bg-accent-mint-300/8 text-accent-mint-300'
                  }`}>
                    {error ? 'Error' : 'Activa'}
                  </span>
                </div>
                <div className="mt-2 space-y-2 text-[0.76rem] text-slate-300 xl:text-[0.82rem]">
                  <div className="flex items-center justify-between">
                    <span>Objetos cargados</span>
                    <span className="font-semibold text-white">{items.length}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Objetos comprados</span>
                    <span className="font-semibold text-white">{ownedCount}</span>
                  </div>
                </div>
              </div>

              <div className="relative z-10 mt-4 min-h-0 rounded-3xl border border-white/10 bg-black/18 p-3">
                <p className="text-center text-xs uppercase tracking-[0.18em] text-slate-400">Categorias</p>
                <div className="tabs-scroll mt-2 flex-row pb-1 min-[1200px]:flex-col">
                  {SHOP_CATEGORIES.map((category) => (
                    <button
                      key={category}
                      type="button"
                      onClick={() => setActiveCategory(category)}
                      className={`min-h-10 shrink-0 whitespace-nowrap rounded-2xl border px-3 py-2.5 text-center text-[0.76rem] font-semibold min-[1200px]:w-full min-[1200px]:text-left xl:text-[0.82rem] ${
                        activeCategory === category
                          ? 'border-0 [background:var(--astrais-cta-bg)] text-white shadow-[0_10px_24px_color-mix(in_srgb,var(--astrais-rarity-epic)_24%,transparent)]'
                          : 'border-white/12 bg-white/6 text-slate-200 hover:bg-white/10'
                      }`}
                    >
                      {category}
                    </button>
                  ))}
                </div>
              </div>
            </aside>

            <section className="panel-glow relative col-span-7 grid min-h-0 grid-rows-[auto_minmax(0,1fr)] overflow-hidden rounded-[28px] border border-white/15 bg-[linear-gradient(160deg,color-mix(in_srgb,var(--astrais-background)_88%,var(--astrais-background-alt)_12%),color-mix(in_srgb,var(--astrais-primary)_42%,transparent),color-mix(in_srgb,var(--astrais-background-alt)_82%,var(--astrais-secondary)_18%))] p-3.5 shadow-[0_20px_58px_color-mix(in_srgb,var(--astrais-background)_52%,transparent)] min-[1400px]:p-5">
              <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,color-mix(in_srgb,var(--astrais-text)_10%,transparent),transparent_25%)]" />
              <header className="relative z-10 flex items-start justify-between gap-3 min-[1400px]:gap-4">
                <div>
                  <p className="text-[0.64rem] uppercase tracking-[0.28em] text-accent-beige-300">Catalogo activo</p>
                  <h2 className="mt-3 font-['Press_Start_2P'] text-[clamp(0.92rem,1.4vw,1.15rem)] text-white">
                    Seleccion premium
                  </h2>
                  {error ? <p className="mt-2 text-[0.76rem] text-(--astrais-error)">{error}</p> : null}
                </div>

                <div className="flex items-center gap-2.5 min-[1400px]:gap-3">
                  <button
                    type="button"
                    onClick={() => setCurrentPage((page) => Math.max(0, page - 1))}
                    disabled={currentPage === 0 || loading}
                    className={`rounded-full border px-3 py-2 text-[0.74rem] font-semibold transition ${
                      currentPage === 0 || loading
                        ? 'cursor-not-allowed border-white/10 bg-white/6 text-slate-500'
                        : 'border-white/15 bg-white/8 text-white hover:bg-white/12'
                    }`}
                  >
                    Anterior
                  </button>
                  <span className="rounded-full border border-white/12 bg-black/18 px-3 py-2 text-[0.72rem] uppercase tracking-[0.18em] text-slate-300">
                    Pagina {currentPage + 1}/{pageCount}
                  </span>
                  <button
                    type="button"
                    onClick={() => setCurrentPage((page) => Math.min(pageCount - 1, page + 1))}
                    disabled={currentPage >= pageCount - 1 || loading}
                    className={`rounded-full border px-3 py-2 text-[0.74rem] font-semibold transition ${
                      currentPage >= pageCount - 1 || loading
                        ? 'cursor-not-allowed border-white/10 bg-white/6 text-slate-500'
                        : 'border-white/15 bg-white/8 text-white hover:bg-white/12'
                    }`}
                  >
                    Siguiente
                  </button>
                </div>
              </header>

              <div className="relative z-10 mt-4 grid min-h-0 grid-cols-2 grid-rows-2 gap-3 min-[1400px]:gap-4">
                {loading ? (
                  <div className="col-span-2 row-span-2 grid place-items-center rounded-3xl border border-white/12 bg-black/18 text-sm text-slate-300">
                    Cargando tienda...
                  </div>
                ) : visibleItems.length === 0 ? (
                  <div className="col-span-2 row-span-2 grid place-items-center rounded-3xl border border-white/12 bg-black/18 p-8 text-center text-sm leading-6 text-slate-300">
                    No hay cosmeticos disponibles en esta categoria.
                  </div>
                ) : visibleItems.map((item) => {
                  return (
                    <article
                      key={item.id}
                      onClick={() => setSelectedItemId(item.id)}
                      className={`catalog-card flex min-h-0 cursor-pointer flex-col rounded-3xl border p-3 transition min-[1400px]:p-4 ${
                        selectedItem?.id === item.id
                          ? 'border-accent-beige-300/40 bg-[linear-gradient(160deg,color-mix(in_srgb,var(--astrais-text)_12%,transparent),color-mix(in_srgb,var(--astrais-primary)_16%,transparent))] shadow-[0_16px_38px_color-mix(in_srgb,var(--astrais-background)_42%,transparent)]'
                          : 'border-white/12 bg-[color-mix(in_srgb,var(--astrais-background)_74%,transparent)] hover:border-white/18 hover:bg-white/8'
                      }`}
                    >
                      <div
                        className="relative overflow-hidden h-full rounded-[20px] border border-white/10 px-3 py-3"
                        style={{ background: getSoftGradient(item.accentFrom, item.accentTo) }}
                      >
                        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,color-mix(in_srgb,var(--astrais-text)_16%,transparent),transparent_34%)]" />
                        <div className="relative flex items-start justify-between gap-3">
                          <div>
                            <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-300">{item.category}</p>
                            <h3 className="mt-2 truncate text-[0.82rem] font-semibold text-white min-[1400px]:text-[0.94rem]">{item.name}</h3>
                          </div>
                          <span className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.16em] ${getRarityClasses(item.rarityLabel)}`}>
                            {item.rarityLabel}
                          </span>
                        </div>

                        <div className="relative flex h-5/6 place-items-center justify-center gap-3">
                          <ShopItemVisual item={item} className="h-5/6 w-5/6 opacity-85" />
                          <p className="max-w-48 text-right text-[0.66rem] leading-5 text-slate-200 min-[1400px]:text-[0.76rem]">
                            {item.shortDescription}
                          </p>
                        </div>
                      </div>

                      <div className="mt-3 flex items-center justify-between">
                        <span className="text-[0.8rem] font-semibold text-(--astrais-reward) min-[1400px]:text-[0.9rem]">{item.price} L</span>
                        <span className="rounded-full border border-white/12 bg-white/6 px-2 py-1 text-[0.54rem] uppercase tracking-[0.14em] text-slate-300">
                          {item.equipped ? 'Equipado' : item.owned ? 'Comprado' : 'Disponible'}
                        </span>
                      </div>
                    </article>
                  )
                })}
              </div>
            </section>

            {selectedItem ? (
              <aside className="panel-glow relative col-span-3 grid min-h-0 grid-rows-[minmax(0,1fr)_auto] overflow-y-scroll rounded-[26px] border border-white/15 bg-[linear-gradient(170deg,color-mix(in_srgb,var(--astrais-background)_90%,var(--astrais-background-alt)_10%),color-mix(in_srgb,var(--astrais-background-alt)_82%,var(--astrais-secondary)_18%))] p-3.5 shadow-[0_20px_56px_color-mix(in_srgb,var(--astrais-background)_50%,transparent)] min-[1400px]:p-5">
                <div className="pointer-events-none absolute -right-10 top-6 h-36 w-36 rounded-full bg-secondary-500/18 blur-3xl" />
                <div
                  className="relative min-h-0 overflow-hidden rounded-3xl border border-white/10 p-4"
                  style={{ background: getSoftGradient(selectedItem.accentFrom, selectedItem.accentTo, '19%', '10%') }}
                >
                  <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,color-mix(in_srgb,var(--astrais-text)_16%,transparent),transparent_34%)]" />

                  <div className="relative z-10 grid h-full min-h-0 grid-rows-[auto_auto_auto_auto_auto_auto_auto]">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-300">{selectedItem.category}</p>
                        <h2 className="mt-2 font-['Press_Start_2P'] text-[0.8rem] leading-snug text-white min-[1400px]:text-[0.96rem]">
                          {selectedItem.name}
                        </h2>
                      </div>
                      <span className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.16em] ${getRarityClasses(selectedItem.rarityLabel)}`}>
                        {selectedItem.rarityLabel}
                      </span>
                    </div>

                    <p className="mt-4 text-[0.74rem] leading-5 text-slate-200 min-[1400px]:text-[0.86rem] min-[1400px]:leading-6">{selectedItem.detail}</p>

                    <div className="mt-4 flex items-center justify-center rounded-[22px] border border-white/10 bg-black/18 p-4">
                      <ShopItemVisual item={selectedItem} className="h-24 w-24 opacity-95 min-[1400px]:h-30 min-[1400px]:w-30" />
                    </div>

                    <div className="mt-4 grid grid-cols-2 gap-3">
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.56rem] uppercase tracking-[0.16em] text-slate-400">Precio</p>
                        <p className="mt-2 text-[0.86rem] font-semibold text-[var(--astrais-reward)] min-[1400px]:text-[1.04rem]">{selectedItem.price} L</p>
                      </div>
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.56rem] uppercase tracking-[0.16em] text-slate-400">Slot</p>
                        <p className="mt-4 text-[0.7rem] font-semibold uppercase tracking-[0.14em] text-white min-[1400px]:text-[9px]">
                          {selectedItem.slot}
                        </p>
                      </div>
                    </div>

                    <div className="mt-4 rounded-[22px] border border-white/10 bg-black/20 px-4 py-3">
                      <p className="text-[0.56rem] uppercase tracking-[0.16em] text-slate-400">Perk visual</p>
                      <p className="mt-2 text-[0.72rem] leading-5 text-slate-200 min-[1400px]:text-[0.82rem] min-[1400px]:leading-6">{selectedItem.perk}</p>
                    </div>

                    <div className="mt-4 grid grid-cols-3 gap-3">
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-400">Tema</p>
                        <p className="mt-2 text-[0.64rem] leading-5 text-white min-[1400px]:text-[0.74rem]">{equippedTheme?.name ?? 'Ninguno'}</p>
                      </div>
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-400">Mascota</p>
                        <p className="mt-2 text-[0.64rem] leading-5 text-white min-[1400px]:text-[0.74rem]">{equippedCompanion?.name ?? 'Ninguna'}</p>
                      </div>
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.54rem] uppercase tracking-[0.16em] text-slate-400">Aura</p>
                        <p className="mt-2 text-[0.64rem] leading-5 text-white min-[1400px]:text-[0.74rem]">{equippedAura?.name ?? 'Ninguna'}</p>
                      </div>
                    </div>

                    <div className="mt-4 flex items-center justify-between gap-3">
                      <button
                        type="button"
                        onClick={handleBuySelected}
                        disabled={isOwned || availableBalance < selectedItem.price || isMutatingSelected}
                        className={`rounded-2xl px-4 py-3 text-[0.72rem] font-semibold transition min-[1400px]:text-[0.82rem] ${
                          isOwned
                            ? 'cursor-default border border-accent-mint-300/25 bg-accent-mint-300/12 text-accent-mint-300'
                            : availableBalance < selectedItem.price || isMutatingSelected
                              ? 'cursor-not-allowed border border-white/10 bg-white/8 text-slate-400'
                              : 'border border-transparent [background:var(--astrais-cta-bg)] text-white shadow-[0_14px_28px_color-mix(in_srgb,var(--astrais-rarity-epic)_24%,transparent)] hover:-translate-y-0.5'
                        }`}
                      >
                        {isMutatingSelected ? 'Procesando...' : isOwned ? 'Ya comprado' : 'Comprar ahora'}
                      </button>

                      <button
                        type="button"
                        onClick={handleEquipSelected}
                        disabled={!isOwned || isEquipped || isMutatingSelected}
                        className={`rounded-2xl px-4 py-3 text-[0.72rem] font-semibold transition min-[1400px]:text-[0.82rem] ${
                          !isOwned || isMutatingSelected
                            ? 'cursor-not-allowed border border-white/10 bg-white/8 text-slate-400'
                            : isEquipped
                              ? 'cursor-default border border-accent-mint-300/25 bg-accent-mint-300/12 text-accent-mint-300'
                              : 'border border-white/15 bg-white/8 text-white hover:bg-white/12'
                        }`}
                      >
                        {isEquipped ? 'Equipado' : 'Equipar'}
                      </button>
                    </div>
                  </div>
                </div>

                <div className="mt-4 flex flex-col items-center justify-between gap-3">
                  <div className="rounded-2xl border border-white/10 bg-black/18 px-4 py-3 text-[0.72rem] leading-5 text-slate-300 min-[1400px]:text-[0.8rem]">
                    El saldo y el inventario se actualizan desde el backend y la base de datos.
                  </div>
                  <NavLink
                    to="/games"
                    className="rounded-2xl border border-white/15 bg-white/8 px-4 py-3 text-[0.72rem] font-semibold text-white transition hover:bg-white/12 min-[1400px]:text-[0.82rem]"
                  >
                    Ir al arcade
                  </NavLink>
                </div>
              </aside>
            ) : null}
          </section>

          <section className="mx-auto flex h-full max-w-md items-center justify-center lg:hidden">
            <article className="rounded-[28px] border border-white/15 bg-[color-mix(in_srgb,var(--astrais-background)_84%,transparent)] p-6 text-center shadow-[0_24px_60px_color-mix(in_srgb,var(--astrais-background)_50%,transparent)] backdrop-blur-sm">
              <p className="text-[0.72rem] uppercase tracking-[0.28em] text-accent-beige-300">Astrais Store</p>
              <h1 className="mt-4 font-['Press_Start_2P'] text-lg text-white">Vista de escritorio</h1>
              <p className="mt-4 text-sm leading-6 text-slate-300">
                Esta tienda esta optimizada para escritorios medianos y grandes. Saldo disponible: {availableBalance} ludiones.
              </p>
            </article>
          </section>
        </main>
      </div>

      <style>{`
        .scanlines {
          background-image: repeating-linear-gradient(
            180deg,
            color-mix(in srgb, var(--astrais-text) 6%, transparent) 0,
            color-mix(in srgb, var(--astrais-text) 6%, transparent) 1px,
            transparent 1px,
            transparent 4px
          );
        }

        .panel-glow::after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: inherit;
          box-shadow: inset 0 1px 0 color-mix(in srgb, var(--astrais-text) 10%, transparent);
          pointer-events: none;
        }

        .catalog-card {
          box-shadow: 0 14px 32px color-mix(in srgb, var(--astrais-background) 28%, transparent);
        }

        ::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </div>
  )
}

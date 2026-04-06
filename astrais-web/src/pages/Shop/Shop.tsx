import React from 'react'
import { NavLink } from 'react-router'
import Navbar from '../../components/layout/Navbar'
import astra from '../../assets/astra.png'
import bgImage from '../../assets/homeScreenBack.jpg'
import logo from '../../assets/logo_w.svg'
import shopArt from '../../assets/shop.png'
import { readClickerStats } from '../Games/gameStorage'
import { SHOP_CATEGORIES, SHOP_ITEMS, getItemsForCategory, getRarityClasses, type ShopCategory } from './shopCatalog'
import { calculateAvailableBalance, calculateSpentLudions, readShopState, type ShopState, writeShopState } from './shopStorage'

const PAGE_SIZE = 4

function usePersistedShopState() {
  const [shopState, setShopState] = React.useState<ShopState>(() => readShopState())

  const updateShopState = React.useCallback((nextState: ShopState) => {
    setShopState(nextState)
    writeShopState(nextState)
  }, [])

  return [shopState, updateShopState] as const
}

export default function Shop() {
  const [shopState, updateShopState] = usePersistedShopState()
  const [activeCategory, setActiveCategory] = React.useState<ShopCategory>('Todos')
  const [currentPage, setCurrentPage] = React.useState(0)
  const [selectedItemId, setSelectedItemId] = React.useState<string>(SHOP_ITEMS[0]?.id ?? '')
  const [gameStats] = React.useState(() => readClickerStats())

  const filteredItems = React.useMemo(() => getItemsForCategory(activeCategory), [activeCategory])
  const pageCount = Math.max(1, Math.ceil(filteredItems.length / PAGE_SIZE))
  const visibleItems = React.useMemo(
    () => filteredItems.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE),
    [currentPage, filteredItems],
  )

  const selectedItem =
    filteredItems.find((item) => item.id === selectedItemId) ??
    visibleItems[0] ??
    SHOP_ITEMS[0]

  const spentLudions = calculateSpentLudions(shopState.ownedIds)
  const availableBalance = calculateAvailableBalance(gameStats.totalLudionsEarned, shopState.ownedIds)
  const isOwned = selectedItem ? shopState.ownedIds.includes(selectedItem.id) : false
  const isEquipped = selectedItem ? shopState.equippedBySlot[selectedItem.slot] === selectedItem.id : false

  const equippedTheme = SHOP_ITEMS.find((item) => item.id === shopState.equippedBySlot.theme)
  const equippedCompanion = SHOP_ITEMS.find((item) => item.id === shopState.equippedBySlot.companion)
  const equippedAura = SHOP_ITEMS.find((item) => item.id === shopState.equippedBySlot.aura)

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
      return
    }

    const selectedIsVisible = visibleItems.some((item) => item.id === selectedItemId)

    if (!selectedIsVisible) {
      setSelectedItemId(visibleItems[0].id)
    }
  }, [selectedItemId, visibleItems])

  const handleBuySelected = () => {
    if (!selectedItem || isOwned || availableBalance < selectedItem.price) {
      return
    }

    updateShopState({
      ...shopState,
      ownedIds: [...shopState.ownedIds, selectedItem.id],
    })
  }

  const handleEquipSelected = () => {
    if (!selectedItem || !isOwned) {
      return
    }

    updateShopState({
      ...shopState,
      equippedBySlot: {
        ...shopState.equippedBySlot,
        [selectedItem.slot]: selectedItem.id,
      },
    })
  }

  return (
    <div
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative h-screen overflow-hidden bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(249,115,22,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(139,92,246,0.24),transparent_38%),radial-gradient(circle_at_center,rgba(34,197,94,0.16),transparent_45%)]" />
      <div className="pointer-events-none absolute inset-0 bg-black/63" />
      <div className="scanlines pointer-events-none absolute inset-0 opacity-25" />

      <div className="relative z-10 flex h-full min-h-0 flex-col">
        <Navbar />

        <main className="flex min-h-0 flex-1 px-3 pb-3 pt-1 md:px-4 md:pb-4 xl:px-6 xl:pb-5">
          <section className="mx-auto hidden h-full w-full max-w-365 gap-3 lg:grid lg:grid-cols-[13.5rem_minmax(0,1.02fr)_16.25rem] min-[1400px]:gap-4 min-[1400px]:grid-cols-[15.75rem_minmax(0,1.24fr)_20.25rem]">
            <aside className="panel-glow relative grid min-h-0 grid-rows-[auto_auto_auto_minmax(0,1fr)] overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(170deg,rgba(15,23,42,0.9),rgba(30,74,99,0.78))] p-3.5 shadow-[0_20px_56px_rgba(7,12,24,0.46)] min-[1400px]:p-5">
              <div className="pointer-events-none absolute -left-12 top-3 h-36 w-36 rounded-full bg-secondary-500/16 blur-3xl" />
              <div className="relative z-10 flex items-start justify-between gap-3">
                <div>
                  <p className="text-[0.64rem] uppercase tracking-[0.28em] text-accent-beige-300">Astrais Store</p>
                  <h1 className="mt-3 font-['Press_Start_2P'] text-[clamp(0.92rem,1.4vw,1.2rem)] leading-tight text-white">
                    Tienda orbital
                  </h1>
                  <p className="mt-2 text-[0.8rem] leading-5 text-slate-300 xl:text-[0.86rem] xl:leading-6">
                    Explora, compra y equipa a tu antojo.
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
                  <p className="mt-2 text-[1rem] font-semibold text-[#f8d089] xl:text-[1.12rem]">{spentLudions}</p>
                </div>
              </div>

              <div className="relative z-10 mt-4 rounded-3xl border border-white/10 bg-black/18 p-3">
                <div className="flex items-center justify-between">
                  <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">Conexion arcade</p>
                  <span className="rounded-full border border-accent-mint-300/25 bg-accent-mint-300/8 px-2 py-1 text-[0.54rem] uppercase tracking-[0.14em] text-accent-mint-300">
                    Activa
                  </span>
                </div>
                <div className="mt-2 space-y-2 text-[0.76rem] text-slate-300 xl:text-[0.82rem]">
                  <div className="flex items-center justify-between">
                    <span>Ludiones del arcade</span>
                    <span className="font-semibold text-white">{gameStats.totalLudionsEarned}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Objetos comprados</span>
                    <span className="font-semibold text-white">{shopState.ownedIds.length}</span>
                  </div>
                </div>
              </div>

              <div className="relative z-10 mt-4 min-h-0 rounded-3xl border border-white/10 bg-black/18 p-3">
                <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-400">Categorias</p>
                <div className="mt-2 grid grid-cols-1 gap-2">
                  {SHOP_CATEGORIES.map((category) => (
                    <button
                      key={category}
                      type="button"
                      onClick={() => setActiveCategory(category)}
                      className={`rounded-2xl border px-3 py-2.5 text-left text-[0.76rem] font-semibold transition xl:text-[0.82rem] ${
                        activeCategory === category
                          ? 'border-transparent bg-linear-to-r from-[#f97316] via-[#ec4899] to-[#8b5cf6] text-white shadow-[0_10px_24px_rgba(236,72,153,0.20)]'
                          : 'border-white/12 bg-white/6 text-slate-200 hover:bg-white/10'
                      }`}
                    >
                      {category}
                    </button>
                  ))}
                </div>
              </div>
            </aside>

            <section className="panel-glow relative grid min-h-0 grid-rows-[auto_minmax(0,1fr)] overflow-hidden rounded-[28px] border border-white/15 bg-[linear-gradient(160deg,rgba(15,23,42,0.88),rgba(76,29,149,0.56),rgba(30,74,99,0.72))] p-3.5 shadow-[0_20px_58px_rgba(7,12,24,0.48)] min-[1400px]:p-5">
              <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.10),transparent_25%)]" />
              <header className="relative z-10 flex items-start justify-between gap-3 min-[1400px]:gap-4">
                <div>
                  <p className="text-[0.64rem] uppercase tracking-[0.28em] text-accent-beige-300">Catalogo activo</p>
                  <h2 className="mt-3 font-['Press_Start_2P'] text-[clamp(0.92rem,1.4vw,1.15rem)] text-white">
                    Seleccion premium
                  </h2>
                </div>

                <div className="flex items-center gap-2.5 min-[1400px]:gap-3">
                  <button
                    type="button"
                    onClick={() => setCurrentPage((page) => Math.max(0, page - 1))}
                    disabled={currentPage === 0}
                    className={`rounded-full border px-3 py-2 text-[0.74rem] font-semibold transition ${
                      currentPage === 0
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
                    disabled={currentPage >= pageCount - 1}
                    className={`rounded-full border px-3 py-2 text-[0.74rem] font-semibold transition ${
                      currentPage >= pageCount - 1
                        ? 'cursor-not-allowed border-white/10 bg-white/6 text-slate-500'
                        : 'border-white/15 bg-white/8 text-white hover:bg-white/12'
                    }`}
                  >
                    Siguiente
                  </button>
                </div>
              </header>

              <div className="relative z-10 mt-4 grid min-h-0 grid-cols-2 gap-3 min-[1400px]:gap-4">
                {visibleItems.map((item) => {
                  const itemOwned = shopState.ownedIds.includes(item.id)
                  const itemEquipped = shopState.equippedBySlot[item.slot] === item.id

                  return (
                    <article
                      key={item.id}
                      onClick={() => setSelectedItemId(item.id)}
                      className={`catalog-card flex min-h-0 cursor-pointer flex-col rounded-3xl border p-3 transition min-[1400px]:p-4 ${
                        selectedItem?.id === item.id
                          ? 'border-accent-beige-300/40 bg-[linear-gradient(160deg,rgba(255,255,255,0.12),rgba(129,140,248,0.10))] shadow-[0_16px_38px_rgba(15,23,42,0.36)]'
                          : 'border-white/12 bg-[rgba(15,23,42,0.74)] hover:border-white/18 hover:bg-white/8'
                      }`}
                    >
                      <div
                        className="relative overflow-hidden rounded-[20px] border border-white/10 px-3 py-3"
                        style={{ background: `linear-gradient(145deg, ${item.accentFrom}33, ${item.accentTo}22)` }}
                      >
                        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.16),transparent_34%)]" />
                        <div className="relative flex items-start justify-between gap-3">
                          <div className="min-w-0">
                            <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-300">{item.category}</p>
                            <h3 className="mt-2 truncate text-[0.82rem] font-semibold text-white min-[1400px]:text-[0.94rem]">{item.name}</h3>
                          </div>
                          <span className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.16em] ${getRarityClasses(item.rarity)}`}>
                            {item.rarity}
                          </span>
                        </div>

                        {item.badge ? (
                          <span className="absolute left-3 top-3 rounded-full border border-white/15 bg-black/35 px-2 py-1 text-[0.5rem] uppercase tracking-[0.16em] text-accent-beige-300">
                            {item.badge}
                          </span>
                        ) : null}

                        <div className="relative mt-10 flex items-end justify-between gap-3">
                          <img src={logo} alt="Astrais logo" className="h-8 w-8 opacity-85 min-[1400px]:h-11 min-[1400px]:w-11" />
                          <p className="max-w-48 text-right text-[0.66rem] leading-5 text-slate-200 min-[1400px]:text-[0.76rem]">
                            {item.shortDescription}
                          </p>
                        </div>
                      </div>

                      <div className="mt-3 flex items-center justify-between">
                        <span className="text-[0.8rem] font-semibold text-[#f8d089] min-[1400px]:text-[0.9rem]">{item.price} L</span>
                        <span className="rounded-full border border-white/12 bg-white/6 px-2 py-1 text-[0.54rem] uppercase tracking-[0.14em] text-slate-300">
                          {itemEquipped ? 'Equipado' : itemOwned ? 'Comprado' : 'Disponible'}
                        </span>
                      </div>
                    </article>
                  )
                })}
              </div>
            </section>

            {selectedItem ? (
              <aside className="panel-glow relative grid min-h-0 grid-rows-[minmax(0,1fr)_auto] overflow-hidden rounded-[26px] border border-white/15 bg-[linear-gradient(170deg,rgba(15,23,42,0.9),rgba(30,74,99,0.76))] p-3.5 shadow-[0_20px_56px_rgba(7,12,24,0.46)] min-[1400px]:p-5">
                <div className="pointer-events-none absolute -right-10 top-6 h-36 w-36 rounded-full bg-secondary-500/18 blur-3xl" />
                <div
                  className="relative min-h-0 overflow-hidden rounded-3xl border border-white/10 p-4"
                  style={{ background: `linear-gradient(145deg, ${selectedItem.accentFrom}30, ${selectedItem.accentTo}18)` }}
                >
                  <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.16),transparent_34%)]" />
                  <img
                    src={shopArt}
                    alt="Arte de la tienda"
                    className="pointer-events-none absolute -right-20 bottom-0 hidden h-[clamp(7rem,18vh,10rem)] opacity-35 min-[1400px]:block"
                  />
                  <img
                    src={astra}
                    alt="Mascota Astrais"
                    className="pointer-events-none absolute bottom-0 right-2 hidden h-[clamp(5rem,12vh,7rem)] opacity-75 min-[1400px]:block"
                  />

                  <div className="relative z-10 grid h-full min-h-0 grid-rows-[auto_auto_auto_auto_auto_auto]">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-[0.58rem] uppercase tracking-[0.18em] text-slate-300">{selectedItem.category}</p>
                        <h2 className="mt-2 font-['Press_Start_2P'] text-[0.8rem] leading-snug text-white min-[1400px]:text-[0.96rem]">
                          {selectedItem.name}
                        </h2>
                      </div>
                      <span className={`rounded-full border px-2 py-1 text-[0.52rem] uppercase tracking-[0.16em] ${getRarityClasses(selectedItem.rarity)}`}>
                        {selectedItem.rarity}
                      </span>
                    </div>

                    <p className="mt-4 text-[0.74rem] leading-5 text-slate-200 min-[1400px]:text-[0.86rem] min-[1400px]:leading-6">{selectedItem.detail}</p>

                    <div className="mt-4 grid grid-cols-2 gap-3">
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.56rem] uppercase tracking-[0.16em] text-slate-400">Precio</p>
                        <p className="mt-2 text-[0.86rem] font-semibold text-[#f8d089] min-[1400px]:text-[1.04rem]">{selectedItem.price} L</p>
                      </div>
                      <div className="rounded-2xl border border-white/10 bg-black/18 p-3">
                        <p className="text-[0.56rem] uppercase tracking-[0.16em] text-slate-400">Slot</p>
                        <p className="mt-2 text-[0.7rem] font-semibold uppercase tracking-[0.14em] text-white min-[1400px]:text-[0.82rem]">
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
                        disabled={isOwned || availableBalance < selectedItem.price}
                          className={`rounded-2xl px-4 py-3 text-[0.72rem] font-semibold transition min-[1400px]:text-[0.82rem] ${
                          isOwned
                            ? 'cursor-default border border-accent-mint-300/25 bg-accent-mint-300/12 text-accent-mint-300'
                            : availableBalance < selectedItem.price
                              ? 'cursor-not-allowed border border-white/10 bg-white/8 text-slate-400'
                              : 'border border-transparent bg-linear-to-r from-[#f97316] via-[#ec4899] to-[#8b5cf6] text-white shadow-[0_14px_28px_rgba(236,72,153,0.22)] hover:-translate-y-0.5'
                        }`}
                      >
                        {isOwned ? 'Ya comprado' : 'Comprar ahora'}
                      </button>

                      <button
                        type="button"
                        onClick={handleEquipSelected}
                        disabled={!isOwned || isEquipped}
                          className={`rounded-2xl px-4 py-3 text-[0.72rem] font-semibold transition min-[1400px]:text-[0.82rem] ${
                          !isOwned
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

                <div className="mt-4 flex items-center justify-between gap-3">
                  <div className="rounded-2xl border border-white/10 bg-black/18 px-4 py-3 text-[0.72rem] leading-5 text-slate-300 min-[1400px]:text-[0.8rem]">
                    Si necesitas mas saldo, el arcade ya alimenta esta tienda con lo que ganas en minijuegos.
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
            <article className="rounded-[28px] border border-white/15 bg-[rgba(15,23,42,0.84)] p-6 text-center shadow-[0_24px_60px_rgba(7,12,24,0.45)] backdrop-blur-sm">
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
            rgba(255, 255, 255, 0.06) 0,
            rgba(255, 255, 255, 0.06) 1px,
            transparent 1px,
            transparent 4px
          );
        }

        .panel-glow::after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: inherit;
          box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
          pointer-events: none;
        }

        .catalog-card {
          box-shadow: 0 14px 32px rgba(7, 12, 24, 0.22);
        }
      `}</style>
    </div>
  )
}

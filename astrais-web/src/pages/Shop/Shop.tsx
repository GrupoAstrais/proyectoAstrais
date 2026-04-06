import Navbar from "../../components/layout/Navbar"
import bgImage from '../../assets/homeScreenBack.jpg'
import logo from '../../assets/logo_w.svg'

// Mock data para visualización
const PRODUCTS = Array.from({ length: 11 }, (_, i) => ({
  id: i + 1,
  name: `Pixel Item ${i + 1}`,
  price: `${(Math.random() * 80 + 15).toFixed(0)}`,
  tag: i % 4 === 0 ? 'HOT' : i % 4 === 1 ? 'NEW' : null
}))

const CATEGORIES = ['All', 'Events', 'Themes', 'Pets', 'Specials']

export default function Shop() {
  return (
    <div 
      className="flex flex-col h-screen relative bg-cover bg-center bg-no-repeat font-['Space_Grotesk'] text-white overflow-hidden"
      style={{ backgroundImage: `url(${bgImage})` }}>

      <div className="absolute inset-0 bg-linear-to-b from-black/70 via-black/50 to-black/80 pointer-events-none" />
      
      <div 
        className="absolute inset-0 pointer-events-none z-0 opacity-[0.03]"
        style={{ backgroundImage: `repeating-linear-gradient(0deg, transparent, transparent 2px, #000 2px, #000 4px)` }}
      />

      <div className="relative z-50"> {/*El z es para posicionar el elemento por encima o por debajo, ya que se está usando relative*/}
        <Navbar />
      </div>
      
      <div className="relative z-10 grid grid-cols-12 gap-6 w-full mx-auto px-6 pt-2">
        
        <aside className="col-span-2 row-span-1 flex flex-col gap-3 h-fit sticky top-24
                          bg-black/40 backdrop-blur-xs border-4 border-primary-500/80 rounded-md
                          shadow-[0_0_20px_rgba(167,139,250,0.3),8px_8px_0px_0px_#4c2191]
                          p-4 transition-all duration-300 hover:border-accent-beige-300">
          
          <h2 className="text-lg font-bold text-accent-beige-500 tracking-widest uppercase 
                         border-b-2 border-primary-500/50 pb-2 mb-1 flex items-center gap-2">
            <span className="inline-block w-2 h-2 animate-pulse" /> FILTERS
          </h2>
          
          {CATEGORIES.map((cat, idx) => (
            <button 
              key={cat}
              className={`w-full text-left px-3 py-2 font-semibold text-sm tracking-wide transition-all duration-200 
                          border-2 rounded-sm relative overflow-hidden group
                          ${idx === 0 
                            ? 'bg-primary-500 border-white text-white shadow-[4px_4px_0px_0px_#000] -translate-y-0.5' 
                            : 'bg-transparent border-primary-500/40 text-gray-300 hover:bg-primary-500/20 hover:border-accent-beige-300 hover:text-white hover:shadow-[3px_3px_0px_0px_#000] hover:-translate-y-0.5'
                          }`}
            >
              {/* Efecto brillo al hover */}
              <span className="absolute inset-0 bg-white/10 -translate-x-full group-hover:translate-x-0 transition-transform duration-300 skew-x-12" />
              <span className="relative z-10">{cat}</span>
            </button>
          ))}
        
        </aside>

        <div className="col-span-2 row-start-2 ml-2 text-center h-fit w-fit place-self-center
        bg-black/40 backdrop-blur-xs border-4 border-primary-500/80 rounded-md
                          font-semibold tracking-wide
                           hover:bg-primary-500/20 hover:text-white hover:shadow-[3px_3px_0px_0px_#000] hover:-translate-y-0.5'
                          p-4 transition-all duration-300 hover:border-accent-beige-300">
            proximamente... 
        </div>

        {/*PANEL DE PRODUCTOS (col-span-10)*/}
        <main className="col-span-10 row-span-2 bg-black/30 backdrop-blur-md border-4 border-secondary-500/60 rounded-md
                         shadow-[0_0_25px_rgba(148,163,184,0.2),10px_10px_0px_0px_#3b1873] 
                         p-5 flex flex-col relative overflow-hidden">
          
          {/* Cabecera del catálogo */}
          <div className="flex items-center justify-between mb-2 pb-3 border-b-2 border-dashed border-white/20">
            <h2 className="text-2xl font-bold tracking-[0.15em] text-accent-beige-300 drop-shadow-md">
              SHOP
            </h2>
            <div className="flex items-center gap-2 text-xs font-bold bg-black/60 px-3 py-1 border-2 border-gray-600 rounded-sm text-gray-300">
              <span className="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse" />
              {PRODUCTS.length} AVAILABLE ITEMS
            </div>
          </div>

          {/* Grid de productos */}
          <div className="pt-3 grid grid-cols-6 gap-4 overflow-y-auto pr-2 max-h-[calc(100vh-240px)] custom-scrollbar">
            {PRODUCTS.map((product) => (
              <div key={product.id} className="col-span-2 group">
                <div className="bg-slate-900/80 border-4 border-primary-500/50 p-2.5 
                                relative transition-all duration-300 ease-out
                                hover:border-accent-beige-300 hover:shadow-[0_0_15px_rgba(255,215,150,0.5),6px_6px_0px_0px_#0f172a] 
                                hover:-translate-y-1">
                  
                  {/* Badge animado */}
                  {product.tag && (
                    <span className="absolute -top-2 -left-2 z-20 bg-state-error text-white text-[9px] font-black px-2 py-0.5 
                                     border-2 border-slate-900 shadow-md animate-bounce"
                          style={{ animationDuration: '2s' }}>
                      {product.tag}
                    </span>
                  )}

                  {/* Imagen placeholder */}
                  <div className="aspect-square bg-linear-to-br from-primary-600/20 to-secondary-700/30 
                                  border-2 border-white/5 mb-2 overflow-hidden relative pixel-frame">
                    <img 
                      src={`https://picsum.photos/seed/pix${product.id}/200/200`} 
                      alt={product.name} 
                      className="w-full h-full object-cover pixelated transition-transform duration-500 group-hover:scale-110" 
                    />
                    {/* Overlay de brillo al hover */}
                    <div className="absolute inset-0 bg-white/0 group-hover:bg-white/10 transition-colors duration-300" />
                  </div>
                  
                  <h3 className="text-sm font-bold text-white truncate mb-1 tracking-wide">{product.name}</h3>
                  
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-base font-black text-accent-beige-300 drop-shadow-sm">{product.price}
                        <img src={logo} alt="Astrais logo" className="inline-block w-6 h-6 mb-1.5 ml-0.5" />
                    </span>
                    <button className="bg-primary-500 hover:bg-primary-400 text-white text-[10px] font-extrabold px-3 py-1 
                                       border-2 border-white/40 shadow-[3px_3px_0px_0px_#000] 
                                       active:translate-y-0.5 active:shadow-none transition-all duration-150 rounded-sm
                                       hover:shadow-[0_0_10px_rgba(167,139,250,0.6)]">
                      + BUY IT
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>

      {/* CSS Personalizado para efectos avanzados */}
      <style>{`
        /* Forzar renderizado nítido de imágenes */
        .pixelated {
          image-rendering: pixelated;
          image-rendering: -moz-crisp-edges;
          image-rendering: crisp-edges;
        }
        
        /* Marco pixelado sutil alrededor de imágenes */
        .pixel-frame::before {
          content: '';
          position: absolute;
          inset: 0;
          border: 2px solid rgba(255,255,255,0.05);
          box-shadow: inset 0 0 10px rgba(0,0,0,0.4);
          pointer-events: none;
        }

        /* Scrollbar retro */
        .custom-scrollbar::-webkit-scrollbar { width: 10px; }
        .custom-scrollbar::-webkit-scrollbar-track { 
          background: #0f172a; 
          border-left: 2px solid #334155; 
        }
        .custom-scrollbar::-webkit-scrollbar-thumb { 
          background: #8b5cf6; 
          border: 2px solid #0f172a;
          box-shadow: inset 0 0 0 2px #c084fc;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #a78bfa; }
        .custom-scrollbar { scrollbar-color: #8b5cf6 #0f172a; }
      `}</style>
    </div>
  )
}
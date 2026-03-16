import Navbar from '../../components/layout/Navbar'
import '../../styles/Home.css'
import bgImage from '../../assets/homeScreenBack.jpg'
import astra from '../../assets/astra.png'

export default function Home() {
  return (
    <main style={{ backgroundImage: `url(${bgImage})` }} className="h-screen to-40% bg-no-repeat bg-cover font-['Space_Grotesk'] ">
      <Navbar />

      <section className="flex flex-col justify-center items-center gap-3 text-white" aria-label="Panel principal de Astrais">
        <article className="relative home-card w-1/2 h-72">
          <header>
            <p className="home-card__eyebrow pb-2">Bienvenido de vuelta</p>
            <h1 className="font-['Press_Start_2P']">Hi, Astraïs </h1>
            <p>
              ¿Qué te queda por hacer? 
            </p>
          </header>
          <div className="home-actions justify-between flex flex-row w-2/3">
            <div className='flex flex-col gap-4'>
              <button className="home-btn home-btn--primary" type="button">Crear tarea</button>
              <button className="home-btn" type="button">Unirme a un grupo</button>
            </div>
            <div className='flex flex-col gap-4'>
              <button className="home-btn" type="button">Ver agenda</button>
              <button className="home-btn" type="button">Reclamar recompensa</button>
            </div>
          </div>
          <img className="absolute -right-56 -bottom-7" src={astra} /> 
        </article>

        <div className='flex flex-row gap-3'>
          <article className="home-card home-card--notifications">
            <header>
              <h2>Notificaciones sin leer</h2>
              <span className="home-badge">6 nuevas</span>
            </header>
            <ul>
              <li>📩 Tienes una invitación al grupo “Proyecto IA”.</li>
              <li>🏆 Lograste un nuevo hito semanal.</li>
              <li>⏰ Tu tarea “Repasar React” vence en 2 horas.</li>
              <li>🛍️ Nuevo ítem disponible en la tienda cósmica.</li>
            </ul>
          </article>

          <div className='flex flex-col gap-3'>
            <div className='flex flex-row gap-3'>
              <article className="home-card home-card--achievements">
                <header>
                  <h2>Logros del usuario</h2>
                </header>
                <ul className="home-list home-list--compact">
                  <li>Nivel actual: <strong>12</strong></li>
                  <li>Racha activa: <strong>9 días</strong></li>
                  <li>Misiones completadas: <strong>37</strong></li>
                </ul>
              </article>

              <div className='flex flex-col gap-3'>
                <article className="home-card home-card--tasks">
                  <header>
                    <h2>Tareas</h2>
                  </header>
                  <div className="home-columns">
                    <div>
                      <h3>Pendientes</h3>
                      <p>5 tareas</p>
                    </div>
                    <div>
                      <h3>Próximas</h3>
                      <p>3 tareas</p>
                    </div>
                    <div>
                      <h3>En curso</h3>
                      <p>2 tareas</p>
                    </div>
                  </div>
                </article>

                <article className="home-card home-card--shop">
                  <header>
                    <h2>Tienda</h2>
                  </header>
                  <p>Canjea tus monedas por avatares, fondos y potenciadores de productividad.</p>
                  <button className="home-btn" type="button">Entrar a la tienda</button>
                </article>
              </div>
            </div>

            <article className="home-card home-card--games">
              <header>
                <h2>Videojuegos</h2>
              </header>
              <p>Explora minijuegos para ganar experiencia y mantener tu racha.</p>
              <button className="home-btn" type="button">Jugar ahora</button>
            </article>
          </div>
        </div>
      </section>
    </main>
  )
}
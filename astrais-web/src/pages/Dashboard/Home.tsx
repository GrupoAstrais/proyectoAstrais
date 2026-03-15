import Navbar from '../../components/layout/Navbar'
import '../../styles/Home.css'

export default function Home() {
  return (
    <main className="home-screen">
      <Navbar />

      <section className="home-content" aria-label="Panel principal de Astrais">
        <article className="home-card home-card--welcome">
          <header>
            <p className="home-card__eyebrow">Bienvenido de vuelta</p>
            <h1>Hola, AstroUsuario 🚀</h1>
            <p>
              ¿Qué quieres hacer hoy? Aquí tienes accesos rápidos para avanzar en tus objetivos.
            </p>
          </header>
          <div className="home-actions">
            <button className="home-btn home-btn--primary" type="button">Crear tarea</button>
            <button className="home-btn" type="button">Unirme a un grupo</button>
            <button className="home-btn" type="button">Ver agenda</button>
            <button className="home-btn" type="button">Reclamar recompensa</button>
          </div>
        </article>

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

        <article className="home-card home-card--games">
          <header>
            <h2>Videojuegos</h2>
          </header>
          <p>Explora minijuegos para ganar experiencia y mantener tu racha.</p>
          <button className="home-btn" type="button">Jugar ahora</button>
        </article>
      </section>
    </main>
  )
}
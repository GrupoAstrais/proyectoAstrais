import Navbar from "../../components/layout/Navbar";
import "../../styles/Home.css";

const dashboardCards = [
  {
    title: "Resumen diario",
    description: "Visualiza tus tareas prioritarias y métricas clave del día en un solo lugar.",
  },
  {
    title: "Grupos activos",
    description: "Accede rápido a tus equipos y revisa actividad reciente sin perder contexto.",
  },
  {
    title: "Tienda",
    description: "Canjea recompensas y descubre nuevos ítems con una interfaz clara y contrastada.",
  },
];

export default function Home() {
  return (
    <section className="home-page">
      <div className="home-navbar-shell">
        <Navbar />
      </div>

      <main className="home-content">
        <div className="home-grid">
          {dashboardCards.map((card) => (
            <article key={card.title} className="home-card">
              <h3>{card.title}</h3>
              <p>{card.description}</p>
            </article>
          ))}
        </div>
      </main>
    </section>
  );
}
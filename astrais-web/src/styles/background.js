(() => {
  const canvas = document.getElementById("orbital-background");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");
  if (!ctx) return;
  let width = 0;
  let height = 0;
  let stars = [];

  // Configuracion de los orbes.
  const orbs = [
    {
      color: "42, 133, 255",
      core: "76, 151, 255",
      speed: 0.00004,
      phase: 0.3,
      size: 0.28,
      xAmp: 0.39,
      yAmp: 0.33,
      xFreq: 1.0,
      yFreq: 1.47,
    },
    {
      color: "48, 219, 145",
      core: "92, 232, 166",
      speed: 0.00002,
      phase: 2.4,
      size: 0.25,
      xAmp: 0.37,
      yAmp: 0.36,
      xFreq: 1.32,
      yFreq: 0.92,
    },
    {
      color: "165, 88, 255",
      core: "178, 116, 255",
      speed: 0.00009,
      phase: 4.7,
      size: 0.3,
      xAmp: 0.41,
      yAmp: 0.31,
      xFreq: 0.86,
      yFreq: 1.22,
    },
  ];

  const randomBetween = (min, max) => min + Math.random() * (max - min);

  function resize() {
    const pixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    width = window.innerWidth;
    height = window.innerHeight;

    // Ajuste nitido para pantallas de alta densidad.
    canvas.width = Math.floor(width * pixelRatio);
    canvas.height = Math.floor(height * pixelRatio);
    ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);

    // Estrellas proporcionales al tamano de pantalla.
    const starCount = Math.max(160, Math.min(520, Math.floor((width * height) / 5200)));
    stars = Array.from({ length: starCount }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      radius: randomBetween(0.45, 1.7),
      alpha: randomBetween(0.22, 0.95),
      phase: randomBetween(0, Math.PI * 2),
      twinkle: randomBetween(0.0007, 0.002),
      drift: randomBetween(-0.018, 0.018),
    }));
  }

  function drawStars(time) {
    ctx.save();
    ctx.globalCompositeOperation = "source-over";

    for (const star of stars) {
      const x = (star.x + time * star.drift + width) % width;
      const twinkle = 0.62 + Math.sin(time * star.twinkle + star.phase) * 0.38;
      const alpha = Math.max(0.08, star.alpha * twinkle);

      ctx.beginPath();
      ctx.fillStyle = `rgba(237, 245, 255, ${alpha})`;
      ctx.arc(x, star.y, star.radius, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.restore();
  }

  function getOrbPosition(orb, time) {
    const t = time * orb.speed;
    const variation = Math.sin(t * 0.41 + orb.phase * 1.9) * 0.055;

    // Trayectoria libre en X/Y.
    return {
      x: width * (0.5 + Math.sin(t * orb.xFreq + orb.phase) * orb.xAmp + variation),
      y:
        height *
        (0.5 + Math.cos(t * orb.yFreq + orb.phase * 0.7) * orb.yAmp - variation * 0.75),
    };
  }

  function drawOrb(orb, time, index) {
    const { x, y } = getOrbPosition(orb, time);
    const radius = Math.max(190, Math.min(width, height) * orb.size);
    const pulse = 1 + Math.sin(time * 0.001 + orb.phase) * 0.025;
    const glowRadius = radius * 2.35 * pulse;

    ctx.save();
    ctx.globalCompositeOperation = "screen";

    // Halo exterior.
    const glow = ctx.createRadialGradient(x, y, 0, x, y, glowRadius);
    glow.addColorStop(0, `rgba(${orb.core}, 0.17)`);
    glow.addColorStop(0.18, `rgba(${orb.color}, 0.075)`);
    glow.addColorStop(0.55, `rgba(${orb.color}, 0.045)`);
    glow.addColorStop(1, `rgba(${orb.color}, 0.005)`);
    ctx.fillStyle = glow;
    ctx.beginPath();
    ctx.arc(x, y, glowRadius, 0, Math.PI * 2);
    ctx.fill();

    // Niebla interior.
    const haze = ctx.createRadialGradient(x, y, radius * 0.15, x, y, radius);
    haze.addColorStop(0, `rgba(${orb.core}, 0.035)`);
    haze.addColorStop(0.45, `rgba(${orb.color}, 0.025)`);
    haze.addColorStop(1, `rgba(${orb.color}, 0)`);
    ctx.fillStyle = haze;
    ctx.beginPath();
    ctx.arc(x, y, radius * (0.86 + index * 0.03), 0, Math.PI * 2);
    ctx.fill();

    ctx.restore();
  }

  function draw(time) {
    ctx.clearRect(0, 0, width, height);

    // Velo de fondo muy suave.
    const backdrop = ctx.createLinearGradient(0, 0, width, height);
    backdrop.addColorStop(0, "rgba(4, 7, 18, 0.03)");
    backdrop.addColorStop(0.5, "rgba(7, 10, 24, 0.04)");
    backdrop.addColorStop(1, "rgba(3, 8, 13, 0.02)");
    ctx.fillStyle = backdrop;
    ctx.fillRect(0, 0, width, height);

    orbs.forEach((orb, index) => drawOrb(orb, time, index));
    drawStars(time);
    requestAnimationFrame(draw);
  }

  resize();
  window.addEventListener("resize", resize);
  requestAnimationFrame(draw);
})();

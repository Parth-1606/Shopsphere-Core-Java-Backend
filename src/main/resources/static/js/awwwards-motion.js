/**
 * ShopSphere cinematic motion engine
 * GSAP + rAF — vanilla (no React / Framer Motion)
 */
(function () {
  const REDUCE = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const FINE = window.matchMedia('(pointer: fine)').matches;
  const SPRING = { type: 'spring', stiffness: 120, damping: 18 };
  // GSAP spring-ish via custom ease + duration
  const springOut = 'power3.out';
  const expoOut = 'expo.out';

  let gsapReady = false;
  let orbitRaf = null;
  let particleRaf = null;
  let mouse = { x: 0.5, y: 0.5, sx: 0.5, sy: 0.5 };
  let orbitSpeed = 1;
  let orbitPaused = false;
  let homeCleanup = [];

  function whenGsap(cb) {
    if (window.gsap) {
      gsapReady = true;
      cb(window.gsap);
    } else {
      const t = setInterval(() => {
        if (window.gsap) {
          clearInterval(t);
          gsapReady = true;
          cb(window.gsap);
        }
      }, 30);
    }
  }

  /* ---------- DOM chrome ---------- */
  function ensureChrome() {
    if (!document.querySelector('.ss-fx-layer')) {
      const fx = document.createElement('div');
      fx.className = 'ss-fx-layer';
      fx.innerHTML = `
        <div class="ss-mesh" id="ssMesh"></div>
        <canvas id="ssParticles"></canvas>
        <div class="ss-noise"></div>
      `;
      document.body.prepend(fx);
    }
    if (!document.querySelector('.ss-curtain')) {
      const c = document.createElement('div');
      c.className = 'ss-curtain';
      document.body.prepend(c);
    }
    if (!document.querySelector('.ss-progress')) {
      const p = document.createElement('div');
      p.className = 'ss-progress';
      document.body.appendChild(p);
    }
    if (!document.querySelector('.ss-float-cta')) {
      const a = document.createElement('a');
      a.href = '#/shop';
      a.className = 'ss-float-cta';
      a.id = 'ssFloatCta';
      a.innerHTML = 'Shop everything <span class="btn-arrow">→</span>';
      document.body.appendChild(a);
    }
    if (FINE && !REDUCE && !document.querySelector('.ss-cursor')) {
      const cur = document.createElement('div');
      cur.className = 'ss-cursor';
      const ring = document.createElement('div');
      ring.className = 'ss-cursor-ring';
      document.body.appendChild(cur);
      document.body.appendChild(ring);
      document.body.classList.add('ss-cursor-on');
    }
  }

  /* ---------- Page load ---------- */
  function runIntro() {
    ensureChrome();
    const nav = document.querySelector('header.nav');
    const marquee = document.querySelector('.marquee-wrap');
    const curtain = document.querySelector('.ss-curtain');
    const mesh = document.getElementById('ssMesh');

    nav?.classList.add('ss-nav-intro');
    marquee?.classList.add('ss-marquee-intro');
    document.body.classList.add('ss-loading');

    if (REDUCE || !window.gsap) {
      curtain?.remove();
      nav?.classList.remove('ss-nav-intro');
      marquee?.classList.add('ss-marquee-in');
      mesh?.classList.add('on');
      document.body.classList.remove('ss-loading');
      document.body.classList.add('ss-ready');
      return;
    }

    whenGsap((gsap) => {
      const tl = gsap.timeline({
        defaults: { ease: expoOut },
        onComplete: () => {
          document.body.classList.remove('ss-loading');
          document.body.classList.add('ss-ready');
          curtain?.remove();
        }
      });

      tl.to(curtain, { yPercent: -105, duration: 0.85, ease: 'power4.inOut' }, 0.15);
      tl.to(marquee, { y: 0, duration: 0.7, ease: expoOut, onStart: () => marquee?.classList.add('ss-marquee-in') }, 0.35);
      tl.to(nav, {
        opacity: 1,
        y: 0,
        filter: 'blur(0px)',
        duration: 0.75,
        ease: expoOut,
        onComplete: () => nav?.classList.remove('ss-nav-intro')
      }, 0.45);
      tl.to(mesh, { opacity: 1, duration: 1.1, ease: 'power2.out', onStart: () => mesh?.classList.add('on') }, 0.5);
    });
  }

  /* ---------- Cursor + mesh parallax ---------- */
  function initPointerFx() {
    const mesh = document.getElementById('ssMesh');
    const cur = document.querySelector('.ss-cursor');
    const ring = document.querySelector('.ss-cursor-ring');
    let rx = window.innerWidth / 2, ry = window.innerHeight / 2;
    let cx = rx, cy = ry;

    window.addEventListener('pointermove', (e) => {
      mouse.x = e.clientX / window.innerWidth;
      mouse.y = e.clientY / window.innerHeight;
      rx = e.clientX;
      ry = e.clientY;
      if (mesh && !REDUCE) {
        const dx = (mouse.x - 0.5) * 40;
        const dy = (mouse.y - 0.5) * 30;
        mesh.style.transform = `translate3d(${dx}px, ${dy}px, 0)`;
      }
    }, { passive: true });

    function tickCursor() {
      cx += (rx - cx) * 0.18;
      cy += (ry - cy) * 0.18;
      if (cur) cur.style.transform = `translate(${rx}px, ${ry}px) translate(-50%,-50%)`;
      if (ring) ring.style.transform = `translate(${cx}px, ${cy}px) translate(-50%,-50%)`;
      mouse.sx += (mouse.x - mouse.sx) * 0.08;
      mouse.sy += (mouse.y - mouse.sy) * 0.08;
      requestAnimationFrame(tickCursor);
    }
    if (FINE && !REDUCE) requestAnimationFrame(tickCursor);

    document.addEventListener('pointerdown', () => {
      if (ring) { ring.style.width = '22px'; ring.style.height = '22px'; }
    });
    document.addEventListener('pointerup', () => {
      if (ring) { ring.style.width = '34px'; ring.style.height = '34px'; }
    });
  }

  /* ---------- Particles ---------- */
  function initParticles() {
    const canvas = document.getElementById('ssParticles');
    if (!canvas || REDUCE) return;
    const ctx = canvas.getContext('2d');
    let w, h, particles;

    function resize() {
      w = canvas.width = window.innerWidth;
      h = canvas.height = window.innerHeight;
      particles = Array.from({ length: Math.min(48, Math.floor(w / 30)) }, () => ({
        x: Math.random() * w,
        y: Math.random() * h,
        r: Math.random() * 1.6 + 0.3,
        vx: (Math.random() - 0.5) * 0.25,
        vy: -Math.random() * 0.35 - 0.05,
        a: Math.random() * 0.45 + 0.15
      }));
    }
    resize();
    window.addEventListener('resize', resize);

    function frame() {
      ctx.clearRect(0, 0, w, h);
      for (const p of particles) {
        p.x += p.vx + (mouse.sx - 0.5) * 0.4;
        p.y += p.vy;
        if (p.y < -10) { p.y = h + 10; p.x = Math.random() * w; }
        if (p.x < 0) p.x = w;
        if (p.x > w) p.x = 0;
        ctx.beginPath();
        ctx.fillStyle = `rgba(198,255,61,${p.a})`;
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
      }
      particleRaf = requestAnimationFrame(frame);
    }
    frame();
  }

  /* ---------- Scroll progress + float CTA + nav glass ---------- */
  function initScrollChrome() {
    const bar = document.querySelector('.ss-progress');
    const nav = document.querySelector('header.nav');
    const float = document.getElementById('ssFloatCta');
    const heroH = () => Math.min(window.innerHeight * 0.72, 640);

    function onScroll() {
      const max = document.documentElement.scrollHeight - window.innerHeight;
      const pct = max > 0 ? (window.scrollY / max) * 100 : 0;
      if (bar) bar.style.width = pct + '%';
      nav?.classList.toggle('ss-scrolled', window.scrollY > 24);
      const pastHero = window.scrollY > heroH() * 0.55;
      const onHome = (location.hash || '#/') === '#/' || location.hash === '';
      float?.classList.toggle('show', onHome && pastHero && !REDUCE);
    }
    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('hashchange', onScroll);
    onScroll();
  }

  /* ---------- Magnetic + ripple buttons ---------- */
  function enhanceButton(el) {
    if (!el || el.dataset.ssBtn) return;
    el.dataset.ssBtn = '1';

    const arrow = el.querySelector('.btn-arrow');
    if (!arrow && el.textContent.includes('→')) {
      el.innerHTML = el.innerHTML.replace('→', '<span class="btn-arrow">→</span>');
    }

    el.addEventListener('pointermove', (e) => {
      if (REDUCE || !FINE) return;
      const r = el.getBoundingClientRect();
      const x = e.clientX - r.left - r.width / 2;
      const y = e.clientY - r.top - r.height / 2;
      el.style.transform = `translate(${x * 0.18}px, ${y * 0.22}px) scale(1.04)`;
      if (el.classList.contains('btn-primary')) el.classList.add('ss-glow');
      if (el.classList.contains('btn-ghost')) el.classList.add('ss-glass');
    });
    el.addEventListener('pointerleave', () => {
      el.style.transform = '';
      el.classList.remove('ss-glow', 'ss-glass');
    });
    el.addEventListener('pointerdown', (e) => {
      const r = el.getBoundingClientRect();
      const ripple = document.createElement('span');
      ripple.className = 'ss-ripple';
      const size = Math.max(r.width, r.height);
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - r.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - r.top - size / 2) + 'px';
      el.appendChild(ripple);
      setTimeout(() => ripple.remove(), 650);

      if (window.gsap && !REDUCE) {
        gsap.fromTo(el, { scale: 0.94 }, { scale: 1, duration: 0.45, ease: 'elastic.out(1, 0.45)' });
        spawnClickParticles(e.clientX, e.clientY);
      }
    });
  }

  function spawnClickParticles(x, y) {
    for (let i = 0; i < 8; i++) {
      const d = document.createElement('div');
      d.style.cssText = `position:fixed;left:${x}px;top:${y}px;width:4px;height:4px;border-radius:50%;background:var(--accent);pointer-events:none;z-index:9990;box-shadow:0 0 8px rgba(198,255,61,.8);`;
      document.body.appendChild(d);
      const ang = (Math.PI * 2 * i) / 8;
      const dist = 28 + Math.random() * 36;
      if (window.gsap) {
        gsap.to(d, {
          x: Math.cos(ang) * dist,
          y: Math.sin(ang) * dist,
          opacity: 0,
          duration: 0.55,
          ease: expoOut,
          onComplete: () => d.remove()
        });
      } else {
        d.remove();
      }
    }
  }

  window.ssEnhanceButtons = function (root = document) {
    root.querySelectorAll('.btn-primary, .btn-ghost, .add-to-bag').forEach(enhanceButton);
  };

  /* ---------- Split text ---------- */
  function splitTitle(el) {
    const lines = [
      { text: 'SHOP EVERY', outline: false },
      { text: 'ORBIT', outline: true }
    ];
    el.innerHTML = lines.map((line, li) => {
      const chars = [...line.text].map((ch) =>
        ch === ' '
          ? `<span class="char" style="width:0.28em">&nbsp;</span>`
          : `<span class="char${line.outline ? ' outline-word' : ''}">${ch}</span>`
      ).join('');
      const sweep = line.outline ? '' : '<span class="neon-sweep" aria-hidden="true"></span>';
      const cls = line.outline ? 'outline-word chromatic' : 'sweep-host chromatic';
      return `<span class="hero-title-line"><span class="word ${cls}">${chars}${sweep}</span></span>`;
    }).join('');
  }

  /* ---------- Orbit SVG + rAF ---------- */
  function buildOrbitSVG() {
    return `
    <svg class="hero-orbit-svg" id="heroOrbitSvg" viewBox="0 0 400 400" aria-hidden="true">
      <defs>
        <filter id="planetGlow">
          <feGaussianBlur stdDeviation="2.5" result="b"/>
          <feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge>
        </filter>
      </defs>
      <circle class="ring-path" data-ring="0" cx="200" cy="200" r="175" stroke="rgba(198,255,61,0.28)" stroke-width="1.2"/>
      <circle class="ring-path" data-ring="1" cx="200" cy="200" r="130" stroke="rgba(255,62,165,0.22)" stroke-width="1"/>
      <circle class="ring-path" data-ring="2" cx="200" cy="200" r="85" stroke="rgba(46,196,255,0.22)" stroke-width="1"/>
      <g id="orbitPlanets" filter="url(#planetGlow)">
        <circle class="planet-node" data-r="175" data-speed="0.35" data-size="7" fill="#c6ff3d" cx="375" cy="200" r="7"/>
        <circle class="planet-node" data-r="130" data-speed="-0.22" data-size="5" fill="#ff3ea5" cx="330" cy="200" r="5"/>
        <circle class="planet-node" data-r="85" data-speed="0.55" data-size="4" fill="#2ec4ff" cx="285" cy="200" r="4"/>
      </g>
      <path id="cometPath" d="" fill="none" stroke="rgba(245,245,247,0.55)" stroke-width="1.5" stroke-linecap="round" opacity="0"/>
    </svg>`;
  }

  function startOrbit(svg) {
    if (!svg || REDUCE) {
      if (svg) svg.style.opacity = '1';
      return;
    }
    const rings = [...svg.querySelectorAll('.ring-path')];
    const planets = [...svg.querySelectorAll('.planet-node')];
    const angles = planets.map((_, i) => (i / planets.length) * Math.PI * 2);
    const ringAngles = [0, 0, 0];
    const speeds = [0.12, -0.08, 0.18];
    let last = performance.now();
    let cometTimer = 0;

    // stroke draw via GSAP
    whenGsap((gsap) => {
      rings.forEach((ring, i) => {
        const len = ring.getTotalLength();
        ring.style.strokeDasharray = len;
        ring.style.strokeDashoffset = len;
        gsap.to(ring, {
          strokeDashoffset: 0,
          duration: 1.2,
          delay: 0.35 + i * 0.15,
          ease: expoOut
        });
      });
      gsap.to(svg, { opacity: 1, duration: 0.8, delay: 0.2, ease: expoOut });
    });

    function frame(now) {
      const dt = Math.min(0.05, (now - last) / 1000);
      last = now;
      const boost = 1 + (mouse.sx - 0.5) * 0.8 + (mouse.sy - 0.5) * 0.4;
      const spd = (orbitPaused ? 0.25 : 1) * orbitSpeed * boost;

      rings.forEach((ring, i) => {
        ringAngles[i] += speeds[i] * spd * dt;
        ring.setAttribute('transform', `rotate(${ringAngles[i] * 57.3} 200 200)`);
      });

      planets.forEach((p, i) => {
        const r = +p.dataset.r;
        const s = +p.dataset.speed;
        angles[i] += s * spd * dt;
        const x = 200 + Math.cos(angles[i]) * r;
        const y = 200 + Math.sin(angles[i]) * r;
        p.setAttribute('cx', x);
        p.setAttribute('cy', y);
      });

      cometTimer += dt;
      if (cometTimer > 7) {
        cometTimer = 0;
        flyComet(svg);
      }

      orbitRaf = requestAnimationFrame(frame);
    }
    cancelAnimationFrame(orbitRaf);
    orbitRaf = requestAnimationFrame(frame);
  }

  function flyComet(svg) {
    const comet = svg.querySelector('#cometPath');
    if (!comet || !window.gsap) return;
    const x1 = -20, y1 = 40 + Math.random() * 200;
    const x2 = 420, y2 = y1 + 40 + Math.random() * 80;
    comet.setAttribute('d', `M${x1} ${y1} L${x2} ${y2}`);
    const len = comet.getTotalLength();
    comet.style.strokeDasharray = `40 ${len}`;
    comet.style.strokeDashoffset = len;
    gsap.fromTo(comet,
      { attr: { opacity: 0 }, strokeDashoffset: len },
      { attr: { opacity: 0.7 }, strokeDashoffset: -40, duration: 1.1, ease: 'power2.in',
        onComplete: () => gsap.to(comet, { attr: { opacity: 0 }, duration: 0.2 }) }
    );
  }

  /* ---------- Category tilt ---------- */
  function enhanceCatCards(root) {
    root.querySelectorAll('.cat-card').forEach((card) => {
      if (card.dataset.ssCat) return;
      card.dataset.ssCat = '1';
      if (!card.querySelector('.glow-follow')) {
        const g = document.createElement('div');
        g.className = 'glow-follow';
        card.prepend(g);
      }
      const glow = card.querySelector('.glow-follow');
      card.addEventListener('pointermove', (e) => {
        if (REDUCE || !FINE) return;
        const r = card.getBoundingClientRect();
        const x = e.clientX - r.left;
        const y = e.clientY - r.top;
        const rx = ((y / r.height) - 0.5) * -10;
        const ry = ((x / r.width) - 0.5) * 12;
        card.style.transform = `perspective(700px) rotateX(${rx}deg) rotateY(${ry}deg) translateY(-6px)`;
        if (glow) {
          glow.style.left = x + 'px';
          glow.style.top = y + 'px';
        }
      });
      card.addEventListener('pointerleave', () => {
        card.style.transform = '';
      });
    });
  }

  /* ---------- Home cinematic timeline ---------- */
  window.ssInitHomeMotion = function () {
    homeCleanup.forEach((fn) => fn());
    homeCleanup = [];
    cancelAnimationFrame(orbitRaf);

    const root = document.querySelector('.home-landing');
    if (!root) return;

    const title = root.querySelector('.hero-title');
    if (title) splitTitle(title);

    const orbitHost = root.querySelector('.hero-orbit-host');
    if (orbitHost) {
      orbitHost.innerHTML = buildOrbitSVG() + '<div class="hero-bloom" id="heroBloom"></div>';
      const svg = orbitHost.querySelector('#heroOrbitSvg');
      startOrbit(svg);
      homeCleanup.push(() => cancelAnimationFrame(orbitRaf));
    }

    const hero = root.querySelector('.hero');
    hero?.addEventListener('pointerenter', () => { orbitPaused = true; });
    hero?.addEventListener('pointerleave', () => { orbitPaused = false; });

    window.ssEnhanceButtons(root);
    enhanceCatCards(root);

    if (REDUCE || !window.gsap) {
      root.querySelectorAll('.char, .hero-eyebrow, .hero-sub, .hero-actions a, .cat-card, .section-head, .pcard, .hero-bloom')
        .forEach((el) => { el.style.opacity = '1'; el.style.transform = 'none'; });
      return;
    }

    whenGsap((gsap) => {
      const chars = root.querySelectorAll('.hero-title .char');
      const tl = gsap.timeline({ defaults: { ease: expoOut } });

      gsap.set(chars, { y: '110%', opacity: 0 });
      gsap.set(root.querySelector('.hero-eyebrow'), { y: 20, opacity: 0 });
      gsap.set(root.querySelector('.hero-sub'), { y: 24, opacity: 0 });
      gsap.set(root.querySelectorAll('.hero-actions a'), { y: 18, scale: 0.94, opacity: 0 });
      gsap.set(root.querySelectorAll('.cat-card'), { y: 28, opacity: 0 });
      gsap.set(root.querySelector('#heroBloom'), { opacity: 0, scale: 0.8 });

      tl.to(root.querySelector('#heroBloom'), { opacity: 1, scale: 1, duration: 1.1, ease: 'power2.out' }, 0);
      tl.to(root.querySelector('.hero-eyebrow'), { y: 0, opacity: 1, duration: 0.55 }, 0.1);
      tl.to(chars, {
        y: 0,
        opacity: 1,
        duration: 0.75,
        stagger: 0.028,
        ease: 'back.out(1.6)',
      }, 0.2);
      // micro overshoot settle
      tl.to(chars, { y: 0, duration: 0.35, stagger: 0.01, ease: 'power3.out' }, 0.85);
      tl.to(root.querySelector('.hero-sub'), { y: 0, opacity: 1, duration: 0.65 }, 0.55);
      tl.to(root.querySelectorAll('.hero-actions a'), {
        y: 0, scale: 1, opacity: 1, duration: 0.55, stagger: 0.1, ease: 'back.out(1.7)'
      }, 0.7);
      tl.to(root.querySelectorAll('.cat-card'), {
        y: 0, opacity: 1, duration: 0.6, stagger: 0.08, ease: 'power3.out'
      }, 0.85);

      // scroll reveals for sections / cards
      if (window.ScrollTrigger) {
        gsap.registerPlugin(ScrollTrigger);
        root.querySelectorAll('.section-head, .grid .pcard').forEach((el, i) => {
          gsap.from(el, {
            scrollTrigger: { trigger: el, start: 'top 88%', once: true },
            y: 40,
            opacity: 0,
            duration: 0.7,
            delay: (i % 4) * 0.06,
            ease: expoOut
          });
        });
      } else {
        const io = new IntersectionObserver((entries) => {
          entries.forEach((en) => {
            if (en.isIntersecting) {
              en.target.classList.add('in');
              gsap.to(en.target, { y: 0, opacity: 1, duration: 0.7, ease: expoOut });
              io.unobserve(en.target);
            }
          });
        }, { threshold: 0.15 });
        root.querySelectorAll('.section-head, .grid .pcard').forEach((el) => {
          el.classList.add('ss-reveal');
          io.observe(el);
        });
        homeCleanup.push(() => io.disconnect());
      }
    });
  };

  /* ---------- Public micro-helpers ---------- */
  window.ssCartPulse = function () {
    const bag = document.querySelector('a.icon-btn[title="Bag"]');
    const badge = bag?.querySelector('.badge-count');
    badge?.classList.remove('ss-bounce');
    void badge?.offsetWidth;
    badge?.classList.add('ss-bounce');
    bag?.classList.remove('ss-cart-tilt');
    void bag?.offsetWidth;
    bag?.classList.add('ss-cart-tilt');
  };

  window.ssWishPulse = function (btn) {
    btn?.classList.add('ss-heart-fill');
    setTimeout(() => btn?.classList.remove('ss-heart-fill'), 600);
  };

  window.ssToastMorph = function () {
    const toast = document.getElementById('toast');
    if (toast && window.gsap && !REDUCE) {
      gsap.fromTo(toast, { scale: 0.9, y: 20 }, { scale: 1, y: 0, duration: 0.5, ease: 'back.out(1.6)' });
    }
  };

  /* ---------- Boot ---------- */
  function boot() {
    ensureChrome();
    runIntro();
    initPointerFx();
    initParticles();
    initScrollChrome();
    window.ssEnhanceButtons(document);

    // magnetic nav links
    document.querySelectorAll('nav.links a').forEach((a) => {
      a.addEventListener('pointermove', (e) => {
        if (REDUCE || !FINE) return;
        const r = a.getBoundingClientRect();
        const x = (e.clientX - r.left - r.width / 2) * 0.2;
        const y = (e.clientY - r.top - r.height / 2) * 0.25;
        a.style.transform = `translate(${x}px, ${y}px)`;
      });
      a.addEventListener('pointerleave', () => { a.style.transform = ''; });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();

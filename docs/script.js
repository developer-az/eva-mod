(() => {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const progressBar = document.getElementById("progress-bar");
  const nav = document.getElementById("mini-nav");
  const navLinks = [...document.querySelectorAll("[data-nav]")];
  const sections = ["versions", "install"]
    .map((id) => document.getElementById(id))
    .filter(Boolean);

  // Soft sticky nav after leaving the hero
  const onScrollChrome = () => {
    const y = window.scrollY || document.documentElement.scrollTop;
    if (nav) nav.classList.toggle("is-scrolled", y > 48);

    if (progressBar) {
      const doc = document.documentElement;
      const max = Math.max(1, doc.scrollHeight - doc.clientHeight);
      const pct = Math.min(1, Math.max(0, y / max));
      progressBar.style.transform = `scaleX(${pct})`;
    }
  };

  onScrollChrome();
  window.addEventListener("scroll", onScrollChrome, { passive: true });

  // Smooth in-page anchors with sticky-nav offset (CSS handles most browsers)
  document.querySelectorAll('a[href^="#"]').forEach((link) => {
    link.addEventListener("click", (event) => {
      const id = link.getAttribute("href");
      if (!id || id === "#") return;
      const target = document.querySelector(id);
      if (!target) return;

      event.preventDefault();
      target.scrollIntoView({
        behavior: reduceMotion ? "auto" : "smooth",
        block: "start",
      });

      if (history.replaceState) {
        history.replaceState(null, "", id);
      }
    });
  });

  // Active mini-nav highlight
  if ("IntersectionObserver" in window && sections.length) {
    const sectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          const id = entry.target.id;
          navLinks.forEach((a) => {
            a.classList.toggle("is-active", a.dataset.nav === id);
          });
        });
      },
      { rootMargin: "-35% 0px -50% 0px", threshold: 0.01 }
    );
    sections.forEach((section) => sectionObserver.observe(section));
  }

  // Section reveals
  const reveals = document.querySelectorAll(".reveal");
  if (reduceMotion) {
    reveals.forEach((el) => el.classList.add("is-visible"));
  } else if ("IntersectionObserver" in window) {
    const revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          entry.target.classList.add("is-visible");
          revealObserver.unobserve(entry.target);
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -6% 0px" }
    );
    reveals.forEach((el) => revealObserver.observe(el));
  } else {
    reveals.forEach((el) => el.classList.add("is-visible"));
  }

  // Soft press on download buttons
  document.querySelectorAll(".btn-download, .btn-primary").forEach((btn) => {
    btn.addEventListener("click", () => {
      btn.classList.add("is-pressed");
      window.setTimeout(() => btn.classList.remove("is-pressed"), 220);
    });
  });
})();

(() => {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const topbar = document.getElementById("topbar");
  const navLinks = [...document.querySelectorAll("[data-nav]")];
  const sections = ["archive", "install"]
    .map((id) => document.getElementById(id))
    .filter(Boolean);

  const onScroll = () => {
    if (!topbar) return;
    // Switch to solid bar once the full-bleed hero scrolls away.
    const threshold = Math.max(80, window.innerHeight * 0.55);
    topbar.classList.toggle("is-scrolled", (window.scrollY || 0) > threshold);
  };
  onScroll();
  window.addEventListener("scroll", onScroll, { passive: true });
  window.addEventListener("resize", onScroll, { passive: true });

  document.querySelectorAll('a[href^="#"]').forEach((link) => {
    link.addEventListener("click", (event) => {
      const id = link.getAttribute("href");
      const target = id && document.querySelector(id);
      if (!target) return;
      event.preventDefault();
      target.scrollIntoView({ behavior: reduceMotion ? "auto" : "smooth", block: "start" });
      if (history.replaceState) history.replaceState(null, "", id);
    });
  });

  if ("IntersectionObserver" in window) {
    if (sections.length) {
      const sectionObserver = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (!entry.isIntersecting) return;
            navLinks.forEach((a) =>
              a.classList.toggle("is-active", a.dataset.nav === entry.target.id)
            );
          });
        },
        { rootMargin: "-30% 0px -55% 0px", threshold: 0.01 }
      );
      sections.forEach((section) => sectionObserver.observe(section));
    }

    const reveals = document.querySelectorAll(".reveal");
    if (reduceMotion) {
      reveals.forEach((el) => el.classList.add("is-visible"));
    } else {
      const revealObserver = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (!entry.isIntersecting) return;
            entry.target.classList.add("is-visible");
            revealObserver.unobserve(entry.target);
          });
        },
        { threshold: 0.1, rootMargin: "0px 0px -8% 0px" }
      );
      reveals.forEach((el) => revealObserver.observe(el));
    }
  } else {
    document.querySelectorAll(".reveal").forEach((el) => el.classList.add("is-visible"));
  }
})();

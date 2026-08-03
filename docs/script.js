(() => {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // Gentle reveal when version / install sections enter the viewport
  if (!reduceMotion && "IntersectionObserver" in window) {
    const targets = document.querySelectorAll(".version-item, .steps li, .section-head");
    targets.forEach((el) => {
      el.style.opacity = "0";
    });

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          entry.target.style.opacity = "";
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.18, rootMargin: "0px 0px -8% 0px" }
    );

    targets.forEach((el) => observer.observe(el));
  }

  // Soft press feedback on download buttons
  document.querySelectorAll(".btn-download").forEach((btn) => {
    btn.addEventListener("click", () => {
      btn.classList.add("is-pressed");
      window.setTimeout(() => btn.classList.remove("is-pressed"), 280);
    });
  });
})();

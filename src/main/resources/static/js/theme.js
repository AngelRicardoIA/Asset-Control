const themeToggle = document.querySelector("[data-theme-toggle]");
const themeLabel = document.querySelector("[data-theme-label]");
const storedTheme = localStorage.getItem("asset-control-theme");
const systemTheme = "light";

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("asset-control-theme", theme);
    themeToggle?.setAttribute("aria-pressed", String(theme === "dark"));
    themeToggle?.setAttribute("aria-label", theme === "dark" ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    if (themeLabel) {
        themeLabel.textContent = theme === "dark" ? "Modo claro" : "Modo oscuro";
    }
}

applyTheme(storedTheme ?? systemTheme);

themeToggle?.addEventListener("click", () => {
    const currentTheme = document.documentElement.dataset.theme;
    applyTheme(currentTheme === "dark" ? "light" : "dark");
});

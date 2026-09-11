const themeToggle = document.querySelector("[data-theme-toggle]");
const storedTheme = localStorage.getItem("asset-control-theme");
const systemTheme = "light";

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("asset-control-theme", theme);
    themeToggle?.setAttribute("aria-pressed", String(theme === "dark"));
}

applyTheme(storedTheme ?? systemTheme);

themeToggle?.addEventListener("click", () => {
    const currentTheme = document.documentElement.dataset.theme;
    applyTheme(currentTheme === "dark" ? "light" : "dark");
});
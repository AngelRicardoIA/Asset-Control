const menuTrigger = document.querySelector("[data-menu-open]");
const menuPanel = document.querySelector("[data-menu-panel]");
const menuBackdrop = document.querySelector("[data-menu-backdrop]");
const menuClose = document.querySelector("[data-menu-close]");

if (menuTrigger && menuPanel && menuBackdrop && menuClose) {
    const links = menuPanel.querySelectorAll("[data-menu-link]");
    const pathname = window.location.pathname.replace(/\/$/, "") || "/";

    links.forEach((link) => {
        const destination = new URL(link.href).pathname.replace(/\/$/, "") || "/";
        if (pathname === destination || (destination !== "/" && pathname.startsWith(destination + "/"))) {
            link.setAttribute("aria-current", "page");
        }
    });

    function setMenuOpen(open) {
        if (!open && menuPanel.contains(document.activeElement)) {
            menuTrigger.focus();
        }
        menuPanel.inert = !open;
        menuPanel.setAttribute("aria-hidden", String(!open));
        menuPanel.dataset.open = String(open);
        menuBackdrop.hidden = !open;
        menuTrigger.setAttribute("aria-expanded", String(open));
        document.body.classList.toggle("menu-open", open);
        if (open) {
            menuClose.focus();
        }
    }

    menuTrigger.addEventListener("click", () => setMenuOpen(true));
    menuClose.addEventListener("click", () => setMenuOpen(false));
    menuBackdrop.addEventListener("click", () => setMenuOpen(false));

    document.addEventListener("keydown", (event) => {
        if (menuPanel.dataset.open !== "true") {
            return;
        }
        if (event.key === "Escape") {
            setMenuOpen(false);
        }
        if (event.key === "Tab") {
            const focusable = [...menuPanel.querySelectorAll("a[href], button:not(:disabled)")];
            const first = focusable[0];
            const last = focusable[focusable.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        }
    });
}

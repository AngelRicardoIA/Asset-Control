const pageSize = 3;

for (const list of document.querySelectorAll("[data-history-list]")) {
    const items = [...list.children].filter((item) => item.hasAttribute("data-history-item"));
    if (items.length <= pageSize) {
        continue;
    }

    const totalPages = Math.ceil(items.length / pageSize);
    const targetId = window.location.hash.slice(1);
    const targetIndex = items.findIndex((item) => item.id === targetId);
    let currentPage = targetIndex < 0 ? 0 : Math.floor(targetIndex / pageSize);

    const footer = document.createElement("nav");
    footer.className = "history-pages";
    footer.setAttribute("aria-label", list.dataset.historyLabel || "Páginas del historial");
    list.insertAdjacentElement("afterend", footer);

    function button(label, destination, description, current = false) {
        const control = document.createElement("button");
        control.type = "button";
        control.className = "pagination__link";
        control.textContent = label;
        control.setAttribute("aria-label", description);
        control.disabled = destination < 0 || destination >= totalPages;
        if (current) {
            control.classList.add("pagination__link--current");
            control.setAttribute("aria-current", "page");
        }
        control.addEventListener("click", () => {
            currentPage = destination;
            render();
        });
        return control;
    }

    function render() {
        items.forEach((item, index) => {
            item.hidden = Math.floor(index / pageSize) !== currentPage;
        });
        footer.replaceChildren();
        const summary = document.createElement("span");
        summary.className = "history-pages__summary";
        summary.setAttribute("aria-live", "polite");
        const first = currentPage * pageSize + 1;
        const last = Math.min((currentPage + 1) * pageSize, items.length);
        summary.textContent = `${first}–${last} de ${items.length}`;
        footer.append(summary, button("‹", currentPage - 1, "Página anterior"));
        for (let number = Math.max(0, currentPage - 2); number <= Math.min(totalPages - 1, currentPage + 2); number++) {
            footer.append(button(String(number + 1), number, `Página ${number + 1}`, number === currentPage));
        }
        footer.append(button("›", currentPage + 1, "Página siguiente"));
    }

    render();
    if (targetIndex >= 0) {
        requestAnimationFrame(() => items[targetIndex].scrollIntoView({block: "center"}));
    }
}

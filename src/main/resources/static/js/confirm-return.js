document.addEventListener("submit", (event) => {
    const form = event.target;

    if (!(form instanceof HTMLFormElement)
            || !form.hasAttribute("data-confirm-return")) {
        return;
    }

    if (!window.confirm(form.dataset.confirmReturn)) {
        event.preventDefault();
    }
});
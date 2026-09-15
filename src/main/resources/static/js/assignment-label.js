(() => {
    const dialog = document.querySelector("#assignment-label-dialog");
    if (!dialog) return;

    const form = dialog.querySelector("[data-label-form]");
    const fields = dialog.querySelector("[data-label-fields]");
    const message = dialog.querySelector("[data-label-message]");
    const submitButton = dialog.querySelector("[data-label-submit]");
    const closeButtons = dialog.querySelectorAll("[data-label-close]");
    const contentFields = ["fullName", "asset", "model", "serialNumber", "host"];
    let endpoint = "";
    let opener = null;
    let loadingRequest = null;
    let printing = false;
    let sent = false;

    function showMessage(text, state = "info") {
        message.textContent = text;
        message.dataset.state = state;
        message.hidden = !text;
    }

    async function readResponse(response) {
        if (!response.headers.get("content-type")?.includes("application/json")) {
            throw new Error("La sesión o la conexión cambió. Actualiza la página antes de continuar.");
        }
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || "No se pudo completar la solicitud.");
        }
        return data;
    }

    async function openLabel(button) {
        if (dialog.open) return;
        opener = button;
        endpoint = button.dataset.labelUrl;
        form.reset();
        fields.querySelectorAll("input").forEach(input => input.setCustomValidity(""));
        sent = false;
        fields.disabled = true;
        submitButton.disabled = true;
        submitButton.textContent = "Imprimir etiqueta";
        showMessage("Cargando datos de la asignación…");
        dialog.showModal();
        const request = new AbortController();
        loadingRequest = request;
        try {
            const response = await fetch(endpoint, {
                headers: {Accept: "application/json"},
                credentials: "same-origin",
                cache: "no-store",
                signal: AbortSignal.any([request.signal, AbortSignal.timeout(15000)])
            });
            const data = await readResponse(response);
            if (request.signal.aborted) return;
            form.elements.namedItem("printerIp").value = data.printerIp || "";
            contentFields.forEach(name => {
                form.elements.namedItem(name).value = data.content[name] || "";
            });
            fields.disabled = false;
            submitButton.disabled = false;
            showMessage("");
            form.elements.namedItem("printerIp").focus();
        } catch (error) {
            if (request.signal.aborted) return;
            showMessage(error instanceof TypeError || error.name === "TimeoutError"
                ? "No se pudieron cargar los datos. Revisa la conexión." : error.message, "error");
        }
    }

    document.addEventListener("click", event => {
        const button = event.target.closest("[data-label-open]");
        if (button) openLabel(button);
    });

    closeButtons.forEach(button => button.addEventListener("click", () => {
        if (!printing) dialog.close();
    }));

    dialog.addEventListener("cancel", event => {
        if (printing) event.preventDefault();
    });

    dialog.addEventListener("close", () => {
        loadingRequest?.abort();
        opener?.focus();
    });

    form.addEventListener("submit", async event => {
        event.preventDefault();
        if (printing || sent || fields.disabled) return;

        for (const input of fields.querySelectorAll("input")) {
            input.value = input.value.trim();
            input.setCustomValidity(input.value.length > input.maxLength
                ? `Este dato admite hasta ${input.maxLength} caracteres en la etiqueta.` : "");
        }
        if (!form.reportValidity()) return;

        const content = Object.fromEntries(contentFields.map(name => [name, form.elements.namedItem(name).value]));
        const body = {printerIp: form.elements.namedItem("printerIp").value, content};
        const headers = {"Content-Type": "application/json", Accept: "application/json"};
        const csrf = form.querySelector("[data-csrf-header]");
        if (csrf) headers[csrf.dataset.csrfHeader] = csrf.value;

        printing = true;
        fields.disabled = true;
        submitButton.disabled = true;
        closeButtons.forEach(button => { button.disabled = true; });
        submitButton.textContent = "Enviando…";
        showMessage("Enviando etiqueta a la impresora…");

        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers,
                credentials: "same-origin",
                body: JSON.stringify(body),
                signal: AbortSignal.timeout(45000)
            });
            const data = await readResponse(response);
            sent = true;
            showMessage(data.message, "success");
        } catch (error) {
            showMessage(error instanceof TypeError || error.name === "TimeoutError"
                ? "No se pudo confirmar el envío. Revisa la impresora antes de volver a imprimir."
                : error.message, "error");
        } finally {
            printing = false;
            fields.disabled = sent;
            submitButton.disabled = sent;
            submitButton.textContent = sent ? "Etiqueta enviada" : "Imprimir etiqueta";
            closeButtons.forEach(button => { button.disabled = false; });
        }
    });

    fields.addEventListener("input", event => event.target.setCustomValidity(""));
})();

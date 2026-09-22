document.addEventListener("DOMContentLoaded", () => {
    const returnDialog = document.querySelector("#asset-return-dialog");
    const detailDialog = document.querySelector("#return-detail-dialog");

    document.addEventListener("click", (event) => {
        const returnTrigger = event.target.closest("[data-return-open]");

        if (returnTrigger && returnDialog) {
            const form = returnDialog.querySelector("[data-return-form]");
            const dateInput = returnDialog.querySelector("[data-return-date]");
            const localDate = new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
                    .toISOString()
                    .slice(0, 10);

            form.action = returnTrigger.dataset.returnAction;
            form.reset();
            dateInput.value = localDate;
            returnDialog.querySelector("[data-return-asset]").textContent =
                    returnTrigger.dataset.returnAsset;
            returnDialog.querySelector("[data-return-person]").textContent =
                    returnTrigger.dataset.returnPerson;
            returnDialog.showModal();
            return;
        }

        const detailTrigger = event.target.closest("[data-return-details]");

        if (detailTrigger && detailDialog) {
            detailDialog.querySelector("[data-return-detail-asset]").textContent =
                    detailTrigger.dataset.returnAsset;
            detailDialog.querySelector("[data-return-detail-person]").textContent =
                    detailTrigger.dataset.returnPerson;
            detailDialog.querySelector("[data-return-detail-date]").textContent =
                    detailTrigger.dataset.returnDate;
            detailDialog.querySelector("[data-return-detail-received-by]").textContent =
                    detailTrigger.dataset.returnReceivedBy || "No registrado";
            detailDialog.querySelector("[data-return-detail-notes]").textContent =
                    detailTrigger.dataset.returnNotes || "Sin comentarios.";
            detailDialog.showModal();
        }
    });

    document.querySelectorAll("[data-return-close]").forEach((button) => {
        button.addEventListener("click", () => returnDialog?.close());
    });

    document.querySelectorAll("[data-return-detail-close]").forEach((button) => {
        button.addEventListener("click", () => detailDialog?.close());
    });

    [returnDialog, detailDialog].forEach((dialog) => {
        dialog?.addEventListener("click", (event) => {
            if (event.target === dialog) {
                dialog.close();
            }
        });
    });
});

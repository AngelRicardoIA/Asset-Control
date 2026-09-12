document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-assignment-form]").forEach((form) => {
        const assignmentType = form.querySelector("[data-assignment-type]");
        const dueDate = form.querySelector("[data-due-date]");
        const dueDateField = form.querySelector("[data-due-date-field]");

        if (!assignmentType || !dueDate || !dueDateField) {
            return;
        }

        const updateDueDateField = () => {
            const isLoan = assignmentType.value === "LOAN";

            dueDateField.classList.toggle("is-hidden", !isLoan);
            dueDate.disabled = !isLoan;
            dueDate.required = isLoan;

            if (!isLoan) {
                dueDate.value = "";
            }
        };

        assignmentType.addEventListener("change", updateDueDateField);

        updateDueDateField();
    });
});
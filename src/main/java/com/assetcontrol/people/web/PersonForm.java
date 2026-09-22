package com.assetcontrol.people.web;

import com.assetcontrol.people.application.CreatePersonCommand;
import com.assetcontrol.people.application.UpdatePersonCommand;
import com.assetcontrol.people.domain.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PersonForm {

    @NotBlank(message = "El número de empleado es obligatorio.")
    @Size(max = 100, message = "El número de empleado no puede superar 100 caracteres.")
    private String externalId;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(max = 100, message = "El nombre de usuario no puede superar 100 caracteres.")
    private String username;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String fullName;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Escribe un correo válido.")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres.")
    private String email;

    @Size(max = 150, message = "El puesto no puede superar 150 caracteres.")
    private String jobTitle;

    @Size(max = 150, message = "El área no puede superar 150 caracteres.")
    private String department;

    @Size(max = 150, message = "El nombre del gerente no puede superar 150 caracteres.")
    private String managerName;

    public static PersonForm from(Person person) {
        PersonForm form = new PersonForm();
        form.setExternalId(person.getExternalId());
        form.setUsername(person.getUsername());
        form.setFullName(person.getFullName());
        form.setEmail(person.getEmail());
        form.setJobTitle(person.getJobTitle());
        form.setDepartment(person.getDepartment());
        form.setManagerName(person.getManagerName());
        return form;
    }

    public CreatePersonCommand toCreateCommand() {
        return new CreatePersonCommand(
                externalId,
                username,
                fullName,
                email,
                jobTitle,
                department,
                managerName
        );
    }

    public UpdatePersonCommand toUpdateCommand() {
        return new UpdatePersonCommand(
                externalId,
                username,
                fullName,
                email,
                jobTitle,
                department,
                managerName
        );
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}

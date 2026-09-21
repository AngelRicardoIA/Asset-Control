package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.RegisterPhoneCommand;
import com.assetcontrol.phones.application.UpdatePhoneCommand;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PhoneForm {

    @NotBlank(message = "El IMEI es obligatorio.")
    @Size(max = 32, message = "El IMEI no puede superar 32 caracteres.")
    private String imei;

    @NotBlank(message = "La marca es obligatoria.")
    @Size(max = 100, message = "La marca no puede superar 100 caracteres.")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 100, message = "El modelo no puede superar 100 caracteres.")
    private String model;

    private Long siteId;

    @Size(max = 100, message = "La ubicación no puede superar 100 caracteres.")
    private String newSiteName;

    private Long phoneLineId;

    @Size(max = 30, message = "El número no puede superar 30 caracteres.")
    private String newLineNumber;

    @Size(max = 100, message = "El proveedor no puede superar 100 caracteres.")
    private String newLineCarrier;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.")
    private String observations;

    private PhoneStatus status = PhoneStatus.AVAILABLE;

    @Valid
    private PhoneAssignmentForm initialAssignment = new PhoneAssignmentForm();

    public static PhoneForm from(Phone phone) {
        PhoneForm form = new PhoneForm();
        form.imei = phone.getImei();
        form.brand = phone.getBrand();
        form.model = phone.getModel();
        form.siteId = phone.getSite().getId();
        form.phoneLineId = phone.getPhoneLine() == null
                ? null
                : phone.getPhoneLine().getId();
        form.observations = phone.getObservations();
        form.status = phone.getStatus();
        return form;
    }

    public RegisterPhoneCommand toRegisterCommand() {
        return new RegisterPhoneCommand(
                imei,
                brand,
                model,
                siteId,
                newSiteName,
                phoneLineId,
                newLineNumber,
                newLineCarrier,
                observations
        );
    }

    public UpdatePhoneCommand toUpdateCommand() {
        return new UpdatePhoneCommand(
                imei,
                brand,
                model,
                siteId,
                newSiteName,
                phoneLineId,
                newLineNumber,
                newLineCarrier,
                observations,
                status
        );
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getNewSiteName() {
        return newSiteName;
    }

    public void setNewSiteName(String newSiteName) {
        this.newSiteName = newSiteName;
    }

    public Long getPhoneLineId() {
        return phoneLineId;
    }

    public void setPhoneLineId(Long phoneLineId) {
        this.phoneLineId = phoneLineId;
    }

    public String getNewLineNumber() {
        return newLineNumber;
    }

    public void setNewLineNumber(String newLineNumber) {
        this.newLineNumber = newLineNumber;
    }

    public String getNewLineCarrier() {
        return newLineCarrier;
    }

    public void setNewLineCarrier(String newLineCarrier) {
        this.newLineCarrier = newLineCarrier;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public PhoneStatus getStatus() {
        return status;
    }

    public void setStatus(PhoneStatus status) {
        this.status = status;
    }

    public PhoneAssignmentForm getInitialAssignment() {
        return initialAssignment;
    }

    public void setInitialAssignment(PhoneAssignmentForm initialAssignment) {
        this.initialAssignment = initialAssignment;
    }
}

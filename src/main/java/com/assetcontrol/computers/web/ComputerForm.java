package com.assetcontrol.computers.web;

import com.assetcontrol.computers.application.CreateComputerCommand;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComputerForm {

    @NotBlank(message = "El asset es obligatorio.")
    @Size(max = 100, message = "El asset no puede superar 100 caracteres.")
    private String asset;

    @NotBlank(message = "El hostname es obligatorio.")
    @Size(max = 100, message = "El hostname no puede superar 100 caracteres.")
    private String host;

    @NotNull(message = "Selecciona el tipo de equipo.")
    private ComputerType type;

    @NotBlank(message = "La marca es obligatoria.")
    @Size(max = 100, message = "La marca no puede superar 100 caracteres.")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 150, message = "El modelo no puede superar 150 caracteres.")
    private String model;

    @NotBlank(message = "El número de serie es obligatorio.")
    @Size(max = 150, message = "El número de serie no puede superar 150 caracteres.")
    private String serialNumber;

    @Size(max = 100, message = "El sistema operativo no puede superar 100 caracteres.")
    private String operatingSystem;

    @NotNull(message = "Selecciona un estado.")
    private ComputerStatus status;

    private Long siteId;

    @Size(max = 100, message = "La ubicación no puede superar 100 caracteres.")
    private String newSiteName;

    @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres.")
    private String observations;

    public CreateComputerCommand toCommand() {
        return new CreateComputerCommand(
                asset,
                host,
                type,
                brand,
                model,
                serialNumber,
                operatingSystem,
                status,
                siteId,
                newSiteName,
                observations
        );
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ComputerType getType() {
        return type;
    }

    public void setType(ComputerType type) {
        this.type = type;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public ComputerStatus getStatus() {
        return status;
    }

    public void setStatus(ComputerStatus status) {
        this.status = status;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @AssertTrue(message = "Selecciona una ubicación o agrega una nueva.")
    public boolean isSiteProvided() {
        return siteId != null || (newSiteName != null && !newSiteName.isBlank());
    }

    public String getNewSiteName() {
        return newSiteName;
    }

    public void setNewSiteName(String newSiteName) {
        this.newSiteName = newSiteName;
    }
}
package com.assetcontrol.accessories.web;

import com.assetcontrol.accessories.domain.Accessory;
import com.assetcontrol.accessories.domain.AccessoryType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccessoryForm {
    @NotNull private AccessoryType type;
    @NotBlank @Size(max = 100) private String brand;
    @NotBlank @Size(max = 150) private String model;
    @Size(max = 150) private String serialNumber;
    @Size(max = 100) private String asset;
    @NotNull @Min(1) @Max(200) private Integer quantity = 1;

    public static AccessoryForm from(Accessory accessory) {
        var form = new AccessoryForm();
        form.setType(accessory.getType());
        form.setBrand(accessory.getBrand());
        form.setModel(accessory.getModel());
        form.setSerialNumber(accessory.getSerialNumber());
        form.setAsset(accessory.getAsset());
        return form;
    }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public AccessoryType getType() { return type; }
    public void setType(AccessoryType value) { type = value; }
    public String getBrand() { return brand; }
    public void setBrand(String value) { brand = value; }
    public String getModel() { return model; }
    public void setModel(String value) { model = value; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String value) { serialNumber = value; }
    public String getAsset() { return asset; }
    public void setAsset(String value) { asset = value; }
}

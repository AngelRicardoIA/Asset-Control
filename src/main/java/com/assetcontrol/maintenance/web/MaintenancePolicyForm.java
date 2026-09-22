package com.assetcontrol.maintenance.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MaintenancePolicyForm {
    @NotNull @Min(1) @Max(60)
    private Integer intervalMonths;
    @NotNull @Min(0) @Max(180)
    private Integer warningDays;
    public Integer getIntervalMonths() { return intervalMonths; }
    public void setIntervalMonths(Integer value) { intervalMonths = value; }
    public Integer getWarningDays() { return warningDays; }
    public void setWarningDays(Integer value) { warningDays = value; }
}

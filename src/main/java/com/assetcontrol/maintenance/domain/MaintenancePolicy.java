package com.assetcontrol.maintenance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "maintenance_policy")
public class MaintenancePolicy {

    @Id
    private Long id;

    @Column(name = "interval_months", nullable = false)
    private int intervalMonths;

    @Column(name = "warning_days", nullable = false)
    private int warningDays;

    protected MaintenancePolicy() {
    }

    public int getIntervalMonths() {
        return intervalMonths;
    }

    public int getWarningDays() {
        return warningDays;
    }

    public void update(int intervalMonths, int warningDays) {
        if (intervalMonths < 1 || intervalMonths > 60 || warningDays < 0 || warningDays > 180) {
            throw new IllegalArgumentException("El intervalo o el aviso están fuera de rango.");
        }
        this.intervalMonths = intervalMonths;
        this.warningDays = warningDays;
    }
}

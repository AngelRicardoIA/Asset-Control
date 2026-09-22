package com.assetcontrol.accessories.application;

import com.assetcontrol.accessories.domain.Accessory;
import com.assetcontrol.accessories.domain.AccessoryAssignment;

public record AccessorySnapshot(Accessory accessory, AccessoryAssignment assignment) {
    public boolean assigned() { return assignment != null; }
}

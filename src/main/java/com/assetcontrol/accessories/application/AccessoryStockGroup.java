package com.assetcontrol.accessories.application;

import com.assetcontrol.accessories.domain.AccessoryType;

public record AccessoryStockGroup(AccessoryType type, String brand, long quantity) {}

package com.assetcontrol.accessories.application;

import java.util.List;

public record AccessoryInventory(List<AccessorySnapshot> items, List<AccessoryStockGroup> stock,
                                 long stockCount, long assignedCount) {}

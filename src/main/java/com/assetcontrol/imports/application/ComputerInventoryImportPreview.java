package com.assetcontrol.imports.application;

import java.util.List;

public record ComputerInventoryImportPreview(
        List<ComputerInventoryImportRow> rows
) {
}

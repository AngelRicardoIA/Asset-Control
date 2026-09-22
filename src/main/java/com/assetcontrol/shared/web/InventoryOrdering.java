package com.assetcontrol.shared.web;

import java.util.Comparator;
import java.util.List;

public final class InventoryOrdering {

    private InventoryOrdering() {
    }

    public static <T> List<T> apply(
            List<T> values,
            Comparator<T> alphabeticalComparator,
            Comparator<T> chronologicalComparator,
            InventorySort sort,
            InventorySortDirection direction
    ) {
        Comparator<T> comparator = sort == InventorySort.CHRONOLOGICAL
                ? chronologicalComparator
                : alphabeticalComparator;

        if (direction == InventorySortDirection.DESCENDING) {
            comparator = comparator.reversed();
        }

        return values.stream().sorted(comparator).toList();
    }
}

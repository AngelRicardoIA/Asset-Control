package com.assetcontrol.shared.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public final class InventoryPagination {

    private final Page<?> page;

    public InventoryPagination(Page<?> page) {
        this.page = page;
    }

    public static <T> Page<T> load(int requestedPage, IntFunction<Page<T>> fetch) {
        Page<T> result = fetch.apply(Math.max(1, requestedPage) - 1);

        if (result.getTotalPages() > 0 && result.getNumber() >= result.getTotalPages()) {
            return fetch.apply(result.getTotalPages() - 1);
        }

        return result;
    }

    public long getTotalItems() {
        return page.getTotalElements();
    }

    public long getFirstItem() {
        return page.isEmpty() ? 0 : (long) page.getNumber() * page.getSize() + 1;
    }

    public long getLastItem() {
        return getFirstItem() == 0 ? 0 : getFirstItem() + page.getNumberOfElements() - 1;
    }

    public int getCurrentPage() {
        return page.getNumber() + 1;
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public boolean getHasPrevious() {
        return page.hasPrevious();
    }

    public boolean getHasNext() {
        return page.hasNext();
    }

    public List<Integer> getVisiblePages() {
        if (getTotalPages() == 0) {
            return List.of();
        }

        int start = Math.max(1, Math.min(getCurrentPage() - 2, getTotalPages() - 4));
        int end = Math.min(getTotalPages(), start + 4);
        return IntStream.rangeClosed(start, end).boxed().toList();
    }

    public boolean isFirstPageOutsideWindow() {
        return !getVisiblePages().isEmpty() && getVisiblePages().get(0) > 1;
    }

    public boolean isLastPageOutsideWindow() {
        return !getVisiblePages().isEmpty()
                && getVisiblePages().get(getVisiblePages().size() - 1) < getTotalPages();
    }

    public boolean isLeadingGap() {
        return isFirstPageOutsideWindow() && getVisiblePages().get(0) > 2;
    }

    public boolean isTrailingGap() {
        return isLastPageOutsideWindow()
                && getVisiblePages().get(getVisiblePages().size() - 1) < getTotalPages() - 1;
    }
}

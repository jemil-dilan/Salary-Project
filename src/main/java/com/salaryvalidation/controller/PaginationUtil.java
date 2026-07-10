package com.salaryvalidation.controller;

import com.salaryvalidation.infrastructure.api.model.Pagination;
import org.springframework.data.domain.Page;

public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pagination toPagination(Page<?> page) {
        Pagination pagination = new Pagination();
        pagination.setPage(page.getNumber() + 1);
        pagination.setSize(page.getSize());
        pagination.setTotalElements((int) page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        pagination.setIsFirst(page.isFirst());
        pagination.setIsLast(page.isLast());
        return pagination;
    }
}

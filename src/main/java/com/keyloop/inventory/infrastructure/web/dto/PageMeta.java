package com.keyloop.inventory.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Pagination metadata for paginated responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private Instant timestamp;

    public static PageMeta of(int page, int size, long totalElements, int totalPages) {
        return PageMeta.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .timestamp(Instant.now())
                .build();
    }
}

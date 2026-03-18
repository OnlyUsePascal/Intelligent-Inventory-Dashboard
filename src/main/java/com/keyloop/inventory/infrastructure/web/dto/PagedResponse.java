package com.keyloop.inventory.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated API response wrapper with data array and pagination metadata.
 * 
 * @param <T> the type of items in the data list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private List<T> data;
    private PageMeta meta;

    /**
     * Create a paginated response from domain Page.
     */
    public static <T> PagedResponse<T> of(VehicleRepository.Page<T> page) {
        return PagedResponse.<T>builder()
                .data(page.content())
                .meta(PageMeta.of(page.page(), page.size(), page.totalElements(), page.totalPages()))
                .build();
    }

    /**
     * Create a paginated response from explicit values.
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        return PagedResponse.<T>builder()
                .data(content)
                .meta(PageMeta.of(page, size, totalElements, totalPages))
                .build();
    }
}

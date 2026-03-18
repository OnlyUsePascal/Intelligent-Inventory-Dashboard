package com.keyloop.inventory.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper with data and metadata.
 * 
 * @param <T> the type of data being wrapped
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private Meta meta;

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.now())
                .build();
    }

    /**
     * Metadata for the response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Instant timestamp;

        public static Meta now() {
            return Meta.builder()
                    .timestamp(Instant.now())
                    .build();
        }
    }
}

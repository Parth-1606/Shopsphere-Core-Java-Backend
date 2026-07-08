package com.parth.shopsphere.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        String path,
        int status,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Object errors, int status, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

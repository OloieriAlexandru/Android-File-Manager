package com.example.projectafd.models;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ActionResult {

    private final boolean success;

    @NonNull
    private final String message;

    public static ActionResult ok(String message) {
        return ActionResult.builder()
                .success(true)
                .message(message == null ? "Success" : message)
                .build();
    }

    public static ActionResult error(String message) {
        return ActionResult.builder()
                .success(false)
                .message(message == null ? "Failed":  message)
                .build();
    }
}

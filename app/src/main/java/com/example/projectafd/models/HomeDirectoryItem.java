package com.example.projectafd.models;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class HomeDirectoryItem {

    @NonNull
    private String path;

    @NonNull
    private String name;

    private int iconId;
}

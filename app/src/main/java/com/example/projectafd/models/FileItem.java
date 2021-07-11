package com.example.projectafd.models;

import com.example.projectafd.R;
import com.example.projectafd.models.enums.FileItemType;
import com.example.projectafd.models.interfaces.AbstractFileItemSpecificInfo;

import java.io.File;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class FileItem {

    @NonNull
    private final File file;

    @NonNull
    private final String name;

    @NonNull
    private final FileItemType type;

    @Builder.Default
    private final AbstractFileItemSpecificInfo specificInfo = EmptyFileItemSpecificInfo.get();

    public Instant getLastModified() {
        return Instant.ofEpochMilli(file.lastModified());
    }

    public int getImageResId() {
        switch (type) {
            case DIRECTORY:
                return R.drawable.file_item_directory_icon;
            case TEXT:
                return R.drawable.file_item_text_file_icon;
            default:
                return R.drawable.file_item_normal_file_icon;
        }
    }
}

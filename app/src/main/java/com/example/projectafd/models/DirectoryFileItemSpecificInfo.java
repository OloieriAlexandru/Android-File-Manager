package com.example.projectafd.models;

import com.example.projectafd.models.interfaces.AbstractFileItemSpecificInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DirectoryFileItemSpecificInfo extends AbstractFileItemSpecificInfo {

    private final int itemsCount;

    @Override
    public String getSpecificInfoToDisplay() {
        return String.format("%s items", itemsCount);
    }
}

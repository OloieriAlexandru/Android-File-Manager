package com.example.projectafd.models;

import com.example.projectafd.models.interfaces.AbstractFileItemSpecificInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmptyFileItemSpecificInfo extends AbstractFileItemSpecificInfo {

    public static EmptyFileItemSpecificInfo get() {
        return new EmptyFileItemSpecificInfo();
    }

    @Override
    public String getSpecificInfoToDisplay() {
        return "";
    }
}

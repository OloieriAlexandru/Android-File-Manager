package com.example.projectafd.models;

import android.annotation.SuppressLint;

import com.example.projectafd.models.interfaces.AbstractFileItemSpecificInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NormalFileItemSpecificInfo extends AbstractFileItemSpecificInfo {

    private final long size;

    @SuppressLint("DefaultLocale")
    @Override
    public String getSpecificInfoToDisplay() {
        return String.format("%d bytes", size);
    }
}

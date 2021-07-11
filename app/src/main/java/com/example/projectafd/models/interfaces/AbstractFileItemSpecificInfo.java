package com.example.projectafd.models.interfaces;

import lombok.Data;

@Data
public abstract class AbstractFileItemSpecificInfo {

    public abstract String getSpecificInfoToDisplay();
}

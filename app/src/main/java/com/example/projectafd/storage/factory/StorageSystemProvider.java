package com.example.projectafd.storage.factory;

import com.example.projectafd.storage.abstractions.IStorageSystem;
import com.example.projectafd.storage.implementations.StorageSystem;

public class StorageSystemProvider {

    public static IStorageSystem get(String directory) {
        return new StorageSystem(directory);
    }
}

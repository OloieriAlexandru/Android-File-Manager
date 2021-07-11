package com.example.projectafd.storage.factory;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.projectafd.R;
import com.example.projectafd.models.HomeDirectoryItem;

import java.util.List;

public class HomeDirectoryItemsProvider {

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static List<HomeDirectoryItem> get() {
        return List.of(
                HomeDirectoryItem.builder().name("Main storage").iconId(R.drawable.home_main_storage).path("/")
                        .build(),
                HomeDirectoryItem.builder().name("Downloads").iconId(R.drawable.home_downloads).path("/Download")
                        .build(),
                HomeDirectoryItem.builder().name("Images").iconId(R.drawable.home_images).path("/Pictures")
                        .build(),
                HomeDirectoryItem.builder().name("Audio").iconId(R.drawable.home_audio).path("/Music")
                        .build(),
                HomeDirectoryItem.builder().name("Videos").iconId(R.drawable.home_videos).path("/Movies")
                        .build(),
                HomeDirectoryItem.builder().name("Documents").iconId(R.drawable.home_documents).path("/DCIM")
                        .build());
    }
}

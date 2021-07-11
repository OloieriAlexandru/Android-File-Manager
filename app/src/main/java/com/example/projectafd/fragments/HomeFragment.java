package com.example.projectafd.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projectafd.MainActivity;
import com.example.projectafd.R;
import com.example.projectafd.models.HomeDirectoryItem;
import com.example.projectafd.storage.factory.HomeDirectoryItemsProvider;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final int DIRECTORIES_PER_ROW = 3;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<HomeDirectoryItem> homeDirectoryItems = HomeDirectoryItemsProvider.get();

        FlexboxLayout homeFlexboxLayout = view.findViewById(R.id.homeLayout);
        Context context = getActivity().getApplicationContext();
        LinearLayout currentLinearLayout = null;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 24, 0, 0);

        for (int i = 0; i < homeDirectoryItems.size(); ++i) {
            if (i % DIRECTORIES_PER_ROW == 0) {
                currentLinearLayout = new LinearLayout(context);
                homeFlexboxLayout.addView(currentLinearLayout, layoutParams);
            }

            HomeDirectoryItem item = homeDirectoryItems.get(i);

            LayoutInflater inflater = getLayoutInflater();
            LinearLayout homeDirectoryItem = (LinearLayout) inflater.inflate(R.layout.home_directory_item, null);

            ImageView homeDirectoryItemImage = homeDirectoryItem.findViewById(R.id.homeDirectoryImage);
            TextView homeDirectoryItemName = homeDirectoryItem.findViewById(R.id.homeDirectoryName);
            TextView homeDirectoryItemInfo = homeDirectoryItem.findViewById(R.id.homeDirectoryInfo);

            homeDirectoryItemImage.setImageResource(item.getIconId());
            homeDirectoryItemName.setText(item.getName());

            homeDirectoryItem.setOnClickListener(v -> {
                MainActivity.updateShownFragment(StorageFragment.newInstance(item.getPath()));
            });

            if (currentLinearLayout != null) {
                currentLinearLayout.addView(homeDirectoryItem);
            }
        }

        super.onViewCreated(view, savedInstanceState);
    }
}

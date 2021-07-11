package com.example.projectafd.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectafd.MainActivity;
import com.example.projectafd.R;
import com.example.projectafd.models.ActionResult;
import com.example.projectafd.storage.abstractions.IStorageSystem;
import com.example.projectafd.storage.factory.StorageSystemProvider;
import com.example.projectafd.utils.DialogBuilders;
import com.example.projectafd.utils.ToastBuilders;

import java.io.File;

public class FileViewFragment extends Fragment {

    public static final String PARAM_DIR = "DIR_PATH";
    public static final String PARAM_FILE = "FILE_PATH";

    private File file;

    private IStorageSystem storageSystem;

    private String currentSavedContent;

    private EditText editText;

    public FileViewFragment() {
    }

    public static FileViewFragment newInstance(File directory, File file) {
        Bundle args = new Bundle();
        args.putString(PARAM_DIR, directory.getAbsolutePath());
        args.putString(PARAM_FILE, file.getAbsolutePath());

        FileViewFragment fragment = new FileViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String filePath = getArguments().getString(PARAM_FILE);
            String dirPath = getArguments().getString(PARAM_DIR);

            storageSystem = StorageSystemProvider.get(dirPath);
            file = new File(filePath);

            ActionResult fileReadingResult = storageSystem.readFile(file);
            if (fileReadingResult.isSuccess()) {
                currentSavedContent = fileReadingResult.getMessage();
            } else {
                System.out.println("ERROR");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initButtons(view);
    }

    private void initViews(View view) {
        editText = view.findViewById(R.id.fileViewArea);
        editText.setText(currentSavedContent);
    }

    private void initButtons(View view) {
        Button resetButton = (Button) view.findViewById(R.id.fileViewResetButton);
        resetButton.setOnClickListener(this::onResetButtonClicked);

        Button saveButton = (Button) view.findViewById(R.id.fileViewSaveButton);
        saveButton.setOnClickListener(this::onSaveButtonClicked);

        Button backButton = (Button) view.findViewById(R.id.fileViewBackButton);
        backButton.setOnClickListener(this::onBackButtonClicked);
    }

    private void onResetButtonClicked(View view) {
        String currentText = getCurrentEditText();
        if (currentText.equals(currentSavedContent)) {
            return;
        }
        ToastBuilders.openShortToast(getActivity(), "File content reset!");
        editText.setText(currentSavedContent);
    }

    private void onSaveButtonClicked(View view) {
        String currentText = getCurrentEditText();
        if (currentText.equals(currentSavedContent)) {
            return;
        }
        writeTextToFile(currentText);
        ToastBuilders.openShortToast(getActivity(), "File saved successfully!");
    }

    private void onBackButtonClicked(View view) {
        String currentText = getCurrentEditText();
        if (!currentText.equals(currentSavedContent)) {
            DialogBuilders.openConfirmDialog(getActivity(), this.getLayoutInflater(),
                "Do you really want to exit without saving?", (dialog, which) -> {
                MainActivity.updateLastShownFragment();
            });
            return;
        }
        MainActivity.updateLastShownFragment();
    }

    private void writeTextToFile(String text) {
        storageSystem.writeFile(file, text);
        currentSavedContent = text;
    }

    private String getCurrentEditText() {
        return editText.getText().toString();
    }
}

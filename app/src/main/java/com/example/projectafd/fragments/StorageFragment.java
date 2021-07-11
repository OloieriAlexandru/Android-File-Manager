package com.example.projectafd.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.projectafd.MainActivity;
import com.example.projectafd.R;
import com.example.projectafd.models.ActionResult;
import com.example.projectafd.models.DialogBuilderResultObject;
import com.example.projectafd.models.FileItem;
import com.example.projectafd.models.enums.FileItemType;
import com.example.projectafd.storage.abstractions.IStorageSystem;
import com.example.projectafd.storage.factory.StorageSystemProvider;
import com.example.projectafd.ui.adapters.FileItemsAdapter;
import com.example.projectafd.utils.DialogBuilders;
import com.example.projectafd.utils.Helper;
import com.example.projectafd.utils.ToastBuilders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StorageFragment extends Fragment {

    public static final String DIRECTORY_PARAM = "INITIAL_DIR";
    public static final int COLOR_SELECTED = Color.parseColor("#1CBBF7");
    public static final int COLOR_NOT_SELECTED = Color.parseColor("#FFFFFF");

    private static final int STATE_BROWSE_ADD = 0;
    private static final int STATE_SELECTED = 1;
    private static final int STATE_MOVE_COPY = 2;
    private static final int OPERATION_COPY = 0;
    private static final int OPERATION_MOVE = 1;
    private static final int OPERATION_NONE = 2;
    private int currentState;
    private int operation;
    private boolean copyMoveAllSuccessful;
    private boolean copyMoveOneSuccessful;
    private Iterator<FileItem> setIterator;

    private DialogBuilderResultObject dialogBuilderResultObject;

    private IStorageSystem storageSystem;

    private List<FileItem> files;

    private FileItemsAdapter fileItemsAdapter;

    private List<LinearLayout> statesLayouts;

    private Set<FileItem> selectedFiles;

    public StorageFragment() {
    }

    public static StorageFragment newInstance(String initialDir) {
        Bundle args = new Bundle();
        args.putString(DIRECTORY_PARAM, initialDir);

        StorageFragment fragment = new StorageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String currentDirectory = getArguments().getString(DIRECTORY_PARAM);

            storageSystem = StorageSystemProvider.get(currentDirectory);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initButtons(view);
        initFilesList(view);
        initStates(view);

        Helper.hideKeyboardFrom(getActivity(), view);
    }

    private void initButtons(View view) {
        Button newFileButton = (Button) view.findViewById(R.id.filesListCreateFileButton);
        newFileButton.setOnClickListener(this::onNewFileButtonClicked);

        Button newFolderButton = (Button) view.findViewById(R.id.filesListCreateFolderButton);
        newFolderButton.setOnClickListener(this::onNewFolderButtonClicked);

        Button copyButton = (Button) view.findViewById(R.id.filesListCopyButton);
        copyButton.setOnClickListener(this::onCopyButtonClicked);

        Button moveButton = (Button) view.findViewById(R.id.filesListMoveButton);
        moveButton.setOnClickListener(this::onMoveButtonClicked);

        Button renameButton = (Button) view.findViewById(R.id.filesListRenameButton);
        renameButton.setOnClickListener(this::onRenameButtonClicked);

        Button deleteButton = (Button) view.findViewById(R.id.filesListDeleteButton);
        deleteButton.setOnClickListener(this::onDeleteButtonClicked);

        Button pasteButton = (Button) view.findViewById(R.id.filesListPasteButton);
        pasteButton.setOnClickListener(this::onPasteButtonClicked);

        Button cancelButton = (Button) view.findViewById(R.id.filesListCancelButton);
        cancelButton.setOnClickListener(this::onCancelButtonClicked);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFilesList(@NonNull View view) {
        files = storageSystem.getFileItems();
        selectedFiles = new HashSet<>();
        fileItemsAdapter = new FileItemsAdapter(getActivity().getApplicationContext(), -1, files);

        ListView listView = (ListView) view.findViewById(R.id.list_view);
        listView.setAdapter(fileItemsAdapter);

        listView.setOnItemLongClickListener((adapterView, view1, position, id) -> {
            FileItem fileItem = files.get(position);
            if (fileItem != null && currentState == STATE_BROWSE_ADD && !fileItem.getName().equals("..")) {
                enterState(STATE_SELECTED);
                selectedFileItemChangeState(fileItem, view1, true);
                return true;
            }
            return false;
        });

        listView.setOnItemClickListener((adapterView, view12, position, id) -> {
            FileItem fileItem = files.get(position);
            if (fileItem != null) {
                handleFileItemClicked(fileItem, view12);
            }
        });

        listView.setLongClickable(true);
    }

    private void initStates(@NonNull View view) {
        statesLayouts = new ArrayList<>();
        statesLayouts.add((LinearLayout) view.findViewById(R.id.filesListState0));
        statesLayouts.add((LinearLayout) view.findViewById(R.id.filesListState1));
        statesLayouts.add((LinearLayout) view.findViewById(R.id.filesListState2));
        currentState = 0;
    }

    private void enterState(int state) {
        if (state == currentState) {
            return;
        }
        if (!(0 <= state && state < statesLayouts.size())) {
            return;
        }
        for (LinearLayout stateLayout : statesLayouts) {
            stateLayout.setVisibility(View.GONE);
        }

        if (state != STATE_MOVE_COPY) {
            resetStateSelected();
        }

        statesLayouts.get(state).setVisibility(View.VISIBLE);
        currentState = state;
    }

    private void handleFileItemClicked(FileItem fileItem, View view) {
        boolean isDirectory = fileItem.getType() == FileItemType.DIRECTORY;
        if (currentState == STATE_BROWSE_ADD) {
            if (isDirectory) {
                changeDirectory(fileItem);
            } else {
                openFileViewer(fileItem);
            }
        } else if (currentState == STATE_SELECTED) {
            handleStateSelectedFileItemClick(fileItem, view);
        } else if (currentState == STATE_MOVE_COPY) {
            if (isDirectory) {
                changeDirectory(fileItem);
            }
        }
    }

    private void handleStateSelectedFileItemClick(FileItem fileItem, View view) {
        selectedFileItemChangeState(fileItem, view, !selectedFiles.contains(fileItem));
        if (selectedFiles.size() == 0) {
            enterState(STATE_BROWSE_ADD);
        }
    }

    private synchronized void selectedFileItemChangeState(FileItem fileItem, View view, boolean selected) {
        if (fileItem.getName().equals("..")) {
            return;
        }
        if (selected) {
            selectedFiles.add(fileItem);
            view.setBackgroundColor(COLOR_SELECTED);
        } else {
            selectedFiles.remove(fileItem);
            view.setBackgroundColor(COLOR_NOT_SELECTED);
        }
    }

    private void resetStateSelected() {
        selectedFiles.clear();
    }

    private void changeDirectory(FileItem directory) {
        storageSystem.changeDirectory(directory.getFile());

        reloadFiles();
    }

    private void reloadFiles() {
        files.clear();
        files.addAll(storageSystem.getFileItems());
        fileItemsAdapter.notifyDataSetChanged();
    }

    private void openFileViewer(FileItem file) {
        FileViewFragment fileViewFragment = FileViewFragment.newInstance(storageSystem.getCurrentDirectory(), file.getFile());
        MainActivity.updateShownFragment(fileViewFragment);
    }

    private void copyMoveInit() {
        copyMoveAllSuccessful = true;
        copyMoveOneSuccessful = false;
        setIterator = selectedFiles.iterator();
    }

    private void copyMoveEnd() {
        if (copyMoveAllSuccessful) {
            ToastBuilders.openShortToast(getActivity(), String.format("All files %s successfully!", operation == OPERATION_COPY ? "copied" : "moved"));
        } else {
            ToastBuilders.openShortToast(getActivity(), String.format("Error while %s some of the files!", operation == OPERATION_COPY ? "copying" : "moving"));
        }
        if (copyMoveOneSuccessful) {
            reloadFiles();
        }
        enterState(STATE_BROWSE_ADD);
    }

    private void handleNextItem() {
        if (!setIterator.hasNext()) {
            copyMoveEnd();
            return;
        }
        FileItem nextItem = setIterator.next();

        ActionResult actionResult;
        if (operation == OPERATION_COPY) {
            actionResult = storageSystem.copy(nextItem);
        } else {
            actionResult = storageSystem.move(nextItem);
        }

        if (actionResult.isSuccess()) {
            copyMoveOneSuccessful = true;
            handleNextItem();
        } else {
            if (actionResult.getMessage().equals("Override")) {
                DialogBuilders.openConfirmDialog(getActivity(), this.getLayoutInflater(),
                    String.format("Do you really want to override the \"%s\" item from this directory?", nextItem.getName()), "Skip", (dialog, which) -> {
                        ActionResult result;
                        if (operation == OPERATION_COPY) {
                            result = storageSystem.copyForce(nextItem);
                        } else {
                            result = storageSystem.moveForce(nextItem);
                        }

                        if (result.isSuccess()) {
                            copyMoveOneSuccessful = true;
                        } else {
                            copyMoveAllSuccessful = false;
                        }
                        handleNextItem();
                    }, (dialog, which) -> handleNextItem());
            } else {
                copyMoveAllSuccessful = false;
                handleNextItem();
            }
        }
    }

    private void copyMoveSelectedFiles() {
        copyMoveInit();
        handleNextItem();
    }

    // Button events

    public void onNewFileButtonClicked(View view) {
        dialogBuilderResultObject = DialogBuilders.openNameInputDialog(getActivity(), this.getLayoutInflater(),
            "Enter file name", (dialog, which) -> {
                String newFileName = dialogBuilderResultObject.getNameInputAlertDialogEditText().getText().toString();
                ActionResult actionResult = storageSystem.createFile(newFileName);
                if (actionResult.isSuccess()) {
                    reloadFiles();
                }
                ToastBuilders.openShortToast(getActivity(), actionResult);
            });
    }

    public void onNewFolderButtonClicked(View view) {
        dialogBuilderResultObject = DialogBuilders.openNameInputDialog(getActivity(), this.getLayoutInflater(),
            "Enter folder name", (dialog, which) -> {
                String newDirectoryName = dialogBuilderResultObject.getNameInputAlertDialogEditText().getText().toString();
                ActionResult actionResult = storageSystem.createDirectory(newDirectoryName);
                if (actionResult.isSuccess()) {
                    reloadFiles();
                }
                ToastBuilders.openShortToast(getActivity(), actionResult);
            });
    }

    public void onCopyButtonClicked(View view) {
        operation = OPERATION_COPY;
        enterState(STATE_MOVE_COPY);
    }

    public void onMoveButtonClicked(View view) {
        operation = OPERATION_MOVE;
        enterState(STATE_MOVE_COPY);
    }

    public void onRenameButtonClicked(View view) {
        if (currentState != STATE_SELECTED || selectedFiles.size() > 1) {
            return;
        }
        for (FileItem fileItem : selectedFiles) {
            dialogBuilderResultObject = DialogBuilders.openNameInputDialog(getActivity(), this.getLayoutInflater(),
                "Enter new name", "Rename", (dialog, which) -> {
                    String newName = dialogBuilderResultObject.getNameInputAlertDialogEditText().getText().toString();
                    ActionResult actionResult = storageSystem.rename(fileItem, newName);
                    resetStateSelected();
                    reloadFiles();
                    ToastBuilders.openShortToast(getActivity(), actionResult);
                }, (dialog, which) -> {
                    resetStateSelected();
                    reloadFiles();
                });
            EditText nameInputAlertDialogEditText = dialogBuilderResultObject.getNameInputAlertDialogEditText();
            nameInputAlertDialogEditText.setText(fileItem.getName());
            nameInputAlertDialogEditText.setSelection(fileItem.getName().length());
        }
        enterState(STATE_BROWSE_ADD);
    }

    public void onDeleteButtonClicked(View view) {
        if (currentState != STATE_SELECTED) {
            return;
        }
        DialogBuilders.openConfirmDialog(getActivity(), this.getLayoutInflater(),
            "Do you really want to delete the selected items?", "Cancel", (dialog, which) -> {
                boolean allSuccessful = true;
                boolean oneSuccessful = false;
                for (FileItem fileItem : selectedFiles) {
                    ActionResult actionResult = storageSystem.delete(fileItem);
                    if (actionResult.isSuccess()) {
                        oneSuccessful = true;
                    } else {
                        allSuccessful = false;
                    }
                }
                if (allSuccessful) {
                    ToastBuilders.openShortToast(getActivity(), "All files deleted successfully!");
                } else {
                    ToastBuilders.openShortToast(getActivity(), "Error while deleting some of the files!");
                }
                resetStateSelected();
                reloadFiles();
                enterState(STATE_BROWSE_ADD);
            }, (dialog, which) -> {
                resetStateSelected();
                reloadFiles();
                enterState(STATE_BROWSE_ADD);
            });
    }

    public void onPasteButtonClicked(View view) {
        if (currentState != STATE_MOVE_COPY) {
            return;
        }
        copyMoveSelectedFiles();
    }

    public void onCancelButtonClicked(View view) {
        operation = OPERATION_NONE;
        resetStateSelected();
        reloadFiles();
        enterState(STATE_BROWSE_ADD);
    }
}

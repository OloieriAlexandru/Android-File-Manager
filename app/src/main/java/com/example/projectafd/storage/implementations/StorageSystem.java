package com.example.projectafd.storage.implementations;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.example.projectafd.models.ActionResult;
import com.example.projectafd.models.DirectoryFileItemSpecificInfo;
import com.example.projectafd.models.FileItem;
import com.example.projectafd.models.NormalFileItemSpecificInfo;
import com.example.projectafd.models.enums.FileItemType;
import com.example.projectafd.models.interfaces.AbstractFileItemSpecificInfo;
import com.example.projectafd.storage.abstractions.IStorageSystem;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StorageSystem implements IStorageSystem {

    private static final int NEXT_FILE_NAME_MAX_RETRIES = 100_000;
    private File currentDirectory;

    public StorageSystem(String initialDirectory) {
        String basePath = Environment.getExternalStorageDirectory().toString();
        String currentPath = String.format("%s/%s", basePath, initialDirectory);

        currentDirectory = new File(currentPath);
    }

    @Override
    public ActionResult readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder fileContentBuilder = new StringBuilder();
            String fileLine;

            while ((fileLine = reader.readLine()) != null) {
                fileContentBuilder.append(fileLine);
                fileContentBuilder.append('\n');
            }

            return ActionResult.ok(fileContentBuilder.toString());
        } catch (FileNotFoundException e) {
            return ActionResult.error("The file doesn't exist!");
        } catch (IOException e) {
            return ActionResult.error("Error when reading from file!");
        }
    }

    @Override
    public ActionResult writeFile(File file, String newFileContent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(newFileContent);

            return ActionResult.ok("Content written successfully to file!");
        } catch (IOException e) {
            return ActionResult.error("Error when writing to file!");
        }
    }

    @Override
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    @Override
    public List<File> getFiles() {
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(currentDirectory.listFiles())));
    }

    @Override
    public List<FileItem> getFileItems() {
        List<FileItem> returnedItems = Arrays.stream(Objects.requireNonNull(currentDirectory.listFiles()))
                .map(StorageSystem::toFileItem)
                .collect(Collectors.toList());
        if (currentDirectory.getParentFile() != null && currentDirectory.getParentFile().listFiles() != null) {
            returnedItems.add(0, FileItem.builder()
                    .name("..")
                    .type(FileItemType.DIRECTORY)
                    .file(currentDirectory.getParentFile())
                    .build());
        }
        return returnedItems;
    }

    @Override
    public void changeDirectory(File directory) {
        currentDirectory = directory;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult createDirectory(String directoryName) {
        Path newDirectoryPath = Paths.get(currentDirectory.getAbsolutePath(), directoryName);
        File newDirectory = new File(newDirectoryPath.toString());
        try {
            if(!newDirectory.mkdir()) {
                return ActionResult.error("The directory already exists!");
            }
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        }
        return ActionResult.ok("Directory created successfully!");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult createFile(String fileName) {
        Path newDirectoryPath = Paths.get(currentDirectory.getPath(), fileName);
        File newFile = new File(newDirectoryPath.toString());
        try {
            if (!newFile.createNewFile()) {
                return ActionResult.error("The file already exists!");
            }
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        } catch (IOException e) {
            return ActionResult.error("An error occurred!");
        }
        return ActionResult.ok("File created successfully!");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult rename(FileItem fileItem, String newName) {
        Path newItemPath = Paths.get(currentDirectory.getPath(), newName);
        File newFile = new File(newItemPath.toString());
        File oldFile = fileItem.getFile();
        try {
            if (!oldFile.renameTo(newFile)) {
                return ActionResult.error("Rename operation failed!");
            }
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        } catch (NullPointerException e) {
            return ActionResult.error("Unexpected error occurred!");
        }

        return ActionResult.ok("Item renamed successfully!");
    }

    @Override
    public ActionResult delete(FileItem fileItem) {
        File file = fileItem.getFile();
        try {
            if (fileItem.getType() == FileItemType.DIRECTORY) {
                FileUtils.deleteDirectory(file);
            } else {
                if (!file.delete()) {
                    return ActionResult.error("Failed to delete the file!");
                }
            }
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        } catch (IOException e) {
            return ActionResult.error("An error occurred!");
        }

        return ActionResult.ok(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult copy(FileItem fileItem) {
        Path targetPath;
        String copiedFileName = fileItem.getName();

        Set<String> existingFileNames = new HashSet<>(Arrays.asList(currentDirectory.list()));
        if (fileItem.getFile().getParentFile().getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
            ActionResult result = getNextAvailableName(existingFileNames, copiedFileName);
            if (!result.isSuccess()) {
                return ActionResult.error("Copy operation failed!");
            }
            targetPath = Paths.get(currentDirectory.getPath(), result.getMessage());
        } else {
            if (existingFileNames.contains(copiedFileName)) {
                return ActionResult.error("Override");
            } else {
                targetPath = Paths.get(currentDirectory.getPath(), copiedFileName);
            }
        }

        return copyCall(fileItem.getFile().toPath(), targetPath);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult copyForce(FileItem fileItem) {
        Path targetPath = Paths.get(currentDirectory.getPath(), fileItem.getName());
        return copyCall(fileItem.getFile().toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ActionResult copyCall(Path fromPath, Path targetPath, CopyOption... options) {
        try {
            Files.copy(fromPath, targetPath, options);
        } catch (DirectoryNotEmptyException e) {
            return ActionResult.error("Operation not allowed!");
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        } catch (IOException e) {
            return ActionResult.error("An error occurred!");
        }

        return ActionResult.ok("File copied successfully!");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult move(FileItem fileItem) {
        if (fileItem.getFile().getParentFile().getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
            return ActionResult.ok("The item is already in the current directory!");
        }
        String movedFileName = fileItem.getName();
        Set<String> existingFileNames = new HashSet<>(Arrays.asList(currentDirectory.list()));
        if (existingFileNames.contains(fileItem.getName())) {
            return ActionResult.error("Override");
        }

        return moveCall(fileItem.getFile(), fileItem.getFile().toPath(), Paths.get(currentDirectory.getPath(), movedFileName));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ActionResult moveForce(FileItem fileItem) {
        Path targetPath = Paths.get(currentDirectory.getPath(), fileItem.getName());
        return moveCall(fileItem.getFile(), fileItem.getFile().toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ActionResult moveCall(File movedFile, Path fromPath, Path targetPath, CopyOption... options) {
        try {
            Files.copy(fromPath, targetPath, options);
            if (!movedFile.delete()) {
                return ActionResult.error("An error occurred!");
            }
        } catch (DirectoryNotEmptyException e) {
            return ActionResult.error("Operation not allowed!");
        } catch (SecurityException e) {
            return ActionResult.error("You're not allowed to do this!");
        } catch (IOException e) {
            return ActionResult.error("An error occurred!");
        }

        return ActionResult.ok("File moved successfully!");
    }

    private ActionResult getNextAvailableName(Set<String> existingFileNames, String originalFileName) {
        for (int i = 1; i < NEXT_FILE_NAME_MAX_RETRIES; ++i) {
            @SuppressLint("DefaultLocale") String possibleFileName = String.format("%s (%d)", originalFileName, i);
            if (!existingFileNames.contains(possibleFileName)) {
                return ActionResult.ok(possibleFileName);
            }
        }
        return ActionResult.error("Could not file a new possible name!");
    }

    @SuppressLint("NewApi")
    private static FileItem toFileItem(File file) {
        FileItemType fileItemType;
        AbstractFileItemSpecificInfo fileItemSpecificInfo;
        if (file.isDirectory()) {
            fileItemType = FileItemType.DIRECTORY;
            File[] files = file.listFiles();
            int filesCount = 0;
            if (files != null) {
                filesCount = files.length;
            }
            fileItemSpecificInfo = new DirectoryFileItemSpecificInfo(filesCount);
        } else {
            fileItemType = FileItemType.OTHER;
            fileItemSpecificInfo = new NormalFileItemSpecificInfo(file.length());
        }

        return FileItem.builder()
                .name(file.getName())
                .type(fileItemType)
                .specificInfo(fileItemSpecificInfo)
                .file(file)
                .build();
    }
}

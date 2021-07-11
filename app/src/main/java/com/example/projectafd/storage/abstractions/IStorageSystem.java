package com.example.projectafd.storage.abstractions;

import com.example.projectafd.models.ActionResult;
import com.example.projectafd.models.FileItem;

import java.io.File;
import java.util.List;

public interface IStorageSystem {

    ActionResult readFile(File file);

    ActionResult writeFile(File file, String newFileContent);

    File getCurrentDirectory();

    List<File> getFiles();

    List<FileItem> getFileItems();

    void changeDirectory(File directory);

    ActionResult createDirectory(String directoryName);

    ActionResult createFile(String fileName);

    ActionResult rename(FileItem fileItem, String newName);

    ActionResult delete(FileItem fileItem);

    ActionResult copy(FileItem fileItem);

    ActionResult copyForce(FileItem fileItem);

    ActionResult move(FileItem fileItem);

    ActionResult moveForce(FileItem fileItem);
}

package ua.in.quireg.chan.models.presentation;

import java.io.File;

public class FileModel {
    public File file;

    public FileModel() { }

    public FileModel(File file) {
        this.file = file;
    }

    public int getFileSize() {
        return (int) Math.round(this.file.length() / 1024.0);
    }
}

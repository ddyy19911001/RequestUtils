package yin.deng.dyutils.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListParams extends BaseHttpInfo {
    private List<String> fileNames=new ArrayList<>();
    private List<File>  files=new ArrayList<>();

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}

package org.liuyehcf.markdown.format.model;

import java.io.File;

/**
 * Created by HCF on 2018/1/13.
 */
public class HexoParam extends NormalParam {
    private File fileDirectory;

    private File imageDirectory;

    public File getFileDirectory() {
        return fileDirectory;
    }

    public void setFileDirectory(File fileDirectory) {
        this.fileDirectory = fileDirectory;
    }

    public File getImageDirectory() {
        return imageDirectory;
    }

    public void setImageDirectory(File imageDirectory) {
        this.imageDirectory = imageDirectory;
    }
}

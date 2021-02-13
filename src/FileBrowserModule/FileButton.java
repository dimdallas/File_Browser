package FileBrowserModule;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

public class FileButton extends JButton {
    private File myFile;
    private String extension;
    private String type;
    private long fileSizeInKB;
    private String lastModified;

    public FileButton(String name, File file){
        super(name);
        myFile = file;
        extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        try {
            type = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSizeInKB = file.length()/1024;
        lastModified = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(file.lastModified());
    }

    public File getMyFile() {
        return myFile;
    }

    public String getExtension() { return extension; }

    public String getType() {
        return type;
    }

    public String getFileSizeInKB() {
        return fileSizeInKB + " KB";
    }

    public String getLastModified() {
        return lastModified;
    }
}

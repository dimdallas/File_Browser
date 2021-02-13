package FileBrowserModule;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DirectoryNode extends DefaultMutableTreeNode {
    private File myFile;
    private Set<File> subFiles;
    private Set<DirectoryNode> subFolders;
    private String type = "folder";
    private long fileSizeInKB;
    private String lastModified;

    public DirectoryNode(String fileName, String filePath) {
        super(fileName);
        myFile = new File(filePath);

        subFiles = new LinkedHashSet<>(); //CHANGE TO SORTED
        subFolders = new HashSet<>();

        File[] contentFiles = myFile.listFiles(file -> !file.isHidden());
        if (contentFiles != null) {
            for (File dir : contentFiles)
                if (dir.isDirectory())
                    subFiles.add(dir);

            for (File file : contentFiles)
                if (file.isFile())
                    subFiles.add(file);
        }
    }

    public void addSubDirectories() {
        if (subFolders.isEmpty()) {
            if (!subFiles.isEmpty()) {
                for (File file : subFiles) {
                    if (file.isDirectory()) {
                        DirectoryNode subDirectory = new DirectoryNode(file.getName(), file.getAbsolutePath());
                        subFolders.add(subDirectory);
                        this.add(subDirectory);
                    }
                }
            }
        }
    }

    public void addNode(DirectoryNode toBeAdded){
        subFolders.add(toBeAdded);
        this.add(toBeAdded);
    }

    public void removeNode(DirectoryNode toBeRemoved){
        subFolders.remove(toBeRemoved);
        this.remove(toBeRemoved);
    }

    void renameNode(File newNameDir){
        this.setMyFile(newNameDir);
        this.setUserObject(newNameDir.getName());
    }

    void setSubFiles() {
        File[] contentFiles = myFile.listFiles(file -> !file.isHidden());
        if (contentFiles != null) {
            subFiles.clear();
            for (File dir : contentFiles)
                if (dir.isDirectory())
                    subFiles.add(dir);

            for (File file : contentFiles)
                if (file.isFile())
                    subFiles.add(file);
        }
    }

    File getMyFile() {
        return myFile;
    }

    void setMyFile(File myFile) {
        this.myFile = myFile;
    }

    public String getType() {
        return type;
    }

    public long findFolderSize(File dir){
        long folderSize = 0;
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isFile())
                    folderSize += file.length() / 1024;
                else folderSize += findFolderSize(file);
            }
        }

        return folderSize;
    }

    public String getFileSizeInKB() {
        fileSizeInKB = findFolderSize(myFile);
        return fileSizeInKB + " KB";
    }

/*
    public void findLastModified(File dir){
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isFile()) {
                    String fileModified = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(file.lastModified());
                    if (lastModified.compareTo(fileModified) < 0){
                        lastModified = fileModified;
                    }
                }
                else {
                    findLastModified(file);
                }
            }
        }
    }
*/

    public String getLastModified() {
//        findLastModified(myFile);
        lastModified = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(myFile.lastModified());
        return lastModified;
    }


    Set<File> getSubFiles() {
        return subFiles;
    }

    public Set<DirectoryNode> getSubFolders() {
        return subFolders;
    }

    DirectoryNode getDirectory(String dirName) {
        for (DirectoryNode dir : subFolders) {
            if (dir.getUserObject().equals(dirName))
                return dir;
        }
        return null;
    }
}

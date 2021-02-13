package FileBrowserModule;

import javax.swing.*;

public class DirectoryButton extends JButton {
    private DirectoryNode myDirectory;

    public DirectoryButton(String name, DirectoryNode myDirectory){
        super(name);
        this.myDirectory = myDirectory;
    }

    public DirectoryNode getMyDirectory() {
        return myDirectory;
    }
}

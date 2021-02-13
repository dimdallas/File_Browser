package FileBrowserModule;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBrowser {
    private static JFrame browserFrame;
    private static JMenuBar menuBar;
    private static JPanel mainPanel;
    private static JScrollPane scrollTree;
    private static JScrollPane scrollContents;
    private static JSplitPane splitPane;
    private static JPanel contents;

    //tree related
    private static DirectoryNode directoryTreeRoot;
    private static JTree directoryTree;

    //selected items
    private static JButton selectedButton = null;
    private static DirectoryNode currOpenFolder;

    //Searching
    private static JPanel searchPanel;
    private static JTextField searchText;

    //create/rename options
    private static JButton okButton;
    private static JTextField optionText;

    private static final int MINIMUM_WIDTH = 700;
    private static final int MINIMUM_HEIGHT = 500;
    private static final Dimension browserSize =
            new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT);

//    private static File rootFile = FileSystemView.getFileSystemView().getRoots()[0];
    private static File rootFile = new File("/");
    private static final String usersPath = System.getProperty("user.home");
    private static final String separator = File.separator;
    private static final String iconPath = "hw4-icons" + separator + "icons" + separator;

    //RUN FILE FROM BUTTON
    //DELETE-RENAME FOR FILES
    private static MouseListener fileButtonListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            //RIGHT CLICK
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                JPopupMenu rightClickMenu = new JPopupMenu();

                JMenuItem deleteOption = new JMenuItem("Delete");
                rightClickMenu.add(deleteOption);
                JMenuItem renameOption = new JMenuItem("Rename");
                rightClickMenu.add(renameOption);

                File selectedFile = ((FileButton) mouseEvent.getSource()).getMyFile();
                String fileName = selectedFile.getName();

                //DELETE FOR FILE
                deleteOption.addActionListener(actionEvent -> {
                    String[] buttons = {"Confirm", "Cancel"};
                    int buttonSelection = JOptionPane.showOptionDialog(
                            browserFrame,
                            "Are you sure you want to delete file " + fileName + "?",
                            "Confirm Deletion",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            buttons,
                            buttons[0]);

                    switch (buttonSelection) {
                        case JOptionPane.OK_OPTION:
                            if (selectedFile.delete()) {
                                currOpenFolder.setSubFiles();
                                if (contents.getLayout().getClass() == FlowLayout.class)
                                    directoryContents(currOpenFolder);
                                else directoryList(currOpenFolder);
                                contents.addMouseListener(panelListener);
                                scrollContents = new JScrollPane(contents,
                                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                scrollContents.setPreferredSize(browserSize);
                                mainPanel.remove(splitPane);
                                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        scrollTree, scrollContents);
                                mainPanel.add(splitPane, BorderLayout.CENTER);
                                browserFrame.setContentPane(mainPanel);
                                browserFrame.pack();
                                browserFrame.setVisible(true);
                            } else
                                System.out.println("File " + fileName + " not deleted");
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;
                        default:
                            System.out.println("Pane discarded");
                    }
                });
                //RENAME FOR FILE
                renameOption.addActionListener(actionEvent -> {
                    JFrame renameFrame = createOptionFrame("Rename " + fileName);
                    renameFrame.setTitle("Rename file");

                    optionText.addKeyListener(new KeyListener() {
                        @Override
                        public void keyTyped(KeyEvent keyEvent) {
                            if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                                File newNameFile = new File(
                                        currOpenFolder.getMyFile().getPath() +
                                                separator + optionText.getText());
                                if (!newNameFile.exists()) {
                                    if (selectedFile.renameTo(newNameFile)) {
                                        currOpenFolder.setSubFiles();
                                        if (contents.getLayout().getClass() == FlowLayout.class)
                                            directoryContents(currOpenFolder);
                                        else directoryList(currOpenFolder);
                                        contents.addMouseListener(panelListener);
                                        scrollContents = new JScrollPane(contents,
                                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                        scrollContents.setPreferredSize(browserSize);
                                        mainPanel.remove(splitPane);
                                        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                scrollTree, scrollContents);
                                        mainPanel.add(splitPane, BorderLayout.CENTER);
                                        browserFrame.setContentPane(mainPanel);
                                        browserFrame.pack();
                                        browserFrame.setVisible(true);
                                        renameFrame.dispose();
                                    }
                                    //RENAME ERROR
                                    else {
                                        JOptionPane.showMessageDialog(browserFrame, "Could not rename file",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                //"EXISTS" WARNING
                                else {
                                    JOptionPane.showMessageDialog(browserFrame, "File already exists",
                                            "Warning", JOptionPane.WARNING_MESSAGE);
                                }
                            } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                                renameFrame.dispose();
                            }
                        }

                        @Override
                        public void keyPressed(KeyEvent keyEvent) {

                        }

                        @Override
                        public void keyReleased(KeyEvent keyEvent) {

                        }
                    });
                    okButton.addActionListener(actionEvent1 -> {
                        File newNameFile = new File(
                                currOpenFolder.getMyFile().getPath() +
                                        separator + optionText.getText());
                        if (!newNameFile.exists()) {
                            if (selectedFile.renameTo(newNameFile)) {
                                currOpenFolder.setSubFiles();
                                if (contents.getLayout().getClass() == FlowLayout.class)
                                    directoryContents(currOpenFolder);
                                else directoryList(currOpenFolder);
                                contents.addMouseListener(panelListener);
                                scrollContents = new JScrollPane(contents,
                                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                mainPanel.remove(splitPane);
                                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        scrollTree, scrollContents);
                                mainPanel.add(splitPane, BorderLayout.CENTER);
                                browserFrame.setContentPane(mainPanel);
                                browserFrame.pack();
                                browserFrame.setVisible(true);
                                renameFrame.dispose();
                            }
                            //RENAME ERROR
                            else {
                                JOptionPane.showMessageDialog(browserFrame, "Could not rename file",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "File already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    });

                    Dimension browserFrameSize = browserFrame.getSize();
                    Point browserFramePoint = browserFrame.getLocation();
                    int X = (browserFrameSize.width - renameFrame.getWidth()) / 2;
                    int Y = (browserFrameSize.height - renameFrame.getHeight()) / 2;
                    renameFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
                    renameFrame.setVisible(true);
                });


                rightClickMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
            //LEFT CLICK
            else if (mouseEvent.getClickCount() == 2) {
                File runFile = ((FileButton) mouseEvent.getSource()).getMyFile();
                Desktop runnable = Desktop.getDesktop();
                Runtime runtime = Runtime.getRuntime();
                try {
                    if (runFile.canExecute()){
                        runtime.exec(runFile.getPath());
                        runtime.exec("wine " + runFile.getPath());
                    }
                    else
                        runnable.open(runFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mouseEvent.getClickCount() == 1) {
                selectedButton = (FileButton) mouseEvent.getSource();
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    };

    //OPEN FOLDER FORM BUTTON
    //DELETE-RENAME FOR DIRECTORIES
    private static MouseListener folderButtonListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            //RIGHT CLICK
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                JPopupMenu rightClickMenu = new JPopupMenu();

                JMenuItem deleteOption = new JMenuItem("Delete");
                rightClickMenu.add(deleteOption);
                JMenuItem renameOption = new JMenuItem("Rename");
                rightClickMenu.add(renameOption);

                DirectoryButton selectedButton = (DirectoryButton) mouseEvent.getSource();
                DirectoryNode selectedNode = selectedButton.getMyDirectory();
                File selectedDir = selectedNode.getMyFile();
                String directoryName = selectedDir.getName();

                //DELETE FOR DIRECTORIES
                deleteOption.addActionListener(actionEvent -> {
                    String[] buttons = {"Confirm", "Cancel"};
                    int buttonSelection = JOptionPane.showOptionDialog(
                            browserFrame,
                            "Are you sure you want to delete file " + directoryName + "?",
                            "Confirm Deletion",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            buttons,
                            buttons[0]);

                    switch (buttonSelection) {
                        case JOptionPane.OK_OPTION:
                            deleteContents(selectedDir);

                            if (selectedDir.delete()) {
                                currOpenFolder.setSubFiles();
                                currOpenFolder.removeNode(selectedNode);

                                ((DefaultTreeModel) directoryTree.getModel()).reload(currOpenFolder);
                                if (currOpenFolder.equals(directoryTreeRoot)) {
                                    TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                                    directoryTree.setSelectionPath(selectedPath);
                                }

                            } else
                                System.out.println("Directory " + directoryName + " not deleted");
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;
                        default:
                            System.out.println("Pane discarded");
                    }
                });
                //RENAME FOR DIRECTORIES
                renameOption.addActionListener(actionEvent -> {
                    JFrame renameFrame = createOptionFrame("Rename " + directoryName);
                    renameFrame.setTitle("Rename directory");

                    optionText.addKeyListener(new KeyListener() {
                        @Override
                        public void keyTyped(KeyEvent keyEvent) {
                            if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                                File newNameDirectory = new File(
                                        currOpenFolder.getMyFile().getPath() +
                                                separator + optionText.getText());
                                if (!newNameDirectory.exists()) {
                                    if (selectedDir.renameTo(newNameDirectory)) {
                                        currOpenFolder.setSubFiles();
                                        selectedNode.renameNode(newNameDirectory);
                                        ((DefaultTreeModel) directoryTree.getModel()).reload(directoryTreeRoot);
                                        if (currOpenFolder.equals(directoryTreeRoot)) {
                                            TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                                            directoryTree.setSelectionPath(selectedPath);
                                        }
                                        renameFrame.dispose();
                                    }
                                    //RENAME ERROR
                                    else {
                                        JOptionPane.showMessageDialog(browserFrame, "Could not rename directory",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                //"EXISTS" WARNING
                                else {
                                    JOptionPane.showMessageDialog(browserFrame, "Directory already exists",
                                            "Warning", JOptionPane.WARNING_MESSAGE);
                                }
                            } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                                renameFrame.dispose();
                            }
                        }

                        @Override
                        public void keyPressed(KeyEvent keyEvent) {

                        }

                        @Override
                        public void keyReleased(KeyEvent keyEvent) {

                        }
                    });
                    okButton.addActionListener(actionEvent1 -> {
                        File newNameDirectory = new File(
                                currOpenFolder.getMyFile().getPath() +
                                        separator + optionText.getText());
                        if (!newNameDirectory.exists()) {
                            if (selectedDir.renameTo(newNameDirectory)) {
                                currOpenFolder.setSubFiles();
                                selectedNode.renameNode(newNameDirectory);
                                ((DefaultTreeModel) directoryTree.getModel()).reload(directoryTreeRoot);
                                if (currOpenFolder.equals(directoryTreeRoot)) {
                                    TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                                    directoryTree.setSelectionPath(selectedPath);
                                }
                                renameFrame.dispose();
                            }
                            //RENAME ERROR
                            else {
                                JOptionPane.showMessageDialog(browserFrame, "Could not rename directory",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "Directory already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    });

                    Dimension browserFrameSize = browserFrame.getSize();
                    Point browserFramePoint = browserFrame.getLocation();
                    int X = (browserFrameSize.width - renameFrame.getWidth()) / 2;
                    int Y = (browserFrameSize.height - renameFrame.getHeight()) / 2;
                    renameFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
                    renameFrame.setVisible(true);
                });

                rightClickMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
            //LEFT CLICK
            else if (mouseEvent.getClickCount() == 2) {
                currOpenFolder = ((DirectoryButton) mouseEvent.getSource()).getMyDirectory();
                TreePath newPath = new TreePath(currOpenFolder.getPath());
                directoryTree.setSelectionPath(newPath);
            } else if (mouseEvent.getClickCount() == 1) {
                selectedButton = (DirectoryButton) mouseEvent.getSource();
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    };

    //CREATE FILE FROM BAR AND RIGHT CLICK
    private static ActionListener createNewFile = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFrame newFileFrame = createOptionFrame("Enter filename");

            optionText.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                        String dirPath = currOpenFolder.getMyFile().getPath();
                        String fileName = optionText.getText();
                        File newFile = new File(dirPath + separator + fileName);

                        if (!newFile.exists()) {
                            try {
                                if (newFile.createNewFile()) {
                                    currOpenFolder.setSubFiles();
                                    if (contents.getLayout().getClass() == FlowLayout.class)
                                        directoryContents(currOpenFolder);
                                    else directoryList(currOpenFolder);
                                    contents.addMouseListener(panelListener);
                                    scrollContents = new JScrollPane(contents,
                                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                    scrollContents.setPreferredSize(browserSize);
                                    mainPanel.remove(splitPane);
                                    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            scrollTree, scrollContents);
                                    mainPanel.add(splitPane, BorderLayout.CENTER);
                                    browserFrame.setContentPane(mainPanel);
                                    browserFrame.pack();
                                    browserFrame.setVisible(true);
                                    newFileFrame.dispose();
                                } else JOptionPane.showMessageDialog(browserFrame, "Could not create file",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "File already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        newFileFrame.dispose();
                    }
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {

                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
            okButton.addActionListener(actionEvent1 -> {
                String dirPath = currOpenFolder.getMyFile().getPath();
                String fileName = optionText.getText();
                File newFile = new File(dirPath + separator + fileName);

                if (!newFile.exists()) {
                    try {
                        if (newFile.createNewFile()) {
                            currOpenFolder.setSubFiles();
                            if (contents.getLayout().getClass() == FlowLayout.class)
                                directoryContents(currOpenFolder);
                            else directoryList(currOpenFolder);
                            contents.addMouseListener(panelListener);
                            scrollContents = new JScrollPane(contents,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            mainPanel.remove(splitPane);
                            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                    scrollTree, scrollContents);
                            mainPanel.add(splitPane, BorderLayout.CENTER);
                            browserFrame.setContentPane(mainPanel);
                            browserFrame.pack();
                            browserFrame.setVisible(true);
                            newFileFrame.dispose();
                        }
                        //CREATE FILE ERROR
                        else {
                            JPanel errorPanel = new JPanel();
                            JOptionPane.showMessageDialog(errorPanel, "Could not create file",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            errorPanel.setVisible(true);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //"EXISTS" WARNING
                else {
                    JPanel warningPanel = new JPanel();
                    JOptionPane.showMessageDialog(warningPanel, "File already exists",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    warningPanel.setVisible(true);
                }
            });

            Dimension browserFrameSize = browserFrame.getSize();
            Point browserFramePoint = browserFrame.getLocation();
            int X = (browserFrameSize.width - newFileFrame.getWidth()) / 2;
            int Y = (browserFrameSize.height - newFileFrame.getHeight()) / 2;
            newFileFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
            newFileFrame.setVisible(true);
        }
    };

    //CREATE DIRECTORY FROM BAR AND RIGHT CLICK
    private static ActionListener createNewDirectory = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFrame newDirectoryFrame = createOptionFrame("Enter directory name");

            optionText.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                        String parentDirPath = currOpenFolder.getMyFile().getPath();
                        String dirName = optionText.getText();
                        String dirPath = parentDirPath + separator + dirName;
                        File newDirectory = new File(dirPath);

                        if (!newDirectory.exists()) {
                            if (newDirectory.mkdir()) {
                                currOpenFolder.setSubFiles();
                                DirectoryNode newNode = new DirectoryNode(dirName, dirPath);
                                currOpenFolder.addNode(newNode);

                                ((DefaultTreeModel) directoryTree.getModel()).reload(currOpenFolder);
                                if (currOpenFolder.equals(directoryTreeRoot)) {
                                    TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                                    directoryTree.setSelectionPath(selectedPath);
                                }

                                newDirectoryFrame.dispose();
                            } else System.out.println("Directory " + dirName + " not created");
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "Directory already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        newDirectoryFrame.dispose();
                    }
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {

                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
            okButton.addActionListener(actionEvent1 -> {
                String parentDirPath = currOpenFolder.getMyFile().getPath();
                String dirName = optionText.getText();
                String dirPath = parentDirPath + separator + dirName;
                File newDirectory = new File(dirPath);

                if (!newDirectory.exists()) {
                    if (newDirectory.mkdir()) {
                        currOpenFolder.setSubFiles();
                        DirectoryNode newNode = new DirectoryNode(dirName, dirPath);
                        currOpenFolder.addNode(newNode);
                        ((DefaultTreeModel) directoryTree.getModel()).reload(currOpenFolder);
                        if (currOpenFolder.equals(directoryTreeRoot)) {
                            TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                            directoryTree.setSelectionPath(selectedPath);
                        }

                        newDirectoryFrame.dispose();
                    } else System.out.println("Directory " + dirName + " not created");
                }
                //"EXISTS" WARNING
                else {
                    JPanel errorPanel = new JPanel();
                    JOptionPane.showMessageDialog(errorPanel, "File already exists",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    errorPanel.setVisible(true);
                }
            });

            Dimension browserFrameSize = browserFrame.getSize();
            Point browserFramePoint = browserFrame.getLocation();
            int X = (browserFrameSize.width - newDirectoryFrame.getWidth()) / 2;
            int Y = (browserFrameSize.height - newDirectoryFrame.getHeight()) / 2;
            newDirectoryFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
            newDirectoryFrame.setVisible(true);
        }
    };

    //RIGHT CLICK FOR PANEL OPTIONS
    private static MouseListener panelListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                JPopupMenu rightClickMenu = new JPopupMenu();

//               JMenu createNew = new JMenu("Create New...");
                JMenuItem createFile = new JMenuItem("Create New File");
                createFile.addActionListener(createNewFile);
                JMenuItem createDir = new JMenuItem("Create New Directory");
                createDir.addActionListener(createNewDirectory);

                rightClickMenu.add(createFile);
                rightClickMenu.add(createDir);

                rightClickMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    };

    //DELETE FROM BAR
    private static ActionListener delete = actionEvent -> {
        if (selectedButton == null) {
            System.out.println("Select file!");
        }
        //FILE BUTTONS
        else if (selectedButton.getClass() == FileButton.class) {
            File selectedFile = ((FileButton) selectedButton).getMyFile();
            String fileName = selectedFile.getName();

            String[] buttons = {"Confirm", "Cancel"};
            int buttonSelection = JOptionPane.showOptionDialog(
                    browserFrame,
                    "Are you sure you want to delete file " + fileName + "?",
                    "Confirm Deletion",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    buttons,
                    buttons[0]);

            switch (buttonSelection) {
                case JOptionPane.OK_OPTION:
                    if (selectedFile.delete()) {
                        currOpenFolder.setSubFiles();
                        if (contents.getLayout().getClass() == FlowLayout.class)
                            directoryContents(currOpenFolder);
                        else directoryList(currOpenFolder);
                        contents.addMouseListener(panelListener);
                        scrollContents = new JScrollPane(contents,
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        scrollContents.setPreferredSize(browserSize);
                        mainPanel.remove(splitPane);
                        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                scrollTree, scrollContents);
                        mainPanel.add(splitPane, BorderLayout.CENTER);
                        browserFrame.setContentPane(mainPanel);
                        browserFrame.pack();
                        browserFrame.setVisible(true);

                        selectedButton = null;
                    } else
                        System.out.println("File " + fileName + " not deleted");
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
                default:
                    System.out.println("Pane discarded");
            }
        }
        //DIRECTORY BUTTONS
        else if (selectedButton.getClass() == DirectoryButton.class) {
            DirectoryNode selectedNode = ((DirectoryButton) selectedButton).getMyDirectory();
            File selectedDir = selectedNode.getMyFile();
            String directoryName = selectedDir.getName();

            String[] buttons = {"Confirm", "Cancel"};
            int buttonSelection = JOptionPane.showOptionDialog(
                    browserFrame,
                    "Are you sure you want to delete file " + directoryName + "?",
                    "Confirm Deletion",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    buttons,
                    buttons[0]);

            switch (buttonSelection) {
                case JOptionPane.OK_OPTION:
                    deleteContents(selectedDir);

                    if (selectedDir.delete()) {
                        currOpenFolder.setSubFiles();
                        currOpenFolder.removeNode(selectedNode);

                        ((DefaultTreeModel) directoryTree.getModel()).reload(currOpenFolder);
                        if (currOpenFolder.equals(directoryTreeRoot)) {
                            TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                            directoryTree.setSelectionPath(selectedPath);
                        }

                        selectedButton = null;
                    } else
                        System.out.println("Directory " + directoryName + " not deleted");
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
                default:
                    System.out.println("Pane discarded");
            }
        }
    };

    //RENAME FROM BAR
    private static ActionListener rename = actionEvent -> {
        if (selectedButton == null) {
            System.out.println("Select file!");
        }
        //FILE BUTTONS
        else if (selectedButton.getClass() == FileButton.class) {
            File selectedFile = ((FileButton) selectedButton).getMyFile();
            String fileName = selectedFile.getName();

            JFrame renameFrame = createOptionFrame("Rename " + fileName);
            renameFrame.setTitle("Rename file");

            optionText.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                        File newNameFile = new File(
                                currOpenFolder.getMyFile().getPath() +
                                        separator + optionText.getText());
                        if (!newNameFile.exists()) {
                            if (selectedFile.renameTo(newNameFile)) {
                                currOpenFolder.setSubFiles();
                                if (contents.getLayout().getClass() == FlowLayout.class)
                                    directoryContents(currOpenFolder);
                                else directoryList(currOpenFolder);
                                contents.addMouseListener(panelListener);
                                scrollContents = new JScrollPane(contents,
                                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                scrollContents.setPreferredSize(browserSize);
                                mainPanel.remove(splitPane);
                                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        scrollTree, scrollContents);
                                mainPanel.add(splitPane, BorderLayout.CENTER);
                                browserFrame.setContentPane(mainPanel);
                                browserFrame.pack();
                                browserFrame.setVisible(true);
                                renameFrame.dispose();

                                selectedButton = null;
                            }
                            //RENAME ERROR
                            else {
                                JOptionPane.showMessageDialog(browserFrame, "Could not rename file",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "File already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        renameFrame.dispose();
                    }
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {

                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
            okButton.addActionListener(actionEvent1 -> {
                File newNameFile = new File(
                        currOpenFolder.getMyFile().getPath() +
                                separator + optionText.getText());
                if (!newNameFile.exists()) {
                    if (selectedFile.renameTo(newNameFile)) {
                        currOpenFolder.setSubFiles();
                        if (contents.getLayout().getClass() == FlowLayout.class)
                            directoryContents(currOpenFolder);
                        else directoryList(currOpenFolder);
                        contents.addMouseListener(panelListener);
                        scrollContents = new JScrollPane(contents,
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        mainPanel.remove(splitPane);
                        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                scrollTree, scrollContents);
                        mainPanel.add(splitPane, BorderLayout.CENTER);
                        browserFrame.setContentPane(mainPanel);
                        browserFrame.pack();
                        browserFrame.setVisible(true);
                        renameFrame.dispose();

                        selectedButton = null;
                    }
                    //RENAME ERROR
                    else {
                        JOptionPane.showMessageDialog(browserFrame, "Could not rename file",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                //"EXISTS" WARNING
                else {
                    JOptionPane.showMessageDialog(browserFrame, "File already exists",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            });

            Dimension browserFrameSize = browserFrame.getSize();
            Point browserFramePoint = browserFrame.getLocation();
            int X = (browserFrameSize.width - renameFrame.getWidth()) / 2;
            int Y = (browserFrameSize.height - renameFrame.getHeight()) / 2;
            renameFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
            renameFrame.setVisible(true);
        }
        //DIRECTORY BUTTONS
        else if (selectedButton.getClass() == DirectoryButton.class) {
            DirectoryNode selectedNode = ((DirectoryButton) selectedButton).getMyDirectory();
            File selectedDir = selectedNode.getMyFile();
            String directoryName = selectedDir.getName();

            JFrame renameFrame = createOptionFrame("Rename " + directoryName);
            renameFrame.setTitle("Rename directory");

            optionText.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                        File newNameDirectory = new File(
                                currOpenFolder.getMyFile().getPath() +
                                        "/" + optionText.getText());
                        if (!newNameDirectory.exists()) {
                            if (selectedDir.renameTo(newNameDirectory)) {
                                currOpenFolder.setSubFiles();
                                selectedNode.renameNode(newNameDirectory);
                                ((DefaultTreeModel) directoryTree.getModel()).reload(directoryTreeRoot);
                                if (currOpenFolder.equals(directoryTreeRoot)) {
                                    TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                                    directoryTree.setSelectionPath(selectedPath);
                                }
                                renameFrame.dispose();
                            }
                            //RENAME ERROR
                            else {
                                JOptionPane.showMessageDialog(browserFrame, "Could not rename directory",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        //"EXISTS" WARNING
                        else {
                            JOptionPane.showMessageDialog(browserFrame, "Directory already exists",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        renameFrame.dispose();
                    }
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {

                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
            okButton.addActionListener(actionEvent1 -> {
                File newNameDirectory = new File(
                        currOpenFolder.getMyFile().getPath() +
                                "/" + optionText.getText());
                if (!newNameDirectory.exists()) {
                    if (selectedDir.renameTo(newNameDirectory)) {
                        currOpenFolder.setSubFiles();
                        selectedNode.renameNode(newNameDirectory);
                        ((DefaultTreeModel) directoryTree.getModel()).reload(directoryTreeRoot);
                        if (currOpenFolder.equals(directoryTreeRoot)) {
                            TreePath selectedPath = new TreePath(currOpenFolder.getPath());
                            directoryTree.setSelectionPath(selectedPath);
                        }
                        renameFrame.dispose();
                    }
                    //RENAME ERROR
                    else {
                        JOptionPane.showMessageDialog(browserFrame, "Could not rename directory",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                //"EXISTS" WARNING
                else {
                    JOptionPane.showMessageDialog(browserFrame, "Directory already exists",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            });

            Dimension browserFrameSize = browserFrame.getSize();
            Point browserFramePoint = browserFrame.getLocation();
            int X = (browserFrameSize.width - renameFrame.getWidth()) / 2;
            int Y = (browserFrameSize.height - renameFrame.getHeight()) / 2;
            renameFrame.setLocation(browserFramePoint.x + X, browserFramePoint.y + Y);
            renameFrame.setVisible(true);
        }
    };

    //SEARCH FROM SEARCH BAR
    private static ActionListener searchButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            contents = new JPanel();
            contents.setLayout(new GridLayout(0, 4));
            Pattern searchPattern = Pattern.compile(searchText.getText());
            searchingList(currOpenFolder, searchPattern);
            contents.addMouseListener(panelListener);
            scrollContents = new JScrollPane(contents,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollContents.setPreferredSize(browserSize);
            mainPanel.remove(splitPane);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    scrollTree, scrollContents);
            mainPanel.add(splitPane, BorderLayout.CENTER);
            browserFrame.setContentPane(mainPanel);
            browserFrame.pack();
            browserFrame.setVisible(true);
        }
    };
    private static KeyListener searchTextListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
                contents = new JPanel();
                contents.setLayout(new GridLayout(0, 4));
                Pattern searchPattern = Pattern.compile(searchText.getText());
                searchingList(currOpenFolder, searchPattern);
                contents.addMouseListener(panelListener);
                scrollContents = new JScrollPane(contents,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollContents.setPreferredSize(browserSize);
                mainPanel.remove(splitPane);
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        scrollTree, scrollContents);
                mainPanel.add(splitPane, BorderLayout.CENTER);
                browserFrame.setContentPane(mainPanel);
                browserFrame.pack();
                browserFrame.setVisible(true);
            }
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {

        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

        }
    };

    private static void createFileBrowser() {
        JFrame.setDefaultLookAndFeelDecorated(false);

        browserFrame = new JFrame("CE325 File Browser");
        browserFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addMenu();
        browserFrame.setJMenuBar(menuBar);
        addMainPanel();
        browserFrame.setContentPane(mainPanel);
        browserFrame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int X = (screenSize.width - browserFrame.getWidth()) / 2;
        int Y = (screenSize.height - browserFrame.getHeight()) / 2;
        browserFrame.setLocation(X, Y);
        browserFrame.setVisible(true);
    }

    private static void addMenu() {
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenu createNew = new JMenu("Create New...");
        createNew.setMnemonic(KeyEvent.VK_C);
        JMenuItem createFile = new JMenuItem("File");
        createFile.addActionListener(createNewFile);
        createFile.setMnemonic(KeyEvent.VK_F1);
        JMenuItem createDir = new JMenuItem("Directory");
        createDir.setMnemonic(KeyEvent.VK_F2);
        createDir.addActionListener(createNewDirectory);
        JMenuItem deleteOption = new JMenuItem("Delete");
        deleteOption.setMnemonic(KeyEvent.VK_D);
        deleteOption.addActionListener(delete);
        JMenuItem renameOption = new JMenuItem("Rename");
        renameOption.setMnemonic(KeyEvent.VK_R);
        renameOption.addActionListener(rename);

        createNew.add(createFile);
        createNew.add(createDir);
        fileMenu.add(createNew);
        fileMenu.add(deleteOption);
        fileMenu.add(renameOption);

        menuBar.add(fileMenu);
    }

    private static void addMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        JPanel aboveMain = new JPanel(new BorderLayout());

        JButton convertButton = new JButton("Change Display");
        convertButton.addActionListener(actionEvent -> {
            //convert panel to list
            if (contents.getLayout().getClass() == FlowLayout.class){
                directoryList(currOpenFolder);
                contents.addMouseListener(panelListener);
                scrollContents = new JScrollPane(contents,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollContents.setPreferredSize(browserSize);
                mainPanel.remove(splitPane);
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        scrollTree, scrollContents);
                mainPanel.add(splitPane, BorderLayout.CENTER);

                browserFrame.setContentPane(mainPanel);
                browserFrame.pack();
                browserFrame.setVisible(true);
            }
            //convert list to panel
            else {
                directoryContents(currOpenFolder);
                contents.addMouseListener(panelListener);
                scrollContents = new JScrollPane(contents,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollContents.setPreferredSize(browserSize);
                mainPanel.remove(splitPane);
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        scrollTree, scrollContents);
                mainPanel.add(splitPane, BorderLayout.CENTER);

                browserFrame.setContentPane(mainPanel);
                browserFrame.pack();
                browserFrame.setVisible(true);
            }
        });
        aboveMain.add(convertButton, BorderLayout.WEST);
        addSearchPanel();
        aboveMain.add(searchPanel, BorderLayout.EAST);

        mainPanel.add(aboveMain, BorderLayout.NORTH);
        addSplitPane();
        mainPanel.add(splitPane, BorderLayout.CENTER);
    }

    private static void addSearchPanel() {
        searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton searchButton = new JButton("Start Search");
        searchButton.addActionListener(searchButtonListener);
        searchPanel.add(searchButton);

        searchText = new JTextField(20);
        searchText.addKeyListener(searchTextListener);
        searchText.setToolTipText("Search here");
        Dimension searchTextSize = new Dimension(searchText.getWidth(), searchButton.getPreferredSize().height);
        searchText.setPreferredSize(searchTextSize);
        searchPanel.add(searchText);
    }

    private static void addSplitPane() {
        createTree();
        scrollTree = new JScrollPane(directoryTree,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        directoryContents(currOpenFolder);
        contents.addMouseListener(panelListener);
        scrollContents = new JScrollPane(contents,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollContents.setPreferredSize(browserSize);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollTree, scrollContents);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
    }

    private static void createTree() {
        File userDir = new File(usersPath);
//        System.out.println(rootFile);
//        while (rootFile.getParentFile() != null)
//            rootFile = rootFile.getParentFile();
//        System.out.println(rootFile);
        directoryTreeRoot = new DirectoryNode(rootFile.getName(), rootFile.getPath());
        directoryTreeRoot.addSubDirectories();
        for (DirectoryNode entry : directoryTreeRoot.getSubFolders()) {
            entry.addSubDirectories();
            if (entry.getUserObject().equals(userDir.getParentFile().getName())){
                for (DirectoryNode homeEntry : entry.getSubFolders())
                    homeEntry.addSubDirectories();
            }
        }
        DirectoryNode homeNode = directoryTreeRoot.getDirectory(userDir.getParentFile().getName());
        DirectoryNode userNode = homeNode.getDirectory(userDir.getName());
        for (DirectoryNode entry : userNode.getSubFolders())
            entry.addSubDirectories();

        directoryTree = new JTree(directoryTreeRoot);
        directoryTree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent treeModelEvent) {

            }

            @Override
            public void treeNodesInserted(TreeModelEvent treeModelEvent) {

            }

            @Override
            public void treeNodesRemoved(TreeModelEvent treeModelEvent) {

            }

            @Override
            public void treeStructureChanged(TreeModelEvent treeModelEvent) {
                if (!currOpenFolder.equals(directoryTreeRoot)) {
                    if (contents.getLayout().getClass() == FlowLayout.class)
                        directoryContents(currOpenFolder);
                    else directoryList(currOpenFolder);
                    contents.addMouseListener(panelListener);
                    scrollContents = new JScrollPane(contents,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollContents.setPreferredSize(browserSize);
                    mainPanel.remove(splitPane);
                    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                            scrollTree, scrollContents);
                    mainPanel.add(splitPane, BorderLayout.CENTER);
                    browserFrame.setContentPane(mainPanel);
                    browserFrame.pack();
                    browserFrame.setVisible(true);
                }
            }
        });
        directoryTree.getSelectionModel().
                setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);


        TreePath userPath = new TreePath(userNode.getPath());
        directoryTree.expandPath(userPath);
        directoryTree.setSelectionPath(userPath);
        currOpenFolder = userNode;

        directoryTree.addTreeSelectionListener(treeSelectionEvent -> {
            TreePath oldPath = treeSelectionEvent.getOldLeadSelectionPath();
            TreePath newPath = treeSelectionEvent.getNewLeadSelectionPath();
            if (oldPath != null) {
                if (!oldPath.getLastPathComponent().equals(directoryTreeRoot) && !oldPath.isDescendant(newPath)) {
                    directoryTree.collapsePath(oldPath);
                }
            }
            if (newPath != null) {
                currOpenFolder = (DirectoryNode) newPath.getLastPathComponent();
                for (DirectoryNode entry : currOpenFolder.getSubFolders())
                    entry.addSubDirectories();

                if (contents.getLayout().getClass() == FlowLayout.class)
                    directoryContents(currOpenFolder);
                else directoryList(currOpenFolder);
                contents.addMouseListener(panelListener);
                scrollContents = new JScrollPane(contents,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollContents.setPreferredSize(browserSize);
                mainPanel.remove(splitPane);
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        scrollTree, scrollContents);
                mainPanel.add(splitPane, BorderLayout.CENTER);

                directoryTree.expandPath(newPath);
                browserFrame.setContentPane(mainPanel);
                browserFrame.pack();
                browserFrame.setVisible(true);
            }
        });
        directoryTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) {
                TreePath willExpandPath = treeExpansionEvent.getPath();
                DirectoryNode willExpandNode = (DirectoryNode) willExpandPath.getLastPathComponent();
                for (DirectoryNode entry : willExpandNode.getSubFolders())
                    entry.addSubDirectories();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) {

            }
        });

        ImageIcon folderIcon = createImageIcon(iconPath + "folder.png");
        //noinspection ConstantConditions
        if (folderIcon != null) {
            Image oldImg = folderIcon.getImage();
            Image newImg = oldImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            folderIcon = new ImageIcon(newImg);
            DefaultTreeCellRenderer folderCell = new DefaultTreeCellRenderer();
            folderCell.setClosedIcon(folderIcon);
            folderCell.setOpenIcon(folderIcon);
            folderCell.setLeafIcon(folderIcon);
            directoryTree.setCellRenderer(folderCell);
        } else {
            System.err.println("Folder icon missing; using default.");
        }
    }

    private static void directoryContents(DirectoryNode openFolder) {
        Set<File> subFiles = openFolder.getSubFiles();
        contents = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contents.setPreferredSize(browserSize);
        for (File file : subFiles) {
            //DirectoryButtons
            if (file.isDirectory()) {
                //create button
                DirectoryNode fileDirectory = openFolder.getDirectory(file.getName());
                DirectoryButton directoryButton = new DirectoryButton(file.getName(), fileDirectory);
                directoryButton.setIcon(createImageIcon(iconPath + "folder.png"));
                directoryButton.addMouseListener(folderButtonListener);

                //set background
                directoryButton.setBorderPainted(false);
                directoryButton.setContentAreaFilled(false);
//                directoryButton.setOpaque(false);

                //text alignment
                directoryButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                directoryButton.setHorizontalTextPosition(SwingConstants.CENTER);
                directoryButton.setToolTipText(file.getName());

                directoryButton.setPreferredSize(new Dimension(120, 90));
                contents.add(directoryButton);
            }
            //FileButtons
            else {
                //create button
                FileButton fileButton = new FileButton(file.getName(), file);
                fileButton.setIcon(createImageIcon(iconPath + fileButton.getExtension() + ".png"));
                fileButton.addMouseListener(fileButtonListener);

                //set background
                fileButton.setBorderPainted(false);
                fileButton.setContentAreaFilled(false);
//                fileButton.setOpaque(false);

                //text alignment
                fileButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                fileButton.setHorizontalTextPosition(SwingConstants.CENTER);
                fileButton.setToolTipText(file.getName());

                fileButton.setPreferredSize(new Dimension(120, 90));
                contents.add(fileButton);
            }
        }
    }

    private static void directoryList(DirectoryNode openFolder){
        Set<File> subFiles = openFolder.getSubFiles();
        contents = new JPanel();
        contents.setLayout(new GridLayout(0, 4));

        for (File file : subFiles) {
            //DirectoryButtons
            if (file.isDirectory()) {
                //create button
                DirectoryNode fileDirectory = openFolder.getDirectory(file.getName());
                DirectoryButton directoryButton = new DirectoryButton(file.getName(), fileDirectory);

                ImageIcon folderIcon = createImageIcon(iconPath + "folder.png");
                Image oldImg = folderIcon.getImage();
                Image newImg = oldImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                folderIcon = new ImageIcon(newImg);

                directoryButton.setIcon(folderIcon);
                directoryButton.addMouseListener(folderButtonListener);

                //set background
                directoryButton.setBorderPainted(false);
                directoryButton.setContentAreaFilled(false);
//                directoryButton.setOpaque(false);

                directoryButton.setToolTipText(file.getName());
                directoryButton.setHorizontalAlignment(SwingConstants.LEFT);
                contents.add(directoryButton);

                JLabel type = new JLabel(fileDirectory.getType());
                type.setHorizontalAlignment(SwingConstants.RIGHT);
                contents.add(type);

                JLabel size = new JLabel(fileDirectory.getFileSizeInKB());
                size.setHorizontalAlignment(SwingConstants.CENTER);
                contents.add(size);

                JLabel modifiedDate = new JLabel(fileDirectory.getLastModified());
                modifiedDate.setHorizontalAlignment(SwingConstants.LEFT);
                contents.add(modifiedDate);
            }
            //FileButtons
            else {
                //create button
                FileButton fileButton = new FileButton(file.getName(), file);

                ImageIcon fileIcon = createImageIcon(iconPath + fileButton.getExtension() + ".png");
                Image oldImg = fileIcon.getImage();
                Image newImg = oldImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                fileIcon = new ImageIcon(newImg);

                fileButton.setIcon(fileIcon);
                fileButton.addMouseListener(fileButtonListener);

                //set background
                fileButton.setBorderPainted(false);
                fileButton.setContentAreaFilled(false);
//                fileButton.setOpaque(false);

                fileButton.setToolTipText(file.getName());
                fileButton.setHorizontalAlignment(SwingConstants.LEFT);
                contents.add(fileButton);

                JLabel type = new JLabel(fileButton.getType());
                type.setHorizontalAlignment(SwingConstants.RIGHT);
                contents.add(type);

                JLabel size = new JLabel(fileButton.getFileSizeInKB());
                size.setHorizontalAlignment(SwingConstants.CENTER);
                contents.add(size);

                JLabel modifiedDate = new JLabel(fileButton.getLastModified());
                modifiedDate.setHorizontalAlignment(SwingConstants.LEFT);
                contents.add(modifiedDate);
            }
        }
    }

    //DIRECTORY CHECKS ITSELF AND SubFiles - NOT subDirectories
    private static void searchingList(DirectoryNode searchNode, Pattern searchPattern) {
        searchNode.addSubDirectories();
        File myFile = searchNode.getMyFile();
        Matcher searchMatcher = searchPattern.matcher(myFile.getName());
        if (searchMatcher.find()) {
            //create button
            DirectoryButton dirListButton = new DirectoryButton(myFile.getPath(), searchNode);

            ImageIcon folderIcon = createImageIcon(iconPath + "folder.png");
            Image oldImg = folderIcon.getImage();
            Image newImg = oldImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            folderIcon = new ImageIcon(newImg);

            dirListButton.setIcon(folderIcon);
            dirListButton.addMouseListener(folderButtonListener);

            //set background
            dirListButton.setBorderPainted(false);
            dirListButton.setContentAreaFilled(false);
//            directoryButton.setOpaque(false);

            dirListButton.setToolTipText(myFile.getName());
            dirListButton.setHorizontalAlignment(SwingConstants.LEFT);
            contents.add(dirListButton);

            JLabel type = new JLabel(searchNode.getType());
            type.setHorizontalAlignment(SwingConstants.RIGHT);
            contents.add(type);

            JLabel size = new JLabel(searchNode.getFileSizeInKB());
            size.setHorizontalAlignment(SwingConstants.CENTER);
            contents.add(size);

            JLabel modifiedDate = new JLabel(searchNode.getLastModified());
            modifiedDate.setHorizontalAlignment(SwingConstants.LEFT);
            contents.add(modifiedDate);
        }

        Set<File> subFiles = searchNode.getSubFiles();
        for (File file : subFiles) {
            //Directories
            if (file.isDirectory()) {
                searchingList(searchNode.getDirectory(file.getName()), searchPattern);
            }
            //Files
            else {
                searchMatcher = searchPattern.matcher(file.getName());
                if (searchMatcher.find()) {
                    //create button
                    FileButton fileListButton = new FileButton(file.getPath(), file);

                    ImageIcon fileIcon = createImageIcon(iconPath + fileListButton.getExtension() + ".png");
                    Image oldImg = fileIcon.getImage();
                    Image newImg = oldImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    fileIcon = new ImageIcon(newImg);

                    fileListButton.setIcon(fileIcon);

                    fileListButton.addMouseListener(fileButtonListener);

                    //set background
                    fileListButton.setBorderPainted(false);
                    fileListButton.setContentAreaFilled(false);
//                fileButton.setOpaque(false);

                    fileListButton.setToolTipText(file.getName());
                    fileListButton.setHorizontalAlignment(SwingConstants.LEFT);
                    contents.add(fileListButton);

                    JLabel type = new JLabel(fileListButton.getType());
                    type.setHorizontalAlignment(SwingConstants.RIGHT);
                    contents.add(type);

                    JLabel size = new JLabel(fileListButton.getFileSizeInKB());
                    size.setHorizontalAlignment(SwingConstants.CENTER);
                    contents.add(size);

                    JLabel modifiedDate = new JLabel(fileListButton.getLastModified());
                    modifiedDate.setHorizontalAlignment(SwingConstants.LEFT);
                    contents.add(modifiedDate);
                }
            }
        }
    }

    private static JFrame createOptionFrame(String frameLabel) {
        JFrame optionFrame = new JFrame(frameLabel);
        optionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel framePanel = new JPanel();
        framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.Y_AXIS));
        optionFrame.setContentPane(framePanel);

        JLabel panelLabel = new JLabel(frameLabel);
        panelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        framePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        framePanel.add(panelLabel);
        framePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel textSpace = new JPanel();
        optionText = new JTextField();
        optionText.setPreferredSize(new Dimension(250, 30));
        optionText.setAlignmentX(Component.CENTER_ALIGNMENT);
        textSpace.add(Box.createRigidArea(new Dimension(5, 0)));
        textSpace.add(optionText);
        textSpace.add(Box.createRigidArea(new Dimension(5, 0)));
        framePanel.add(textSpace);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        framePanel.add(buttonsPanel);
        okButton = new JButton("OK");
        buttonsPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(actionEvent -> optionFrame.dispose());
        buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonsPanel.add(cancelButton);

        optionFrame.setPreferredSize(new Dimension(400, 150));
        optionFrame.pack();
        return optionFrame;
    }

    private static boolean deleteContents(File toBeDeleted) {
        File[] dirFiles = toBeDeleted.listFiles();
        if (dirFiles != null) {
            for (File entry : dirFiles) {
                if (entry.isFile()) {
                    if (entry.delete())
                        System.out.println("File (in directory) deleted");
                } else if (entry.isDirectory()) {
                    if (deleteContents(entry))
                        if (entry.delete())
                            System.out.println("Directory (in directory) deleted");
                }
            }
            return true;
        } else {
            System.out.println("Directory is empty");
            return false;
        }
    }

    private static ImageIcon createImageIcon(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            return new ImageIcon(path);
        } else {
            System.err.println("Couldn't find file: " + path);
            return new ImageIcon(iconPath + "question.png");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileBrowser::createFileBrowser);
    }
}

package app;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class App extends Application {

    // all subclasses of the javaFXApplication class must implement the abstract
    // start() method of the Application class or be an abstract
    // subclass itself
    static String conFilter = " ";
    static String conDepth = " ";
    static Set<String> filepaths = new HashSet<>();
    static boolean clearButtonCreated;
    static List<String> extns = new ArrayList<String>();
    static String[] extnsArray;
    static Set<String> filesFound = new HashSet<String>();
    static int[] count = { 0 };
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static double cw = screenSize.getWidth() * 0.40; // we want prolly 33%
    static double ch = screenSize.getHeight() * 0.35; // 44%
    static boolean executed = false;
    // Each stage is a a window. the start method is passed the root stage on launch

    public boolean checkIfFileHasExtension(String s, String[] extns) {
        return Arrays.stream(extns).anyMatch(entry -> s.endsWith(entry));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // String confilters = " ";

        GridPane mainGrid = new GridPane();

        // mainGrid.setGridLinesVisible(true); // this is nice to visualize the rows and
        // columns of the layout
        TextField filters = new TextField();
        TextField toSearch = new TextField();
        TextField depth = new TextField();
        TextArea console = new TextArea();

        // CHECKBOX CREATION - DEPTHS
        CheckBox allcb = new CheckBox("All");
        CheckBox rootcb = new CheckBox("Root");

        // UI ELEMENTS FOR - ACTION SECTION
        CheckBox movecb = new CheckBox("Move");
        CheckBox copycb = new CheckBox("Copy");
        CheckBox deletecb = new CheckBox("Delete");
        CheckBox savecb = new CheckBox("SavePaths");

        Button moveBrowse = new Button("Browse");
        Button copyBrowse = new Button("Browse");
        Button saveBrowse = new Button("Browse");
        Button clearLocations = new Button("Clear");
        Button view = new Button("View");

        // moveBrowse.setVisible(false);

        // HORIZONTAL CONTAINER FOR DEPTH SECTION
        HBox hbox = new HBox();
        hbox.setSpacing(7);
        // hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.BASELINE_LEFT);
        hbox.getChildren().addAll(allcb, rootcb, depth);

        // HORIZONTAL CONTAINER FOR ACTION SECTION
        HBox hbox2 = new HBox();
        hbox2.setSpacing(7);
        // hbox.setPadding(new Insets(10));
        hbox2.setAlignment(Pos.BASELINE_LEFT);
        hbox2.getChildren().addAll(movecb, moveBrowse, deletecb, copycb, copyBrowse, savecb, saveBrowse);

        console.setEditable(false); // we just want the see the output of the program and not have it be changeable
        Button execute = new Button("Execute");
        Button browse = new Button("Browse");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src"));

        primaryStage.setTitle("JScraper");
        primaryStage.setResizable(true);

        // TOOLTIPS
        Tooltip filtersTip = new Tooltip("space seperated values of .extensions to look for: .txt .png .jpeg");
        Tooltip toSearchTip = new Tooltip("Browse to add locations");
        Tooltip executeTip = new Tooltip("Runs the program");
        Tooltip browseTip = new Tooltip("Click to open a file directory dialog");
        Tooltip depthTip = new Tooltip(
                "Sublevel search selection. Eg: if original location is ../desktop/pics inputing 2 for the depth would search user/desktop/pics/inner1/inner2 and will not traverse any deeper folders. This would be repeated for all directories in the starting location provided");

        // Attaching tooltips to the UI elements and PromptText
        filters.setTooltip(filtersTip);
        filters.setPromptText(".txt .png .jpg .etc");
        toSearch.setTooltip(toSearchTip);
        toSearch.setPromptText("Click browse to add search locations");
        execute.setTooltip(executeTip);
        browse.setTooltip(browseTip);
        depth.setTooltip(depthTip);
        depth.setPromptText("Choose level of search");

        toSearch.setEditable(false);

        /*
         * //realtime window height and width output on resize ChangeListener<Number>
         * stageSizeListener = (observable, oldValue, newValue) -> { console.clear();
         * console.appendText("Window Height: " + primaryStage.getHeight() + "\n" +
         * "Window Width: " + primaryStage.getWidth()); };
         * 
         * primaryStage.widthProperty().addListener(stageSizeListener);
         * primaryStage.heightProperty().addListener(stageSizeListener);
         */

        mainGrid.setVgap(5);
        mainGrid.setHgap(10);
        mainGrid.setPadding(new Insets(5, 5, 5, 5));

        ColumnConstraints column = new ColumnConstraints();

        column.setPercentWidth(15);
        mainGrid.getColumnConstraints().add(column);

        column = new ColumnConstraints();
        column.setPercentWidth(70);
        mainGrid.getColumnConstraints().add(column);

        column = new ColumnConstraints();
        column.setPercentWidth(15);
        mainGrid.getColumnConstraints().add(column);

        // LABELS WITH CORRESPONDING TEXTAREAS
        mainGrid.add(new Label("File Type"), 0, 0);
        mainGrid.add(filters, 1, 0);
        mainGrid.add(new Label("Root Location"), 0, 1);
        mainGrid.add(toSearch, 1, 1);
        mainGrid.add(new Label("Depth"), 0, 3);
        mainGrid.add(new Label("Action"), 0, 4);

        // CHECKBOXES
        /*
         * mainGrid.add(allcb,1,3); mainGrid.add(rootcb,1,3);
         * mainGrid.add(customcb,1,3);
         */
        mainGrid.add(hbox, 1, 3); // Wow this worked, the 3 checkboxs are stacked horizontally within 1 cell of
                                  // the grid, neat
        mainGrid.add(hbox2, 1, 4);
        // BUTTONS
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(7);
        buttonBox.setAlignment(Pos.BASELINE_LEFT);
        buttonBox.getChildren().addAll(browse);

        mainGrid.add(buttonBox, 2, 1);
        mainGrid.add(execute, 2, 14);

        // mainGrid.setHalignment(execute, HPos.CENTER);
        mainGrid.add(console, 0, 5, 2, 15);

        browse.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (!browse.getText().equals("Add")) {
                browse.setText("Add");
            }

            if (!clearButtonCreated) {
                buttonBox.getChildren().add(clearLocations); // add the clear button
                clearButtonCreated = true;
            }
            if (!filepaths.contains(selectedDirectory.getAbsolutePath())) {
                filepaths.add(selectedDirectory.getAbsolutePath()); // add the file path to the hash set
                toSearch.appendText(selectedDirectory.getAbsolutePath() + " ");
                console.appendText("Location Added: " + selectedDirectory.getAbsolutePath() + "\n");
            } else {
                console.appendText("That location is already added\n");
            }

        });

        clearLocations.setOnAction(e -> {

            filepaths.clear();
            filesFound.clear();
            toSearch.clear();
            console.appendText("Locations Cleared\n");

        });

        depth.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !depth.getText().equals("") && !depth.getText().trim().equals(conDepth)) {
                conDepth = depth.getText().trim();
                console.appendText("Custom Depth: " + depth.getText().trim() + "\n");
                allcb.setSelected(false);
                rootcb.setSelected(false);

            }

            depth.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER) && depth.isFocused() == true) {

                    // extns.add(filters.getText().trim().replaceAll("\\s{2,}", " "));
                    if (!depth.getText().isEmpty() && !conDepth.equals(depth.getText())) {
                        conDepth = depth.getText();
                        console.appendText("Custom Depth: " + depth.getText().trim() + "\n");
                        allcb.setSelected(false);
                        rootcb.setSelected(false);
                    }
                    mainGrid.requestFocus();
                }
            });

        });

        allcb.selectedProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal && !rootcb.isSelected()) {
                depth.clear();
                conDepth = Integer.toString((Integer.MAX_VALUE));
                if (executed) {
                    view.setVisible(false);
                } else {
                    view.setVisible(true);
                }
                console.appendText("Full Depth selected\n");

            }

            if (newVal && rootcb.isSelected()) {
                depth.clear();
                rootcb.setSelected(false);
                conDepth = Integer.toString((Integer.MAX_VALUE));
                if (executed) {
                    view.setVisible(false);
                } else {
                    view.setVisible(true);
                }
                console.appendText("Full Depth selected\n");

            }

        });

        rootcb.selectedProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal && !allcb.isSelected()) {
                conDepth = Integer.toString(1);
                depth.clear();
                // allcb.setSelected(false);
                if (executed) {
                    view.setVisible(false);
                } else {
                    view.setVisible(true);
                }
                console.appendText("Root Depth Selected (1 level) no subdirectories will be checked\n");
            }
            if (newVal && allcb.isSelected()) {
                depth.clear();
                allcb.setSelected(false);
                conDepth = Integer.toString(1);
                if (executed) {
                    view.setVisible(false);
                } else {
                    view.setVisible(true);
                }
                console.appendText("Root Depth Selected (1 level) no subdirectories will be checked\n");

            }

        });

        filters.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // System.out.println("Value of filters: " + filters.getText());

            if ((!newVal && !filters.getText().equals("") && !filters.getText().trim().equals(conFilter))) {
                conFilter = filters.getText().trim();

                extns.clear();
                filesFound.clear();
                extns.add(filters.getText().trim().replaceAll("\\s{2,}", " "));
                if (executed) {
                    view.setVisible(false);
                } else {
                    view.setVisible(true);
                }
                console.appendText("File Extensions : " + extns.toString() + "\n");

            }

            filters.setOnKeyPressed(event -> {

                if (event.getCode().equals(KeyCode.ENTER) && filters.isFocused() == true) {

                    // extns.add(filters.getText().trim().replaceAll("\\s{2,}", " "));
                    if (!extns.isEmpty() && !extns.contains(filters.getText().trim().replaceAll("\\s{2,}", " "))) {
                        extns.clear();
                        filesFound.clear();
                        extns.add(filters.getText().trim().replaceAll("\\s{2,}", " "));
                        if (executed) {
                            view.setVisible(false);
                        } else {
                            view.setVisible(true);
                        }
                        console.appendText("File Extensions : " + extns.toString() + "\n");
                    }
                    mainGrid.requestFocus();
                }
            });

        });

        execute.setOnAction(e -> {
            new Thread(() -> {
                filesFound.clear(); // reset the result space on execute
                // System.out.println("Allcb: "+allcb.isSelected() +"\nrootcb:
                // "+rootcb.isSelected());

                if ((((allcb.isSelected() || rootcb.isSelected() || !depth.getText().trim().isEmpty()) == false)
                        || ((!filters.getText().trim().isEmpty() && !toSearch.getText().trim().isEmpty())) == false)) {
                    console.appendText("Please fill in Extensions, location, and select/input a Depth level\n");
                    return;
                }

                executed = true;

                // this is where the big boy algorithm comes into play. luckily java13 has a
                // very elegant solution for this
                extnsArray = new String[extns.size()];
                extnsArray = extns.toString().replaceAll("[\\[\\]\\(\\)]", "").split(" ");
                int[] count = { 0 };
                // TODO : improve this to actually handle multiple file paths like when the y
                // click add more locations
                for (String path : filepaths) {

                    try {
                        Files.walkFileTree(Paths.get(path),
                                new HashSet<FileVisitOption>(Arrays.asList(FileVisitOption.FOLLOW_LINKS)),
                                Integer.valueOf(conDepth), new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                            throws IOException {

                                        // System.out.printf("Visiting file %s\n", file);
                                        ++count[0];

                                        if (checkIfFileHasExtension(file.toString(), extnsArray)) {
                                            filesFound.add(file.toString()); // already checks for uniqueness

                                            Platform.runLater(() -> {
                                                console.clear();
                                                console.appendText(
                                                        "=====================CRAWLING========================\n");
                                                console.appendText("Found: " + filesFound.size() + " \nTraversals: "
                                                        + count[0] + "\n");
                                            });

                                        }
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFileFailed(Path file, IOException e)
                                            throws IOException {
                                        // console.appendText("Access Denied, Skipped: " + file + "\n");
                                        return FileVisitResult.SKIP_SUBTREE;
                                    }

                                    @Override
                                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                            throws IOException {
                                        // System.out.printf("About to visit directory %s\n", dir);
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                    } catch (IOException j) {
                        // handle exception
                    }

                    /*
                     * //this version is slick with streams BUT the java 8 file.walk cannot handle
                     * accessdenied's in the stream and will fail upon a fileaccessdenied wheres the
                     * less slick above java 7 version skips over that subdirectory try
                     * (Stream<Path> walk = Files.walk(Paths.get(path), Integer.valueOf(conDepth)))
                     * {
                     * 
                     * 
                     * List<String> result = walk.map(x -> x.toString()) .filter(f ->
                     * checkIfFileHasExtension(f,extnsArray)).collect(Collectors.toList());
                     * 
                     * filesFound = new HashSet<String>(result); //now we have the entire hashset of
                     * file paths //f.endsWith(".txt")).collect(Collectors.toList()); } catch
                     * (UncheckedIOException j) { console.appendText("Access Denied error\n");
                     * console.appendText(j.toString()); j.printStackTrace(); } catch(IOException
                     * k){ k.printStackTrace(); } }
                     */
                }

                // console.appendText("Found: " + filesFound.size()+ " \nTraversals: " +
                // count[0] + "\n");
                // String joined = String.join("\n", filesFound);

                // implement a view screen so the user can quickly see all the paths
                // add our view results button under execute

                Platform.runLater(() -> {
                    console.clear();
                    console.appendText("==========================DONE====================\n");
                    console.appendText("Found: " + filesFound.size() + " \nTraversals: " + count[0] + "\n");
                    if (executed && !mainGrid.getChildren().contains(view)) {

                        mainGrid.add(view, 2, 15); // only show this button upon succesful execution parameters
                    }

                    else {
                        view.setVisible(true);
                    }
                });

            }).start();

        }); // now an execution has been run we need to display the data/give and manipulate
            // it

        view.setOnAction(e -> {

            // implement the new sscene
            // splits all the paths by new lines and makes a giant ass string on them
            String joined = String.join("\n", filesFound);

            // ObservableList<String> resultPaths =
            // FXCollections.observableArrayList(joined);

            // ListView<String> listView = new ListView<String>(resultPaths);

            TextArea viewPaths = new TextArea();

            GridPane resultsPane = new GridPane();
            Stage win2 = new Stage();
            resultsPane.setVgap(5);
            resultsPane.setHgap(10);
            resultsPane.setPadding(new Insets(5, 5, 5, 5));

            RowConstraints row = new RowConstraints();

            row.setPercentHeight(5);
            resultsPane.getRowConstraints().add(row);

            row = new RowConstraints();
            row.setPercentHeight(95);
            resultsPane.getRowConstraints().add(row);

            ColumnConstraints col = new ColumnConstraints();

            col.setPercentWidth(10);
            resultsPane.getColumnConstraints().add(col);

            col = new ColumnConstraints();
            col.setPercentWidth(75);
            resultsPane.getColumnConstraints().add(col);

            col = new ColumnConstraints();
            col.setPercentWidth(15);
            resultsPane.getColumnConstraints().add(col);

            HBox controls = new HBox(); // top controls

            controls.setSpacing(15);
            controls.setAlignment(Pos.BASELINE_CENTER);
            // we need checkboxs depending on the extensions and file locations
            Label records = new Label("Records: ");
            CheckBox allResults = new CheckBox("All");
            allResults.setSelected(true);

            // We need checkboxs for each file type
            controls.getChildren().add(allResults);

            for (String s : extnsArray) {

                controls.getChildren().add(new CheckBox(s));

            }

            // We need checkboxs for each file location as well
            for (String s : filepaths) {

                controls.getChildren().add(new CheckBox(s));
            }

            viewPaths.appendText(joined); // add the file paths to the textarea
            records.setStyle("-fx-font: 24 arial; -fx-color: red");
            resultsPane.add(controls, 0, 0, 3, 1); // horizontal top row control box
            resultsPane.add(viewPaths, 0, 1, 2, 1); // Second fat row
            resultsPane.add(records, 2, 1);

            Scene results = new Scene(resultsPane, cw * 1.5, ch * 2);
            records.setText(Integer.toString(filesFound.size()));

            // all the file paths unfiltered
            win2.setScene(results);
            win2.setTitle("All Files Found");
            win2.show();

            // Now we need to handle filtering the data on checkbox clicks

            for (Node child : controls.getChildren()) {
                if (child instanceof CheckBox) {

                    // look for the checkboxs
                    // add listeners

                    ((CheckBox) child).selectedProperty().addListener((obs, oldVal, newVal) -> {

                        if (newVal && ((CheckBox) child).getText().equals("All") && ((CheckBox) child).isSelected()) {

                            for (Node child1 : controls.getChildren()) {
                                if (child1 instanceof CheckBox && !((CheckBox) child1).getText().equals("All")) {
                                    ((CheckBox) child1).setSelected(false);
                                }
                            }
                            viewPaths.clear();
                            viewPaths.appendText(joined);
                            records.setText(Integer.toString(filesFound.size()));

                        }

                        else if (newVal && ((CheckBox) child).isSelected()) {

                            // String temp = viewPaths.getText();
                            String criteria = ((CheckBox) child).getText();

                            // List<String> boxText = Arrays.asList( (viewPaths.getText()).split("\n"));

                            List<String> temp = filesFound.stream().filter(str -> str.contains(criteria))
                                    .collect(Collectors.toList());

                            String new1 = String.join("\n", temp);
                            if (allResults.isSelected()) {
                                allResults.setSelected(false);
                                viewPaths.clear();
                                viewPaths.appendText(new1);
                                List<String> boxText = Arrays.asList((viewPaths.getText()).split("\n"));

                                records.setText(Integer.toString(boxText.size()));

                            }

                            else {
                                viewPaths.appendText(new1);
                                List<String> boxText = Arrays.asList((viewPaths.getText()).split("\n"));
                                records.setText(Integer.toString(boxText.size()));

                            }

                        }

                        if ((!newVal && !(((CheckBox) child).isSelected()))) {

                            if ((((CheckBox) child).getText().equals("All"))) {
                                viewPaths.clear();
                                records.setText("0");
                            }
                            String criteria = ((CheckBox) child).getText();

                            List<String> boxText = Arrays.asList((viewPaths.getText()).split("\n"));

                            List<String> temp = boxText.stream().filter(str -> str.contains(criteria))
                                    .collect(Collectors.toList());

                            List<String> newBox = new ArrayList<>(boxText);
                            newBox.removeAll(new HashSet<String>(temp));

                            String new2 = String.join("\n", newBox);
                            records.setText(Integer.toString(newBox.size()));
                            viewPaths.clear();
                            viewPaths.setText(new2);

                        }

                    });

                }
            }

        });

        Scene mainScene = new Scene(mainGrid, cw, ch);
        primaryStage.setScene(mainScene);
        primaryStage.show(); // the stage pobject is created by javafx runtime. you must use .show by default
                             // it is hidden

    }

    public static void main(String args[]) {
        Application.launch(args);
    }

}
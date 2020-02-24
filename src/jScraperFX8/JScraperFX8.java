package jScraperFX8;

/*
 * @author Rogers Abraham
 * @description: A JavaFX based file scraper.
 * @date: 2020/02/12
 * @copyright: Open to anyone free of use free of charge free to develop
 * 
 * Features: 
 * 			Pretty JavaFX GUI
 * 			Searching Multiple file extensions across multiple file directories with the option of customizing search depth
 * 			Viewing All Found files in a seperate scrollable window with filter boxs for extensions and directories showing that also shows total records found per each selected filter
 * 			Move/Copy/Delete/Save as .txt (file paths) of any combination of found file paths based on filters applied e.g. move just .txt files from ../Desktop to /<your_directory>
 * 
 * 
 * To Do:
 * 		   Command line argument capabilities
 * 		   Clean up and optimize the file filtering/walking/Manipulation routines. A bit to many uneeded recalcs and suboptimal data structure usage.
 * 		   Split functionality into seperate classes/be smarter about code organization and modularization
 * 			
 * Requires: JRE8 to run and develop.
 * *Note to devs: I suggest using eclipse, selecting javaSE1.8 as your dev enviroment
 *  Tested and working on:  Windows 10 64-bit
 * 
 */

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.stage.Modality;
import javafx.stage.Stage;



public class JScraperFX8 extends Application {
	
	
	
	public static void main(String args[]) {	 
		Application.launch(args);
	}

	// all subclasses of the javaFXApplication class must implement the abstract
	// start() method of the Application class or be an abstract
	// subclass itself

	// ===================================================================== GLOBAL
	// DATA STRUCTURES
	// ===============================================================================================================
	
	// STRINGS
	static String conFilter = " ";
	static String conDepth = " ";
	static String[] extnsArray;
	static String saveDirectory ="";
	static String copyDirectory="";
	static String moveDirectory="";

	// BOOLEANs
	static boolean clearButtonCreated;
	static boolean executed = false;
	static boolean validFilters = false;
	static boolean validLocations = false;
	static boolean checkboxsSpawned = false;

	// COLLECTIONS
	static List<String> extns = new ArrayList<String>();
	static Set<String> filepaths = new HashSet<>();
	static Set<String> filesFound = new HashSet<String>();
	static Set<String> extnCriteria = new HashSet<String>();
	static Set<String> locationCriteria = new HashSet<String>();
	static HashMap<String, String> constraintData = new HashMap<>();


	// REST / MISC
	static int[] count = { 0 };
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	static double cw = screenSize.getWidth() * 0.63; // we want prolly 33%
	static double ch = screenSize.getHeight() * 0.32; // 44%

	// ========================================================== MAIN JSCRAPER GUI
	// INTERFACE COMPONENTS
	// =========================================================================================================

	static GridPane mainGrid = new GridPane();

	// mainGrid.setGridLinesVisible(true); // this is nice to visualize the rows and
	// columns of the layout
	static TextField filters = new TextField();
	static TextField toSearch = new TextField();
	static TextField depth = new TextField();
	static TextArea console = new TextArea();

	// CHECKBOX FOR DEPTH SECTION
	static CheckBox rootcb = new CheckBox("Root");
	static CheckBox allcb = new CheckBox("All");

	// UI ELEMENTS FOR - ACTION SECTION
	static CheckBox movecb = new CheckBox("Move");
	static CheckBox copycb = new CheckBox("Copy");
	static CheckBox deletecb = new CheckBox("Delete");
	static CheckBox savecb = new CheckBox("SavePaths");
	static CheckBox viewcb = new CheckBox("View");

	static Button moveBrowse = new Button("Browse");
	static Button copyBrowse = new Button("Browse");
	static Button saveBrowse = new Button("Browse");
	static Button clearLocations = new Button("Clear");
	static Button view = new Button("View");
	static Button execute = new Button("Execute");

	// LABELS
	static Label labelFileType = new Label("  File Types:");
	static Label labelLocations = new Label("  Directories:");
	static Label labelDepth = new Label("  Depth:");
	static Label labelAction = new Label("  Action:");

	static Scene mainScene;

	// TOOLTIPS

	static Tooltip filtersTip = new Tooltip("space seperated values of .extensions to look for: .txt .png .jpeg");
	static Tooltip toSearchTip = new Tooltip("Browse to add locations");
	static Tooltip executeTip = new Tooltip("Runs the program");
	static Tooltip browseTip = new Tooltip("Click to open a file directory dialog");
	static Tooltip depthTip = new Tooltip(
			"Sublevel search selection. Eg: if original location is ../desktop/pics inputing 2 for the depth would search user/desktop/pics/inner1/inner2 and will not traverse any deeper folders. This would be repeated for all directories in the starting location provided");

	// ========================================================== VIEW WINDOW GUI
	// INTERFACE COMPONENTS
	// =========================================================================================================
	static String fpTextAreaString;
	static TextArea viewPaths = new TextArea();
	static GridPane resultsPane = new GridPane();
	static Stage win2 = new Stage();
	static RowConstraints row = new RowConstraints();
	static ColumnConstraints col = new ColumnConstraints();
	static Scene results;
	static HBox controls = new HBox();
	static Label records = new Label("Records: ");
	static CheckBox allResults = new CheckBox("All");

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		
		
		// HORIZONTAL CONTAINER FOR DEPTH SECTION
		HBox hbox = new HBox();
		hbox.setSpacing(7);
		hbox.setAlignment(Pos.BASELINE_LEFT);
		hbox.getChildren().addAll(allcb, rootcb, depth);
		viewcb.setSelected(true); //The default action on execute is selected to viewing file paths
		
		// Formatting the extension checkbox hbox control
		controls.setSpacing(20);
		controls.setAlignment(Pos.BASELINE_LEFT);

		// HORIZONTAL CONTAINER FOR ACTION SECTION
		HBox hbox2 = new HBox();
		hbox2.setSpacing(10);
		// hbox.setPadding(new Insets(10));
		hbox2.setAlignment(Pos.BASELINE_LEFT);
		hbox2.getChildren().addAll(viewcb, movecb, deletecb, copycb,  savecb);

		console.setEditable(false); // we just want the see the output of the program and not have it be changeable	
		Button browse = new Button("Browse");
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		primaryStage.setTitle("jScraper");
		primaryStage.setResizable(true);

		// STYLING TOOLTIPS
		filtersTip.setStyle("-fx-text-fill: lightseagreen;");
		toSearchTip.setStyle("-fx-text-fill: lightseagreen;");
		depthTip.setStyle("-fx-text-fill: lightseagreen;");
		browseTip.setStyle("-fx-text-fill: lightseagreen;");

		// ATTACHING
		filters.setTooltip(filtersTip);
		filters.setPromptText(".txt .png .jpg .etc");
		toSearch.setTooltip(toSearchTip);
		toSearch.setPromptText("Click browse to add search locations");
		toSearch.setEditable(false);
		execute.setTooltip(executeTip);
		browse.setTooltip(browseTip);
		depth.setTooltip(depthTip);
		depth.setPromptText("Choose level of search");

		// FORMATTING THE MAIN SCREEN UI COLUMNS ROWS AND FORMATTING
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

		// LABEL STYLING
		labelFileType.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");
		labelLocations.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");
		labelDepth.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");
		labelAction.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");
		//records.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");

		// TEXT AREA STYLING FOR BACKGROUND COLORS AND PROMPT TEXT
		filters.setStyle("-fx-background-color: lightslategray; -fx-prompt-text-fill: #EFBCD5;");
		toSearch.setStyle("-fx-background-color: lightslategray; -fx-prompt-text-fill: #EFBCD5;");
		depth.setStyle("-fx-background-color: lightslategray; -fx-prompt-text-fill: #EFBCD5;");
		console.setStyle(
				"-fx-control-inner-background: lightslategray; -fx-text-fill: lightgreen; -fx-font-weight: bold; -fx-prompt-text-fill: white; -fx-font-size: 15;");

		mainGrid.add(labelFileType, 0, 0);
		mainGrid.add(filters, 1, 0);
		mainGrid.add(labelLocations, 0, 1);
		mainGrid.add(toSearch, 1, 1);
		mainGrid.add(labelDepth, 0, 3);
		mainGrid.add(labelAction, 0, 4);

		// ADDING THE 2 HORIZONAL COMPONENTS for the File Types and File Location rows
		// excluding the right most buttons because those are on the third column
		mainGrid.add(hbox, 1, 3);
		mainGrid.add(hbox2, 1, 4);

		// Adding Browse Button
		HBox buttonBox = new HBox();
		buttonBox.setSpacing(7);
		buttonBox.setAlignment(Pos.BASELINE_LEFT);
		buttonBox.getChildren().addAll(browse);

		// Adding some more things to the main screen grid
		mainGrid.add(buttonBox, 2, 1);
		mainGrid.add(execute, 2, 14);
		mainGrid.add(console, 0, 5, 2, 15);

		resultsPane.setVgap(5);
		resultsPane.setHgap(10);
		resultsPane.setPadding(new Insets(5, 5, 5, 5));

		// ===================================================================SCREEN 2
		// SETUP=====================================================================
		row.setPercentHeight(5);
		resultsPane.getRowConstraints().add(row);
		row = new RowConstraints();
		row.setPercentHeight(95);
		resultsPane.getRowConstraints().add(row);
		col.setPercentWidth(10);
		resultsPane.getColumnConstraints().add(col);
		col = new ColumnConstraints();
		col.setPercentWidth(75);
		resultsPane.getColumnConstraints().add(col);
		col = new ColumnConstraints();
		col.setPercentWidth(15);
		resultsPane.getColumnConstraints().add(col);
		results = new Scene(resultsPane, cw * 1.5, ch * 2);
		win2.initModality(Modality.APPLICATION_MODAL);
		

		// ===========================================================================HANDLING
		// EVENTS==============================================================

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
				toSearch.setStyle("-fx-background-color: darkseagreen");
				
				validLocations = true;
				if(validFilters) {actionSectionRoutine();}
				

			} else {
				console.appendText("That location is already added\n");
			}

		});
		
		savecb.selectedProperty().addListener((obs, oldVal, newVal) -> {
			

			if (newVal && savecb.isSelected()) {
				
				//We want a file dialog
				//viewcb.setSelected(false);
				File selectedDirectory = directoryChooser.showDialog(primaryStage);
				saveDirectory = selectedDirectory.getAbsolutePath();
				console.appendText("Found File Paths will be saved to: " + saveDirectory+"\n");
			

			}

			else {
				saveDirectory = "";
				console.appendText("File paths will not be saved\n");
			}

		});
		
			movecb.selectedProperty().addListener((obs, oldVal, newVal) -> {
			

			if (newVal && movecb.isSelected()) {
				
				//We want a file dialog
				File selectedDirectory = directoryChooser.showDialog(primaryStage);
				moveDirectory = selectedDirectory.getAbsolutePath();
				console.appendText("Found files will be moved to: " + moveDirectory+"\n");

			}

			else {
				moveDirectory = "";
				console.appendText("Unselected Move files Option\n");
			}
		});
		
		deletecb.selectedProperty().addListener((obs, oldVal, newVal) -> {
			

			if (newVal && deletecb.isSelected()) {
				movecb.setSelected(false);
				copycb.setSelected(false);
				
				console.appendText("Found Files will try to be deleted\n");

			}

			else {
				
				console.appendText("Unselected Delete files Option\n");
			}

		});
		
		
		copycb.selectedProperty().addListener((obs, oldVal, newVal) -> {
		

			if (newVal && copycb.isSelected()) {
				deletecb.setSelected(false);
				movecb.setSelected(false);
				//We want a file dialog
				File selectedDirectory = directoryChooser.showDialog(primaryStage);
				copyDirectory = selectedDirectory.getAbsolutePath();
				console.appendText("Found files will be copied to: " + copyDirectory+"\n");

			}

			else {
				copyDirectory = "";
				console.appendText("Unselected Copy option\n");
			}

		});
		
		viewcb.selectedProperty().addListener( (obs, oldVal, newVal) -> {
			
			if (newVal && viewcb.isSelected()) {
				console.appendText("View found file paths option Selected\n");
			}
		
			else {
				console.appendText("View option Unselected\n");
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
					mainGrid.requestFocus();
				}
			});

		});

		allcb.selectedProperty().addListener((obs, oldVal, newVal) -> {

			if (newVal && !rootcb.isSelected()) {
				depth.clear();
				conDepth = Integer.toString((Integer.MAX_VALUE));
				console.appendText("Full Depth selected\n");

			}

			if (newVal && rootcb.isSelected()) {
				depth.clear();
				rootcb.setSelected(false);
				conDepth = Integer.toString((Integer.MAX_VALUE));
				console.appendText("Full Depth selected\n");

			}

		});

		rootcb.selectedProperty().addListener((obs, oldVal, newVal) -> {

			if (newVal && !allcb.isSelected()) {
				conDepth = Integer.toString(1);
				depth.clear();
				// allcb.setSelected(false);
				
				console.appendText("Root Depth Selected (1 level) no subdirectories will be checked\n");
			}
			if (newVal && allcb.isSelected()) {
				depth.clear();
				allcb.setSelected(false);
				conDepth = Integer.toString(1);
				
				console.appendText("Root Depth Selected (1 level) no subdirectories will be checked\n");
			}

		});

		filters.focusedProperty().addListener((obs, oldVal, newVal) -> {

			String cleanedFilters = (filters.getText().trim().replaceAll("\\s{2,}", " "));
			String cleanedExtns = extns.toString().replaceAll("[\\[\\]\\(\\)]", "").replaceAll(",", "");

			if ((!newVal && !cleanedFilters.contentEquals(cleanedExtns))) {

				extns.clear();
				filesFound.clear();
				extns.addAll(Arrays.asList(cleanedFilters.split(" ")).stream().distinct().collect(Collectors.toList()));
				cleanedExtns = extns.toString().replaceAll("[\\[\\]\\(\\)]", "").replaceAll(",", "");

				// this should check every entry for a . prefix and length over 1
				boolean dotcheck = extns.stream().allMatch(
						n -> n.startsWith(".") && n.length() > 1 && !n.substring(1, n.length()).contains("."));

				if (!dotcheck) {
					validFilters = false;
					filters.setStyle("-fx-background-color: lightpink");
					extns.clear();
					console.appendText(
							"Please ensure all extensions begin with a dot, do not contain intermediate dots, and are space seperated\n");
				}

				else {
					validFilters = true;
					if(validLocations) {actionSectionRoutine();}
					filters.setStyle("-fx-background-color: darkseagreen");
					filters.setText(cleanedExtns);
					console.appendText("File Extensions : " + cleanedExtns + "\n");

				}

			}

			filters.setOnKeyPressed(event -> {
				if (event.getCode().equals(KeyCode.ENTER) && filters.isFocused() == true) {
					mainGrid.requestFocus();
				}
			});

		});

	
		execute.setOnAction(e -> {
			new Thread(() -> {

				constraintData.clear();
				filesFound.clear(); 

				if ((((allcb.isSelected() || rootcb.isSelected() || !depth.getText().trim().isEmpty()) == false)
						|| ((!filters.getText().trim().isEmpty() && !toSearch.getText().trim().isEmpty())) == false)) 
				
				{
					console.appendText("Please fill in Extensions, location, and select/input a Depth level\n");
					return;
				}

				executed = true;
				
				//Check that if none of the action items are selected, set view on by defualt

// ========================================================= FILE WALKING ROUTING =================================================================================
				int[] count = { 0 };
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

										if (checkIfFileHasExtension(file.toString())) {
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
										//console.appendText("Access Denied, Skipped: " + file + "\n");
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

				}

				Platform.runLater(() -> {
					console.clear();
					console.appendText("==========================DONE========================\n");
					console.appendText("Found: " + filesFound.size() + " \nTraversals: " + count[0] + "\n");
					if (executed && !mainGrid.getChildren().contains(view) &&  viewcb.isSelected()) {
						
						mainGrid.add(view, 2, 16); // only show this button upon succesful execution parameters
					}

					else {
						view.setVisible(true);
					}
					
					
					
					fpTextAreaString = String.join("\n", filesFound);
					constraintData.put("All", fpTextAreaString);
					
					//if anything from the action hbox is selected besides view
					if(controls.getChildren().stream().anyMatch(y ->( ((CheckBox) y).isSelected() && !((CheckBox) y).getText().equals("View")) ) )
					{
					console.appendText("\n\n=============EXECUTING SELECTED ACTIONS===============\n");
					//console.appendText("Found: " + filesFound.size() + " \nTraversals: " + count[0] + "\n");
					
					//This is where the movecb, copycb savecb deletecb routine goes.
					if(savecb.isSelected()) {
						
						//console.appendText("Found Files Saved to: " + saveDirectory+ " as " + ".txt" +"\n");
						try {
							Files.write(Paths.get(saveDirectory+"/foundfiles.txt"), displayDataSet().getBytes());
							console.appendText("Found Files Saved to: " + saveDirectory+ " as " + "foundfiles.txt" +"\n");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
					
					if(movecb.isSelected()) {
						
						List<String> movepaths = Arrays.asList(displayDataSet().split("\n")); //just the filtered set with selections
						
						for(String path: movepaths) {
							
							try {
								Files.move(Paths.get(path), Paths.get(moveDirectory).resolve(Paths.get(path).getFileName()) , StandardCopyOption.REPLACE_EXISTING);
								
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						console.appendText("Found Files Moved to: " + moveDirectory + "\n");
						
					
					}
					
					if(copycb.isSelected()) {
						
						List<String> copypaths = Arrays.asList(displayDataSet().split("\n")); //just the filtered set with selections
						
						for(String path: copypaths) {
							
							try {
								Files.copy(Paths.get(path), Paths.get(copyDirectory).resolve(Paths.get(path).getFileName()) , StandardCopyOption.REPLACE_EXISTING);
								
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						console.appendText("Found Files Copied to: " + copyDirectory + "\n");
						
					
					}
					
					if(deletecb.isSelected()) {
						
						List<String> deletepaths = Arrays.asList(displayDataSet().split("\n")); //just the filtered set with selections
						
						for(String path: deletepaths) {
							
							try {
								Files.delete(Paths.get(path));
								
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						console.appendText("Deleted Files\n");
						
					
					}
					
					
					
					}
			
					
					
					

				});

				
			}).start();
			
			

		});

		// View Panel on button click for the results of the file Crawl
		view.setOnAction(e -> {
			
		
			//STYLING 
			viewPaths.setStyle(
					"-fx-control-inner-background: lightslategray; -fx-text-fill: lightgreen; -fx-font-weight: bold; -fx-prompt-text-fill: white; -fx-font-size: 15;");
			records.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold; -fx-font-size: 40");
			resultsPane.setStyle("-fx-background-color: slategray; "
					+ "		   -fx-font-family:Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New;"
					+ "			-fx-text-base-color: green;" + "			-fx-text-fill: green;");
			
			
			allResults.setSelected(true);
			viewPaths.appendText(constraintData.get("All")); // add the file paths to the textarea
			resultsPane.add(controls, 0, 0, 3, 1); // horizontal top row control box
			resultsPane.add(viewPaths, 0, 1, 2, 1); // Second fat row
			resultsPane.add(records, 2, 1);

			records.setText(Integer.toString(filesFound.size() - 1));

			win2.setScene(results);
			win2.setTitle("All Files Found");
			//viewPaths.setText
			win2.show();
			
			// Now we need to handle filtering the data on checkbox clicks
			// I should also pass this stuff of to another thread because its fairly heavy
			// to do on the main thread
			new Thread(() -> {

				// Every time the controls are changed from default "all" we can calculate the
				// data, store it, and display then on the
				// the next call with the same checkboxes we just display the stored dataset.
				// That we we dont recalculate everytime
				// We need a method of storing selected checkboxs and the data
				// <HashMap<List<String> constraints, String Dataset> = constraintsData
				// We have the entire found set: Set<String>filesFound = new HashSet<String>();
				
				//controls.getChildren().forEach(x -> ((CheckBox)x).setOnAction(null));
				
				for (Node child : controls.getChildren()) {

					// Use these: viewPaths.appendText(string)
					// constraintData(string key representing critertia)
					EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent arg0) {
							
							if(!controls.getChildren().stream().anyMatch(e -> ((CheckBox) e).isSelected())) {
								//means nothings selected
								locationCriteria.clear();
								extnCriteria.clear();
								viewPaths.setText(displayDataSet());
								return;
							}
							
							

							if (allResults.isSelected() && !((CheckBox) child).getText().equals("All")) {

								allResults.setSelected(false);

								if (((CheckBox) child).getId().equals("location")) {

									locationCriteria.add(((CheckBox) child).getText());
								}

								else {
									extnCriteria.add(((CheckBox) child).getText());
								}
								
								viewPaths.setText(displayDataSet());
								return;

							}
							
							if (allResults.isSelected()) {

								for (Node child : controls.getChildren()) {
									
									if (!((CheckBox) child).getText().equals("All"))

									{
										((CheckBox) child).setSelected(false);
									}
								}

								locationCriteria.clear();
								extnCriteria.clear();
								viewPaths.setText(displayDataSet());
								return;
							}
							
							if (((CheckBox)child).isSelected()) {

								if (((CheckBox) child).getId().equals("location")) {

									locationCriteria.add(((CheckBox) child).getText());
								}

								else {
									extnCriteria.add(((CheckBox) child).getText());
								}
								
								viewPaths.setText(displayDataSet());
								return;

							}


							if (!((CheckBox) child).isSelected()) {

								if (((CheckBox) child).getId().equals("location")) {

									locationCriteria.remove(((CheckBox) child).getText());
								}

								else {
									extnCriteria.remove(((CheckBox) child).getText());
								}
								viewPaths.setText(displayDataSet());
								return;
							}

							

							else {

								viewPaths.setText(displayDataSet());
							}

						}

					};

					((CheckBox) child).setOnAction(event);

				}

			}).start();

		});

		win2.setOnCloseRequest(event -> {
			mainGrid.add(controls, 1, 5);
			resultsPane.getChildren().clear();
			
			//view.setVisible(false);
			
		});
		
		mainGrid.setStyle("-fx-background-color: slategray; "
				+ "		   -fx-font-family:Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New;"
				+ "			-fx-text-base-color: green;" + "			-fx-text-fill: green;");
		mainScene = new Scene(mainGrid, cw, ch);
		primaryStage.setScene(mainScene);
		primaryStage.show(); // the stage pobject is created by javafx runtime. you must use .show by default

	}

	private void actionSectionRoutine() {
		CheckBox extnCB;
		CheckBox locationCB;
		//Insert the new checkboxs onto the grid
	
			controls.getChildren().clear();
			controls.getChildren().add(allResults);
			for (String s : extns) {
				extnCB = new CheckBox(s);
				extnCB.setId("extn");
				controls.getChildren().add(extnCB);
			}
			for (String s : filepaths) {
				locationCB = new CheckBox(s);
				locationCB.setId("location");
				controls.getChildren().add(locationCB);
			}
			GridPane.setRowIndex(execute, 15);
			GridPane.setRowIndex(console, 6);
			mainGrid.layout();
			if (!checkboxsSpawned) {
				mainGrid.add(controls, 1, 5, 3, 1);
				checkboxsSpawned = true;
			}

			allResults.setSelected(true);
			for (Node child : controls.getChildren()) {

				// Use these: viewPaths.appendText(string)
				// constraintData(string key representing critertia)
				EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						
						if(!controls.getChildren().stream().anyMatch(e -> ((CheckBox) e).isSelected())) {
							//means nothings selected
							locationCriteria.clear();
							extnCriteria.clear();
							return;
						}
						
						

						if (allResults.isSelected() && !((CheckBox) child).getText().equals("All")) {

							allResults.setSelected(false);

							if (((CheckBox) child).getId().equals("location")) {

								locationCriteria.add(((CheckBox) child).getText());
							}

							else {
								extnCriteria.add(((CheckBox) child).getText());
							}
							
							
							return;

						}
						
						if (allResults.isSelected()) {

							for (Node child : controls.getChildren()) {
								
								if (!((CheckBox) child).getText().equals("All"))

								{
									((CheckBox) child).setSelected(false);
								}
							}

							locationCriteria.clear();
							extnCriteria.clear();
							
							return;
						}
						
						if (((CheckBox)child).isSelected()) {

							if (((CheckBox) child).getId().equals("location")) {

								locationCriteria.add(((CheckBox) child).getText());
							}

							else {
								extnCriteria.add(((CheckBox) child).getText());
							}
							
							
							return;

						}


						if (!((CheckBox) child).isSelected()) {

							if (((CheckBox) child).getId().equals("location")) {

								locationCriteria.remove(((CheckBox) child).getText());
							}

							else {
								extnCriteria.remove(((CheckBox) child).getText());
							}
							
							return;
						}

						
					}

				};

				((CheckBox) child).setOnAction(event);

			}
		
		
	}
	
	
	
	
	public String displayDataSet() {

		String cleanedLocations = Arrays.asList(locationCriteria).toString().replaceAll("[\\[\\]\\(\\)]", "")
				.replaceAll(",", "").trim();
		String cleanedExtns = Arrays.asList(extnCriteria).toString().replaceAll("[\\[\\]\\(\\)]", "")
				.replaceAll(",", "").trim();
		String allSelected = (cleanedLocations + " " + cleanedExtns).trim(); // All Selected fields

		List<String> arrCL = Arrays.asList(cleanedLocations.split(" "));
		List<String> arrCE = Arrays.asList(cleanedExtns.split(" "));
		
		// locationCriteria extnCriteria

		if (allSelected.equals("") && !allResults.isSelected()) {
			
			records.setText("0");
			return fpTextAreaString = "Pssssst click those checkboxs up there ;)";
			
		}
		
		if (allSelected.equals("") && allResults.isSelected()) {
			
			records.setText(Integer.toString(Arrays.asList((constraintData.get("All").split("\n"))).size()));
			return fpTextAreaString = constraintData.get("All");
		}

		if (!constraintData.containsKey(allSelected)) {

			if (cleanedLocations.equals("")) {
				constraintData.put(cleanedExtns, String.join("\n",
						filesFound.stream()
						.filter(e -> arrCE
								.stream()
								.anyMatch(entry -> e.endsWith(entry)))
								.collect(Collectors.toList())));
			} else {
				constraintData.put(allSelected,
						String.join("\n",
								filesFound.stream()
								.filter(e -> arrCE
										.stream()
										.anyMatch(entry -> e.endsWith(entry)) && arrCL
										.stream().anyMatch(temp -> e.contains(temp)))
										.collect(Collectors.toList())));

			}

		}

		records.setText(Integer.toString(Arrays.asList((constraintData.get(allSelected).split("\n"))).size()));
		return fpTextAreaString = constraintData.get(allSelected);

	}

	public boolean checkIfFileHasExtension(String s) {
		return extns.stream().anyMatch(entry -> s.endsWith(entry));
	}



}

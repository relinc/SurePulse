package net.relinc.viewer.GUI;

import java.io.File;

import org.controlsfx.control.SegmentedButton;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class SampleDirectoryGUI extends CommonGUI {
	private HomeController homeController;
	
	public SampleDirectoryGUI(HomeController hc)
	{
		homeController = hc;
		
		// Directory GUI
		changeDirectoryButton.setGraphic(SPOperations.getIcon(SPOperations.folderImageLocation));
		sampleDirectoryTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		
		xButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if(homeController.homeSplitPane.getItems().size() > 2)
					homeController.homeSplitPane.getItems().remove(2);
				if(homeController.vBoxHoldingCharts.getChildren().size() > 1)
					homeController.vBoxHoldingCharts.getChildren().remove(1);
			}
		});

		addSelectedSampleButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//addSelectedSampleButton.getScene().setCursor(Cursor.WAIT); //dont know why this doesnt work. Need to set sample loading in another thread!
				realCurrentSamplesListView.getItems().removeListener(homeController.sampleListChangedListener);
				for(TreeItem<FileFX> item : sampleDirectoryTreeView.getSelectionModel().getSelectedItems()){
					processFile(item.getValue().file);
				}
				homeController.sampleListChangedListener.onChanged(null);
				realCurrentSamplesListView.getItems().addListener(homeController.sampleListChangedListener);
			}
			
			private void processFile(File dir)
			{
				if(dir.isDirectory()){
					for(File samFile : dir.listFiles()){
						processFile(samFile); // Could be a directory, need recursion
					}
				}
				else{
					if(realCurrentSamplesListView.getItems().stream().filter(sample -> sample.getName().equals(dir.toString())).count() > 0){
						Dialogs.showErrorDialog("Sample already added", "Cannot add sample twice", "Sample was not added",stage);
						return;
					}
					homeController.addSampleToList(dir.getPath());
				}
			}
		});
		
		loadSessionButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				FileFX selectedFile = sessionsListView.getSelectionModel().getSelectedItem();
				if(selectedFile == null || selectedFile.file.isDirectory())
				{
					Dialogs.showErrorDialog("Error", "No session selected", "Please select a session to load", stage);
					return;
				}
				
				homeController.applySession(selectedFile.file);
			}
		});

		changeDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				DirectoryChooser fileChooser = new DirectoryChooser();
				fileChooser.setTitle("Change Working Directory");
				File dir = fileChooser.showDialog(stage);
				if (dir != null) {
					File sampleDataDir = dir;
					File[] files = dir.listFiles();
					for(int i = 0; i < files.length; i++){
						if(files[i].isDirectory() && files[i].getName().equals("Sample Data"))
							sampleDataDir = files[i];
					}
					treeViewHomePath = sampleDataDir.getPath();
					fillAllSamplesTreeView();
					fillSessionsListView();
				}
			}
		});

		refreshDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				fillAllSamplesTreeView();
				fillSessionsListView();
			}
		});
		
		fillAllSamplesTreeView();
		fillSessionsListView();
	}

	public void showSampleDirectoryPane() {
		fillAllSamplesTreeView();
		//xButton.setBlendMode(BlendMode.HARD_LIGHT);
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");

		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		hBoxThatHoldsXButton.getChildren().add(new Label("All Samples in Directory"));


		VBox vbox = new VBox();
		HBox hBox = new HBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(addSelectedSampleButton);
		hBox.getChildren().add(refreshDirectoryButton);
		hBox.getChildren().add(changeDirectoryButton);
		vbox.getChildren().add(hBox);
		
		SegmentedButton b = new SegmentedButton();
		ToggleButton b1 = new ToggleButton("Samples");
		ToggleButton b2 = new ToggleButton("Sessions");
		b.getButtons().addAll(b1,b2);
		
		b1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				vbox.getChildren().remove(sessionsListView);
				vbox.getChildren().remove(loadSessionButton);
				if(!vbox.getChildren().contains(sampleDirectoryTreeView))
					vbox.getChildren().add(sampleDirectoryTreeView);
				if(!vbox.getChildren().contains(addSelectedSampleButton))
					vbox.getChildren().add(1,addSelectedSampleButton);
			}
		});
		b2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				vbox.getChildren().remove(sampleDirectoryTreeView);
				vbox.getChildren().remove(addSelectedSampleButton);
				if(!vbox.getChildren().contains(sessionsListView))
					vbox.getChildren().add(sessionsListView);
				if(!vbox.getChildren().contains(loadSessionButton))
					vbox.getChildren().add(1, loadSessionButton);
				fillSessionsListView();
			}
		});
		
		b.getButtons().get(0).setSelected(true);
		HBox segHBox = new HBox();
		segHBox.getChildren().add(b);
		segHBox.setAlignment(Pos.CENTER);
		vbox.getChildren().add(segHBox);
		
		
		vbox.getChildren().add(sampleDirectoryTreeView);
		
		
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		vbox.prefHeightProperty().bind(stage.getScene().heightProperty());
		optionPane.getChildren().clear();
		optionPane.getChildren().add(vbox);
		AnchorPane optionPane = new AnchorPane();
		optionPane.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);

		VBox.setVgrow(sampleDirectoryTreeView, Priority.ALWAYS);
		VBox.setVgrow(sessionsListView, Priority.ALWAYS);

		while(homeController.homeSplitPane.getItems().size() > 2)
			homeController.homeSplitPane.getItems().remove(2);
		homeController.homeSplitPane.getItems().add(optionPane);
		homeController.homeSplitPane.setDividerPosition(1, 1 - homeController.homeSplitPane.getDividerPositions()[0]);
	}
	
	public void fillAllSamplesTreeView(){
		findFiles(new File(treeViewHomePath), null);
		sampleDirectoryTreeView.setShowRoot(false);
	}
	
	public void fillSessionsListView()
	{
		sessionsListView.getItems().clear();
		File sessionsFile = new File(new File(treeViewHomePath).getParentFile(), "Sessions");
		if(!sessionsFile.exists() || !sessionsFile.isDirectory())
		{
			System.out.println(sessionsFile.toString() + " does not exist or is not a directory");
			return;
		}
		for(File f : sessionsFile.listFiles())
		{
			if(!f.isDirectory() && f.getPath().endsWith(".session"))
			{
				sessionsListView.getItems().add(new FileFX(f));
			}
		}
	}
	
	private void findFiles(File dir, TreeItem<FileFX> parent) {
		TreeItem<FileFX> root = new TreeItem<>(new FileFX(dir), SPOperations.getIcon(SPOperations.folderImageLocation));
		root.setExpanded(true);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root);
			} else {
				if(file.getName().endsWith(SPSettings.compressionExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));

				if(file.getName().endsWith(SPSettings.shearCompressionExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));

				if(file.getName().endsWith(SPSettings.tensionRectangularExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRectImageLocation)));

				if(file.getName().endsWith(SPSettings.tensionRoundExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRoundImageLocation)));
				if(file.getName().endsWith(SPSettings.loadDisplacementExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.loadDisplacementImageLocation)));

			}
		}
		if(parent==null){
			sampleDirectoryTreeView.setRoot(root);
		} else {

			parent.getChildren().add(root);
		}
	} 
	
	
}

package net.relinc.viewer.GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleTypes;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;

public class SampleDirectoryGUI extends CommonGUI {
	private HomeController homeController;
	ToggleButton samplesToggleButton;
	ToggleButton sessionsToggleButton;
	
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
			}
		});

		addSelectedSampleButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) { 
				realCurrentSamplesListView.getItems().removeListener(homeController.sampleListChangedListener);
				
				List<String> samplePaths = new ArrayList<String>();
				sampleDirectoryTreeView.getSelectionModel().getSelectedItems()
					.stream()
					.forEach(item -> fillSamplePaths(samplePaths, item.getValue().file));

				samplePaths.stream()
					.distinct()
					//.parallel() // empirically does not improve speed. 
					.map(path -> {
						Optional<Sample> sample = Optional.empty();
						try {
							sample = Optional.of(SPOperations.loadSample(path));
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						return sample;
					})
					.filter(Optional::isPresent)
					.map(Optional::get)
					.sequential() // Need to be on JavaFX thread to modify controls.
					.forEach(sample -> homeController.addSampleToList(sample));
				
				homeController.sampleListChangedListener.onChanged(null);
				realCurrentSamplesListView.getItems().addListener(homeController.sampleListChangedListener);
			}
			
			private void fillSamplePaths(List<String> samplePaths, File dir)
			{
				if(dir.isDirectory()) {
					Arrays.stream(dir.listFiles())
						.forEach(f -> fillSamplePaths(samplePaths, f));
				} else {
					samplePaths.add(dir.getPath());
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
		samplesToggleButton = new ToggleButton("Samples");
		sessionsToggleButton = new ToggleButton("Sessions");
		b.getButtons().addAll(samplesToggleButton,sessionsToggleButton);
		
		samplesToggleButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showSampleDirectoryControls(vbox);
			}
		});
		sessionsToggleButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showSessionControls(vbox);
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
	
	private void showSampleDirectoryControls(VBox vbox)
	{
		vbox.getChildren().remove(sessionsListView);
		vbox.getChildren().remove(loadSessionButton);
		if(!vbox.getChildren().contains(sampleDirectoryTreeView))
			vbox.getChildren().add(sampleDirectoryTreeView);
		if(!vbox.getChildren().contains(addSelectedSampleButton))
			vbox.getChildren().add(1,addSelectedSampleButton);
	}
	
	private void showSessionControls(VBox vbox)
	{
		vbox.getChildren().remove(sampleDirectoryTreeView);
		vbox.getChildren().remove(addSelectedSampleButton);
		if(!vbox.getChildren().contains(sessionsListView))
			vbox.getChildren().add(sessionsListView);
		if(!vbox.getChildren().contains(loadSessionButton))
			vbox.getChildren().add(1, loadSessionButton);
		fillSessionsListView();
	}
	
	public void fireSessionsToggleButton()
	{
		sessionsToggleButton.fire();
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
		root.setExpanded(false);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root);
			} else {
				SampleTypes.getSampleConstantsMap().values().forEach(sampleInfo -> {
					if(file.getName().endsWith(sampleInfo.getExtension())) {
						root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(sampleInfo.getIconLocation())));
					}
				});
			}
		}
		if(parent==null){
			root.setExpanded(true);
			sampleDirectoryTreeView.setRoot(root);
		} else {

			parent.getChildren().add(root);
		}
	} 
	
	
}

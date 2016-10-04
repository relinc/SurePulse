package net.relinc.viewer.GUI;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;

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
					if(item.getValue().file.isDirectory()){
						for(File samFile : item.getValue().file.listFiles()){
							if(realCurrentSamplesListView.getItems().stream().filter(sample -> sample.getName().equals(SPOperations.stripExtension(samFile.getName()))).count() > 0){
								Dialogs.showErrorDialog("Sample already added", "Cannot add sample twice", "Sample was not added",stage);
								return;
							}
							homeController.addSampleToList(samFile.getPath());
						}
					}
					else{
						if(realCurrentSamplesListView.getItems().stream().filter(sample -> sample.getName().equals(item.getValue().toString())).count() > 0){
							Dialogs.showErrorDialog("Sample already added", "Cannot add sample twice", "Sample was not added",stage);
							return;
						}
						homeController.addSampleToList(item.getValue().file.getPath());
					}
				}
				homeController.sampleListChangedListener.onChanged(null);
				realCurrentSamplesListView.getItems().addListener(homeController.sampleListChangedListener);
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
					homeController.fillAllSamplesTreeView();
				}
			}
		});

		refreshDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				homeController.fillAllSamplesTreeView();
			}
		});
	}
	
	
}

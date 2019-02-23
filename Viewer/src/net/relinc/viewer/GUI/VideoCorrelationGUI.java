package net.relinc.viewer.GUI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.ImageOps;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class VideoCorrelationGUI extends CommonGUI{
	private HomeController homeController;
	public List<File> imagePaths = new ArrayList<>();
	static Button videoDialogXButton = new Button("X");
	
	public VideoCorrelationGUI(HomeController hc)
	{
		homeController = hc;
		init();
	}
	
	private void init()
	{
		openImagesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Sample currentSample = getCheckedSamples().get(0);
				if(currentSample.getResults().size() != 1) {
					Dialogs.showErrorDialog("Can only have one sample result for this feature", homeController.stage);
					return;
				}
				LoadDisplacementSampleResults result = currentSample.getResults().get(0);

				FileChooser fileChooser = new FileChooser();
				imagePaths =
						fileChooser.showOpenMultipleDialog(stage);


				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(result.getDisplacementDataLocation());

				if(currentDisplacementDataSubset.Data.data.length != imagePaths.size()){
					Dialogs.showAlert("The number of images does not match the length of the displacement data.\n"
							+ "Number of images: " + imagePaths.size() + "\n"
									+ "Length of Displacement data: " + currentDisplacementDataSubset.Data.data.length, stage);
				}
				
				if(currentDisplacementDataSubset.getBegin() > imagePaths.size()){
					//badly off.
					imagePaths = new ArrayList<File>();
					Dialogs.showAlert("Displacement data begin Index is greater than number of images. Images not compatible.", stage);
					return;
				}

				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
				imageScrollBar.setMax(currentDisplacementDataSubset.getEnd());


				renderImageMatching();
			}

		});
		
		useSampleImages.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//button shouldn't get clicked unless sample has images.
				Sample currentSample = getCheckedSamples().get(0);
				if(currentSample.getResults().size() != 1) {
					Dialogs.showErrorDialog("Can only have one sample result for this feature", homeController.stage);
					return;
				}
				LoadDisplacementSampleResults result = currentSample.getResults().get(0);
				File tempImageLoadLocation = new File(SPSettings.applicationSupportDirectory + "/SurePulse/tempImagesForViewer");
				if(tempImageLoadLocation.exists())
					SPOperations.deleteFolder(tempImageLoadLocation);
				
				tempImageLoadLocation.mkdirs();
				
				File images = ImageOps.extractSampleImagesToDirectory(currentSample, tempImageLoadLocation);
				
				imagePaths = images == null ? null : Arrays.asList(images.listFiles());
				
				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(result.getDisplacementDataLocation());

				if(currentDisplacementDataSubset.Data.data.length != imagePaths.size()){
					Dialogs.showAlert("The number of images does not match the length of the displacement data", stage);
				}
				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
				imageScrollBar.setValue(imageScrollBar.getMin());
				imageScrollBar.setMax(currentDisplacementDataSubset.getEnd());

				renderImageMatching();
				
			}
		});

		saveVideoButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				double length = net.relinc.libraries.staticClasses.Dialogs.getDoubleValueFromUser("Please Enter the desired video length (seconds):", "seconds");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save Video");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp4 Video(*.mp4)", "*.mp4"));
				fileChooser.setInitialFileName("*.mp4");
				File file = fileChooser.showSaveDialog(stage);


				try {
					File garbageImages = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREANALYZE/GarbageImages");
					if(garbageImages.exists())
						SPOperations.deleteFolder(garbageImages);
					garbageImages.mkdirs();
					int begin = (int)imageScrollBar.getMin();
					int end = (int)imageScrollBar.getMax();
					imageMatchingChart.setAnimated(false);
					for(int i = begin; i <= end; i++){
						imageScrollBar.setValue(i);
						WritableImage image = homeController.chartAnchorPane.snapshot(new SnapshotParameters(), null);
						BufferedImage buf = SwingFXUtils.fromFXImage(image, null);
						buf = ImageOps.getImageWithEvenHeightAndWidth(buf);
						
						String imName = Integer.toString(i - begin);
						while(imName.length() < 4)
							imName = "0" + imName;
						
						ImageIO.write(buf, "png", new File(garbageImages.getPath() + "/" + imName + ".png"));
					}
					
					String videoExportString = file.getPath().endsWith(".mp4") ? file.getPath() : file.getPath() + ".mp4";
			    	String imagesString = garbageImages.getPath() + "/" + "%04d.png";
			    	
			    	double fr = (end - begin + 1) / length;
					
					ImageOps.exportImagesToVideo(imagesString, videoExportString, fr);
					
					imageMatchingChart.setAnimated(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});

		imageScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderImageMatching();
			}
		});
		
		videoDialogXButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				removeVideoControls();
				homeController.renderCharts();
			}
		});
	}
	
	public void removeVideoControls(){
		while(homeController.vBoxHoldingCharts.getChildren().size() > 1)
			homeController.vBoxHoldingCharts.getChildren().remove(1);
	}
	
	public void renderImageMatching(){

		int scrollBarIndex = (int)imageScrollBar.getValue();
		if(imagePaths.size() == 0 || scrollBarIndex < 0 || scrollBarIndex > imagePaths.size() - 1)
		{
			imageView.setImage(null);
			return;
		}
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(imagePaths.get(scrollBarIndex).getPath()));
		} catch (IOException e) {
			System.err.println("Failed to load image in renderImageMatching");
			e.printStackTrace();
			return;
		}

		imageView.setImage(SwingFXUtils.toFXImage(img,null));
		//runDICImageView.setFitHeight(200);
		imageShownLabel.setText(imagePaths.get((int)imageScrollBar.getValue()).getName());

		imageView.fitHeightProperty().unbind();
		imageView.fitWidthProperty().unbind();
		imageView.setFitHeight(10);
		imageView.setFitWidth(10);
		imageView.setFitHeight(-1);
		imageView.setFitWidth(-1);
		imageView.setPreserveRatio(true);

		if(imageView.getImage().getHeight() / imageView.getImage().getWidth() > ((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / 2 / ((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue()){
			imageView.setFitHeight(((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / 2);
		}
		else	
		{
			imageView.setFitWidth(((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue());
		}
		
		
		Sample currentSample = getCheckedSamples().get(0);
		LoadDisplacementSampleResults result = currentSample.getResults().get(0);
		DataSubset currentDisplacement = currentSample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
		int currentIndex = (int)imageScrollBar.getValue() - currentDisplacement.getBegin();
		imageMatchingChart.clearVerticalMarkers();
		if (imageMatchingChart.xDataType == chartDataType.TIME) {
			// Time is on the x axis
			imageMatchingChart.addVerticalValueMarker(
					new Data<Number, Number>(result.time[currentIndex] * CommonGUI.timeUnits.getMultiplier(), 0));
		} 
		else 
		{
			// Displacement/Strain is on the x axis
			if (homeController.loadDisplacementCB.isSelected()) {
				if (homeController.englishRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(result.getDisplacement("in")[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(result.getDisplacement("mm")[currentIndex], 0));
				}

			} 
			else 
			{
				if (homeController.engineeringRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(result.getEngineeringStrain()[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(result.getTrueStrain()[currentIndex], 0));
				}
			}
		}
		
	}

	public void showVideoDialog() {
		if(getCheckedSamples().size() != 1){
			Dialogs.showErrorDialog("Error", "Incorrect number of samples selected", "Please check one sample", stage);
			return;
		}
		
		Sample currentSample = getCheckedSamples().get(0);
		
		if(homeController.vBoxHoldingCharts.getChildren().size() > 1){
			homeController.vBoxHoldingCharts.getChildren().remove(1);
		}
		homeController.sampleDirectoryGUI.fillAllSamplesTreeView(); // Why is this here?
		videoDialogXButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");

		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(videoDialogXButton);
		if(currentSample.hasImages)
			hBoxThatHoldsXButton.getChildren().add(useSampleImages);
		hBoxThatHoldsXButton.getChildren().add(openImagesButton);
		hBoxThatHoldsXButton.getChildren().add(saveVideoButton);

		VBox controlsVBox = new VBox();
		controlsVBox.setAlignment(Pos.CENTER);
		controlsVBox.setSpacing(15);
		controlsVBox.getChildren().add(imageScrollBar);
		controlsVBox.getChildren().add(imageShownLabel);


		VBox vbox = new VBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(controlsVBox);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);

		homeController.vBoxHoldingCharts.getChildren().add(vbox);
		homeController.displayedChartListView.getCheckModel().check("Stress Vs Strain");
		homeController.renderCharts();
	}
}

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
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.ImageOps;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class VideoCorrelationGUI extends CommonGUI{
	private HomeController homeController;
	List<File> imagePaths = new ArrayList<>();
	
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
				FileChooser fileChooser = new FileChooser();
				imagePaths =
						fileChooser.showOpenMultipleDialog(stage);

				Sample currentSample = getCheckedSamples().get(0);
				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);

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
				File tempImageLoadLocation = new File(SPSettings.applicationSupportDirectory + "/SurePulse/tempImagesForViewer");
				if(tempImageLoadLocation.exists())
					SPOperations.deleteFolder(tempImageLoadLocation);
				
				tempImageLoadLocation.mkdirs();
				
				File images = ImageOps.extractSampleImagesToDirectory(currentSample, tempImageLoadLocation);
				
				imagePaths = images == null ? null : Arrays.asList(images.listFiles());
				
				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);

				if(currentDisplacementDataSubset.Data.data.length != imagePaths.size()){
					Dialogs.showAlert("The number of images does not match the length of the displacement data", stage);
				}
				System.out.println("Setting minimum of scroll bar to: " + currentDisplacementDataSubset.getBegin());
				System.out.println("Setting maximum of scroll bar to: " + currentDisplacementDataSubset.getEnd());
				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
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
	}
	
	public void renderImageMatching(){
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(imagePaths.get((int)imageScrollBar.getValue()).getPath()));
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
		DataSubset currentDisplacement = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
		int currentIndex = (int)imageScrollBar.getValue() - currentDisplacement.getBegin();
		imageMatchingChart.clearVerticalMarkers();
		if (imageMatchingChart.xDataType == chartDataType.TIME) {
			// Time is on the x axis
			imageMatchingChart.addVerticalValueMarker(
					new Data<Number, Number>(currentSample.results.time[currentIndex] * homeController.timeUnits.getMultiplier(), 0));
		} 
		else 
		{
			// Displacement/Strain is on the x axis
			if (homeController.loadDisplacementCB.isSelected()) {
				if (homeController.englishRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getDisplacement("in")[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getDisplacement("mm")[currentIndex], 0));
				}

			} 
			else 
			{
				if (homeController.engineeringRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getEngineeringStrain()[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getTrueStrain()[currentIndex], 0));
				}
			}
		}
		
	}
}

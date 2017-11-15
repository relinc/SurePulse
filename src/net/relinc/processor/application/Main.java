package net.relinc.processor.application;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.processor.controllers.SplashPageController;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {

		SPTracker.setEnabled(SPTracker.initiallyEnabled);
		SPTracker.track(SPTracker.surepulseProcessorCategory, "Launch");
		SPSettings.metricMode.set(false);
		if (!SPSettings.readSPSettings()) {
			//TODO: Log and show error
		}

		SPOperations.prepareAppDataDirectory();
		if (SPSettings.Workspace != null) {
			if (!SPSettings.Workspace.exists()) {// they deleted their workspace
				SPSettings.Workspace = null;
			} else {
				SPOperations.prepareWorkingDirectory();
			}
		}
		
		// Show a brochure
		try{
			primaryStage.setTitle("Welcome to SURE-Pulse");
			
			// TODO: Log the image that was shown.
			String[] imageNames = {"sure-bright.png","sure-daq.png", "sure-flat.png", "sure-launch.png",
					"sure-servo.png", "sure-speed.png", "sure-temp.png"};
			int randVal = (int)(Math.random() * imageNames.length);
			Image image = new Image(getClass().getResourceAsStream("/net/relinc/processor/BrochureImages/" + imageNames[randVal]));
			//Image image = new Image(file.toURI().toString());
		    ImageView viewer = new ImageView(image);
		    viewer.setPreserveRatio(true);
			
	        AnchorPane root = new AnchorPane();
	        VBox vBox = new VBox();
	        vBox.getChildren().add(viewer);
	        AnchorPane.setBottomAnchor(vBox, 0.0);
	        AnchorPane.setRightAnchor(vBox, 0.0);
	        AnchorPane.setLeftAnchor(vBox, 0.0);
	        AnchorPane.setTopAnchor(vBox, 0.0);
	        viewer.fitWidthProperty().bind(primaryStage.widthProperty());
	        viewer.fitHeightProperty().bind(primaryStage.heightProperty());
	        
	        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
	        primaryStage.setHeight(primaryScreenBounds.getHeight() * 3 / 4);
	        primaryStage.setWidth(primaryStage.getHeight() * image.getWidth() / image.getHeight());
	        root.getChildren().add(vBox);
	        
	        primaryStage.setScene(new Scene(root, 300, 250));
	        primaryStage.show();
	        
	        PauseTransition delay = new PauseTransition(Duration.seconds(2));
	        delay.setOnFinished( event -> transitionToSurePulse(primaryStage) );
	        delay.play();
		}
		catch(Exception e){
			
		}
	}
	
	public void transitionToSurePulse(Stage stage){
		Timeline timeline = new Timeline();
        KeyFrame key = new KeyFrame(Duration.millis(250),
                       new KeyValue (stage.getScene().getRoot().opacityProperty(), 0)); 
        timeline.getKeyFrames().add(key);   
        timeline.setOnFinished((ae) -> showSurePulse(stage)); 
        timeline.play();
	}
	
	public void showSurePulse(Stage stage){
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/Splashpage.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.getIcons().add(SPSettings.getRELLogo());
			stage.setTitle("SURE-Pulse Data Processor");
			stage.setScene(scene);
			
			// This is an ugly hack that's required to render the controls correctly
			Platform.runLater(new Runnable() {
				@Override public void run() {
					stage.setWidth(stage.getWidth() + 1);
				}
			});
			
			
			SplashPageController c = root1.<SplashPageController> getController();
			c.stage = stage;
			c.renderGUI();
			
			stage.getScene().getRoot().opacityProperty().set(0);
			Timeline timeline = new Timeline();
	        KeyFrame key = new KeyFrame(Duration.millis(1500),
	                       new KeyValue (stage.getScene().getRoot().opacityProperty(), 1)); 
	        timeline.getKeyFrames().add(key);   
	        timeline.setOnFinished(event -> c.showFirstMessageDialog(stage));
	        timeline.play();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	

}

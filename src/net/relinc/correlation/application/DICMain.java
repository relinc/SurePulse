package net.relinc.correlation.application;
	

import java.io.File;

//import controllers.SplashpageController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import net.relinc.correlation.controllers.DICSplashpageController;
import net.relinc.processor.staticClasses.SPSettings;
import javafx.scene.Scene;


public class DICMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		File file = new File(SPSettings.applicationSupportDirectory + "/RELFX/SURE-DIC/");
		if(!file.exists()) {
			file.mkdirs();
		}
		try {
			//prepare app data directory. 
			
			//BorderPane root = new BorderPane();
			FXMLLoader root = new FXMLLoader(getClass().getResource("/net/relinc/correlation/fxml/DICSplashpage.fxml"));
			
			Scene scene = new Scene(root.load());
			scene.getStylesheets().add(getClass().getResource("dicapplication.css").toExternalForm());
			primaryStage.setScene(scene);
			DICSplashpageController cont = root.getController();
			cont.stage = primaryStage;
			cont.createRefreshListener();
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

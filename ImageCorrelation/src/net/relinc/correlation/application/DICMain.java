package net.relinc.correlation.application;
	

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import net.relinc.correlation.controllers.DICSplashpageController;
import net.relinc.correlation.staticClasses.CorrSettings;
import javafx.scene.Scene;
import net.relinc.libraries.staticClasses.SPSettings;

public class DICMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		File file = new File(SPSettings.applicationSupportDirectory + "/" + CorrSettings.appDataName);
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

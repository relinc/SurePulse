package net.relinc.shotcaller.application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.shotcaller.GUI.HomeController;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class ShotCallerMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			// BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/shotcaller/GUI/Home.fxml"));
			// Parent root =
			// FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());

			// Parent root =
			// FXMLLoader.load(getClass().getResource("/fxml/Splashpage.fxml"));
			// Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.getIcons().add(SPSettings.getRELLogo());
			primaryStage.setTitle("Shot Caller");
			primaryStage.setScene(scene);
			HomeController c = root1.<HomeController>getController();
			//c.renderGUI();
			c.stage = primaryStage;
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
//		try {
//			BorderPane root = new BorderPane();
//			Scene scene = new Scene(root,400,400);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

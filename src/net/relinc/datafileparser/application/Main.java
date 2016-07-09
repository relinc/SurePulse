package net.relinc.datafileparser.application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.datafileparser.GUI.Home;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		new Home(primaryStage);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

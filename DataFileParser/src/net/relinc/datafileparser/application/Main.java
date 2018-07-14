package net.relinc.datafileparser.application;
	
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		new Home(primaryStage, null);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

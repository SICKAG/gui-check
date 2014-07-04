package de.sick.guicheck.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUICheckFX extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("GUI Check FX 0.1");
		primaryStage.setScene(new Scene((Parent)FXMLLoader.load(getClass().getResource("GUICheckFX.fxml"))));
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}

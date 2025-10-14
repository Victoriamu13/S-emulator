package controllers;

import controllers.screens.ScreenManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import design.SkinManager;

public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ScreenManager.init(primaryStage);
        ScreenManager.showLoginScreen();
    }

    public static void main(String[] args){
        launch(args);
    }
}

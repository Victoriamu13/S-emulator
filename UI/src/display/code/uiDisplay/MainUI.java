package uiDisplay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import uiDisplay.design.SkinManager;

public class MainUI extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/components/root/Root.fxml"));
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene=new Scene(loader.load(),1400,800);
        SkinManager.apply(scene, "Default");

        stage.setTitle("S-Emulator");
        stage.setScene(scene);

      stage.setMinWidth(400);
       stage.setMinHeight(200);

        stage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}

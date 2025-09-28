package uiDisplay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainUI extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/components/root/Root.fxml"));
        Scene scene=new Scene(loader.load(),1200,800);
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

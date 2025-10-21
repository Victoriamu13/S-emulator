package controllers.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {
    private static Stage stage;

    public static void init(Stage primaryStage){
        stage=primaryStage;
    }

    public static void showLoginScreen(){
        loadScreen("/withWebComponents/landScreen/Login.fxml","S-Emulator - Login");
    }

    public static void showFirstScreen(){
        loadScreen("/withWebComponents/firstScreen/FirstScreen.fxml","S-Emulator - Users");
    }


    private static <T> void loadScreen(String fxmlPath, String title){
        try{
            FXMLLoader loader=new FXMLLoader(ScreenManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

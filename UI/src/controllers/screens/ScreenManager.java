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
        loadScreen("/withWebComponents/loginScreen/Login.fxml","S-Emulator - Login");
    }

    public static void showDashboardScreen(){
        loadScreen("/withWebComponents/dashboardScreen/DashboardScreen.fxml","S-Emulator - Users");
    }

    public static void showExecutionScreen(){
        loadScreen("/withWebComponents/executionScreen/ExecutionScreen.fxml","S-Emulator - Execution");
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

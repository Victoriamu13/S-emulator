package controllers.screens;

import controllers.utils.refreshers.TimerManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {
    public static final String DASHBOARD="dashboard";
    public static final String EXECUTION="execution";
    public static final String LOGIN="login";

    private static Stage stage;

    public static void init(Stage primaryStage){
        stage=primaryStage;
    }

    public static void showLoginScreen(){
        loadScreen("/withWebComponents/loginScreen/Login.fxml","S-Emulator - Login",LOGIN);
    }

    public static void showDashboardScreen(){
        loadScreen("/withWebComponents/dashboardScreen/DashboardScreen.fxml","S-Emulator - Users",DASHBOARD);
    }

    public static void showExecutionScreen(){
        loadScreen("/withWebComponents/executionScreen/ExecutionScreen.fxml","S-Emulator - Execution",EXECUTION);
    }


    private static <T> void loadScreen(String fxmlPath, String title, String screenId){
        try{
            TimerManager.switchScreen(screenId);

            FXMLLoader loader=new FXMLLoader(ScreenManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (stage.getScene() == null) { //case first screen
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(root);  //case switch to another screen
            }

            stage.centerOnScreen();
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

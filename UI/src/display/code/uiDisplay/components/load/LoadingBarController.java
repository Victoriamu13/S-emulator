package uiDisplay.components.load;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadingBarController {
    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;

    public void startLoadingSimulation(){
        Task<Void> task=new Task<>(){
            @Override protected Void call() throws Exception {
                int steps = 100;

                for(int i=0;i<=steps;i++){
                    Thread.sleep(20);
                    updateProgress(i,100);
                }
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnRunning(e->lblLoading.setText("Loading..."));
        task.setOnSucceeded(e->{
            lblLoading.setText("File Loaded Successfully!");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();;
    }
}

package uiDisplay.components.load;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadingBarController {
    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;

    private Runnable onFinished;
    private Runnable onStart;

    public void setOnFinished(Runnable r) {this.onFinished = r;}
    public void setOnStart(Runnable r) { this.onStart = r; }

    public void startLoadingSimulation(){
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblLoading.setText("Loading...");

        if (onStart != null) onStart.run();

        Task<Void> task=new Task<>(){
            @Override protected Void call() throws Exception {
                int steps = 100;

                for(int i=0;i<=steps;i++){
                    Thread.sleep(10);
                    updateProgress(i,100);
                }
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e->{
            lblLoading.setText("File Loaded Successfully!");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);

            if (onFinished != null) onFinished.run();
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();;
    }
}

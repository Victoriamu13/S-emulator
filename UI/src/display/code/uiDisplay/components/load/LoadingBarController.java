package uiDisplay.components.load;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import uiDisplay.design.SkinManager;

public class LoadingBarController {
    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;
    @FXML private ComboBox<String> skinSelector;

    private Runnable onFinished;
    private Runnable onStart;

    public void setOnFinished(Runnable r) {this.onFinished = r;}
    public void setOnStart(Runnable r) { this.onStart = r; }

    @FXML
    private void initialize() {
        skinSelector.getItems().addAll(SkinManager.getAvailableSkins());
        skinSelector.setValue("Default");

        skinSelector.setOnAction(e -> {
            String chosen = skinSelector.getValue();
            SkinManager.apply(skinSelector.getScene(), chosen);
        });
    }

    public void startLoadingSimulation(){
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblLoading.setText("Loading...");

        if (onStart != null) onStart.run();

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

        task.setOnSucceeded(e->{
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);

            if (onFinished != null) onFinished.run();
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();;
    }

    public void markSuccess() {
        lblLoading.setText("File Loaded Successfully!");
        if (progressBar.progressProperty().isBound()) {
            progressBar.progressProperty().unbind();
        }
        progressBar.setProgress(1);
    }

    public void resetLoading() {
        lblLoading.setText("Load Progress : 0%");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }
}

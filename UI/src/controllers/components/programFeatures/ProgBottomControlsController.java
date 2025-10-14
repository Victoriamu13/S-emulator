package controllers.components.programFeatures;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import design.AnimationManager;
import design.SkinManager;

public class ProgBottomControlsController {
    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;
    @FXML private ComboBox<String> skinSelector;
    @FXML private CheckBox wthAnimations;

    private Runnable onFinished;
    private Runnable onStart;
    private boolean animationsEnabled = true;
    private ScaleTransition pulseAnim;

    public void setOnFinished(Runnable r) {this.onFinished = r;}

    @FXML
    private void initialize() {
        skinSelector.getItems().addAll(SkinManager.getAvailableSkins());
        skinSelector.setValue("Default");

        skinSelector.setOnAction(e -> {
            String chosen = skinSelector.getValue();
            SkinManager.apply(skinSelector.getScene(), chosen);
        });

        wthAnimations.setSelected(AnimationManager.isAnimationsEnabled());
        wthAnimations.selectedProperty().addListener((obs, oldVal, newVal) -> {
            AnimationManager.setAnimationsEnabled(newVal);
        });
    }


    public void startLoadingSimulation(){
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblLoading.setText("Load Progress : 0%");

        if (onStart != null) onStart.run();

        Task<Void> task=new Task<>(){
            @Override protected Void call() throws Exception {
                int steps = 100;

                for(int i=0;i<=steps;i++){
                    Thread.sleep(20);
                    updateProgress(i,100);
                    updateMessage("Load Progress : " + i + "%");
                }
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        lblLoading.textProperty().bind(task.messageProperty());

        task.setOnRunning(e -> {
            if (animationsEnabled) startPulseAnimation();
        });

        task.setOnSucceeded(e->{
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);

            if (onFinished != null) onFinished.run();
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();;
    }


    public void resetLoading() {
        lblLoading.setText("Load Progress : 0%");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }

    public void finishLoading() {
        stopPulseAnimation();

        lblLoading.textProperty().unbind();
        lblLoading.setText("Load Progress : 100%");

        progressBar.progressProperty().unbind();
        progressBar.setProgress(1);
    }

    private void startPulseAnimation() {
        if (!AnimationManager.isAnimationsEnabled()) return;
        pulseAnim = new ScaleTransition(Duration.millis(400), progressBar);
        pulseAnim.setFromX(1.0);
        pulseAnim.setToX(1.05);
        pulseAnim.setFromY(1.0);
        pulseAnim.setToY(1.05);
        pulseAnim.setAutoReverse(true);
        pulseAnim.setCycleCount(Animation.INDEFINITE);
        pulseAnim.play();
    }

    private void stopPulseAnimation() {
        if (pulseAnim != null) {
            pulseAnim.stop();
            progressBar.setScaleX(1);
            progressBar.setScaleY(1);
        }
    }
}

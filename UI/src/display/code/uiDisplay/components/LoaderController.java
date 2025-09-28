package uiDisplay.components;

import engineHolder.EngineHolder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import logic.engineFacade.api.EngineFacade;

import javafx.scene.control.Button;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.LoadOutcome;

import java.io.File;

public class
LoaderController {
    @FXML private Button btnLoadFile;
    @FXML private Label lblStatus;
    private EngineHolder holder;

   private final ObjectProperty<EngineFacade> engineProperty=new SimpleObjectProperty<>();

    public void setEngineHolder(EngineHolder holder){this.holder=holder;}

    public ObjectProperty<EngineFacade> engineProperty() { return engineProperty; }
    public EngineFacade getEngine() { return engineProperty.get(); }


    @FXML
    private void initialize(){
        btnLoadFile.setOnAction(e->onLoadFile());
    }

    private void onLoadFile(){
        FileChooser fc=new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files","*.xml"));
        File f=fc.showOpenDialog(btnLoadFile.getScene().getWindow());
        if(f==null)return;

        lblStatus.setText(f.getAbsolutePath());

        EngineFacade engine = new EngineFacadeImpl();
        LoadOutcome res=engine.loadProgram(f.toPath());

        if(res.success()){
            holder.set(engine);
            engineProperty.set(holder.getEngine());
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Error");
            alert.setHeaderText("Invalid Program File");
            alert.setContentText(String.join("\n", res.errors()));
            alert.showAndWait();

            lblStatus.setText("[Load error]");
            holder.set(null);
            engineProperty.set(null);
        }
    }
}

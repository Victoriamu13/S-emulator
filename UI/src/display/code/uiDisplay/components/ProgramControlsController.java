package uiDisplay.components;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;


public class ProgramControlsController {

    @FXML private ComboBox<String> programSelector;
    @FXML private Button btnCollapse;
    @FXML private Button btnExpand;
    @FXML private Label lblDegree;
    @FXML private ComboBox<String> highlightSelector;

    private EngineHolder holder;

    private final IntegerProperty currDegree=new SimpleIntegerProperty(0);
    private final IntegerProperty maxDegree=new SimpleIntegerProperty(0);
    private final StringProperty highlightSelection=new SimpleStringProperty();

    public IntegerProperty currentDegreeProperty(){return currDegree;}

    public int getCurrentDegree(){return currDegree.get();}

    public StringProperty highlightSelectionProperty(){return highlightSelection;}

    @FXML
    public void initialize(){
     lblDegree.textProperty().bind(currDegree.asString().concat(" / ").concat(maxDegree.asString()));

     btnExpand.setOnAction(e->{
         if(holder.hasEngine()&& currDegree.get()<maxDegree.get()){
             currDegree.set(currDegree.get()+1);
             refreshHighlightList(currDegree.get());
         }
     });
     btnCollapse.setOnAction(e->{
         if(holder.hasEngine() && currDegree.get() > 0){
             currDegree.set(currDegree.get() - 1);
             refreshHighlightList(currDegree.get());
         }
     });
        highlightSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> highlightSelection.set(newVal));
    }

    public void setEngineHolder(EngineHolder holder){
        this.holder=holder;

        if(holder.hasEngine()){
            var engine=holder.getEngine();
            this.maxDegree.set(engine.getMaxExpansionDegree());
            programSelector.getItems().setAll(engine.getProgramName());

            currDegree.addListener((obs,oldVal,newVal)->{
                refreshHighlightList(newVal.intValue());
            });
            refreshHighlightList(currDegree.get());
        }
    }

    public void refreshHighlightList(int degree){
        var engine=holder.getEngine();

        highlightSelector.getItems().clear();
        highlightSelector.getItems().addAll(engine.getInputsUsed(degree));
        highlightSelector.getItems().addAll(engine.getLabelsUsed(degree));
    }

}

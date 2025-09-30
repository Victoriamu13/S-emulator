package uiDisplay.components.programFeatures;

import engineHolder.EngineHolder;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class ProgramControlsController {

    @FXML private ComboBox<String> programSelector;
    @FXML private Button btnCollapse;
    @FXML private Button btnExpand;
    @FXML private Label lblDegree;
    @FXML private ComboBox<String> highlightSelector;

    private Integer selectedFinalIndex = null;
    private EngineHolder holder;

    private final IntegerProperty currDegree=new SimpleIntegerProperty(0);
    private final IntegerProperty maxDegree=new SimpleIntegerProperty(0);
    private final StringProperty highlightSelection=new SimpleStringProperty();
    private final StringProperty selectedProgramNameProperty = new SimpleStringProperty();

    public IntegerProperty currentDegreeProperty(){return currDegree;}
    public int getCurrentDegree(){return currDegree.get();}
    public StringProperty highlightSelectionProperty(){return highlightSelection;}
    public StringProperty selectedProgramNameProperty() {return selectedProgramNameProperty;}


    @FXML
    public void initialize(){
        lblDegree.textProperty().bind(currDegree.asString().concat(" / ").concat(maxDegree.asString()));

        highlightSelector.setEditable(true);
        highlightSelector.getEditor().setEditable(false);

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

            // load program and function names
            List<String> items = new ArrayList<>();
            items.add(engine.getProgramName());
            items.addAll(engine.getFunctionNames());
            programSelector.getItems().setAll(items);

            String firstName = programSelector.getSelectionModel().getSelectedItem();
            selectedProgramNameProperty.set(firstName);
            engine.selectProgramOrFunction(firstName);

            // update when user changes selection
            programSelector.getSelectionModel().selectedItemProperty().
                    addListener((obs, oldV, newV) -> {

                        engine.selectProgramOrFunction(newV);
                        selectedProgramNameProperty.set(newV);
                        this.maxDegree.set(engine.getMaxExpansionDegree());
                        refreshHighlightList(currDegree.get());
            });

            // update when degree changes
            currDegree.addListener((obs,oldVal,newVal)->{
                refreshHighlightList(newVal.intValue());
            });
        }
    }

    public void setSelectedFinalIndex(Integer index) {
        this.selectedFinalIndex = index;
        refreshHighlightList(currDegree.get());
    }


    public void refreshHighlightList(int degree){
        var engine=holder.getEngine();
        Set<String> highlightItems = new LinkedHashSet<>();

        highlightItems.addAll(engine.getInputsUsed(degree));
        highlightItems.addAll(engine.getAllVariablesUsed(degree, selectedFinalIndex));
        highlightItems.addAll(engine.getAllLabelsUsed(degree, selectedFinalIndex));

        highlightSelector.getItems().setAll(highlightItems);
        highlightSelector.getSelectionModel().clearSelection();
        highlightSelector.setPromptText("Highlight");
    }

    public void clear() {
        currDegree.set(0);
        maxDegree.set(0);
        highlightSelector.getItems().clear();
        highlightSelection.set(null);
        highlightSelector.setPromptText("Highlight");

    }

}

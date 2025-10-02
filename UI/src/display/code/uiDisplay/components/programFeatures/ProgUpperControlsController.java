package uiDisplay.components.programFeatures;

import engineHolder.EngineHolder;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.Duration;
import uiDisplay.design.AnimationManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static engineHolder.EngineHolder.hasEngine;


public class ProgUpperControlsController {

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
        lblDegree.textProperty().bind(
                Bindings.concat(currDegree.asString(), " / ", maxDegree.asString())
        );

        highlightSelector.setEditable(true);
        highlightSelector.getEditor().setEditable(false);

        btnExpand.setOnAction(e->{
            if(holder.hasEngine()&& currDegree.get()<maxDegree.get()){
                currDegree.set(currDegree.get()+1);
                refreshHighlightList(currDegree.get());
                playDegreeChangeAnimation();
            }
        });
        // button expands program one degree forward
        btnCollapse.setOnAction(e->{
            if(holder.hasEngine() && currDegree.get() > 0){
                currDegree.set(currDegree.get() - 1);
                refreshHighlightList(currDegree.get());
                playDegreeChangeAnimation();
            }
        });
        highlightSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> highlightSelection.set(newVal));
    }

    public void setEngineHolder(EngineHolder holder){
        this.holder = holder;

        if (holder.hasEngine()) {
            var engine = holder.getEngine();
            currDegree.set(0);


            // load program and function names
            List<String> items = new ArrayList<>();
            items.add(engine.getProgramName());
            items.addAll(engine.getFunctionNames());
            programSelector.getItems().setAll(items);

            // set first program WITHOUT firing selection event
            if (!items.isEmpty()) {
                programSelector.setValue(items.get(0));
                String firstName = programSelector.getValue();

                engine.selectProgramOrFunction(firstName);
                engine.resetExpansionCache();
                maxDegree.set(engine.getMaxExpansionDegree());
                selectedProgramNameProperty.set(firstName);

            }

            // listener: when user picks another program/function
            programSelector.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> {
                        if (newV == null) return;

                        engine.resetExpansionCache();
                        engine.selectProgramOrFunction(newV);
                        selectedProgramNameProperty.set(newV);
                        maxDegree.set(engine.getMaxExpansionDegree());
                        refreshHighlightList(currDegree.get());
                    });

            // listener: refresh highlight list when degree changes
            currDegree.addListener((obs, oldVal, newVal) -> {
                refreshHighlightList(newVal.intValue());
            });
        }
    }

    public void setSelectedFinalIndex(Integer index) {
        this.selectedFinalIndex = index;
        refreshHighlightList(currDegree.get());
    }


    public void refreshHighlightList(int degree){
        if(!hasEngine(holder)) {
            highlightSelector.getItems().clear();
            highlightSelector.setPromptText("Highlight");
            return;
        }
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

    private void playDegreeChangeAnimation() {
        if (!AnimationManager.isAnimationsEnabled()) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(250), lblDegree);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }
}

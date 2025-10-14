package controllers.components.expansion;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import logic.engineFacade.model.InstructionDTO;
import controllers.components.instructions.InstructionsController;
import controllers.components.programFeatures.ProgUpperControlsController;


public class HistoryChainController {

    @FXML private TableView<InstructionDTO> historyChainTable;
    @FXML private TableColumn<InstructionDTO, Number> colIdx;
    @FXML private TableColumn<InstructionDTO, String> colBS;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstr;
    @FXML private TableColumn<InstructionDTO, String> colCycles;

    private EngineHolder holder;
    private Integer lastSelectedFinalIndex;
    private String currentHighlight = null;

    private final String HIGHLIGHTED = "highlighted";


    @FXML
    private void initialize() {
        setupColumns();
        setupPlaceholder();
        setupRowHighlighting();
    }

    // ==== setup helpers ====

    private void setupColumns() {
        colIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colInstr.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));
    }

    private void setupPlaceholder() {
        historyChainTable.setPlaceholder(new Label("No history to show"));
    }

    private void setupRowHighlighting() {
        historyChainTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty && currentHighlight != null && !currentHighlight.isEmpty()) {
                    String search = currentHighlight.trim();
                    boolean match = false;

                    if (item.label() != null && item.label().trim().equals(search)) {
                        match = true;
                    } else if (item.command() != null && item.command().matches(".*\\b" + search + "\\b.*")) {
                        match = true;
                    }

                    if (match) {
                        if (!getStyleClass().contains(HIGHLIGHTED)) {
                            getStyleClass().add(HIGHLIGHTED);
                        }
                    } else {
                        getStyleClass().remove(HIGHLIGHTED);
                    }
                } else {
                    getStyleClass().remove(HIGHLIGHTED);
                }
            }
        });
    }

    // ==== API for other controllers ====

    public void attachHistoryChain(InstructionsController instCtrl, IntegerProperty degreeProp,
                                   ProgUpperControlsController programCtrl){

        instCtrl.setOnInstructionSelected(sel->{
            if(sel!=null) {
                lastSelectedFinalIndex = sel.index();
                programCtrl.setSelectedFinalIndex(lastSelectedFinalIndex);
                refreshHistoryChain(programCtrl.getCurrentDegree());
            }else{
                lastSelectedFinalIndex   = null;
                programCtrl.setSelectedFinalIndex(null);
                clear();
            }
        });
        degreeProp.addListener((obs,oldVal,newVal)->{
            lastSelectedFinalIndex = null;
            programCtrl.setSelectedFinalIndex(null);
            clear();
            refreshHistoryChain(newVal.intValue());});
    }

    public void setEngineHolder(EngineHolder holder) {
        this.holder = holder;
        this.lastSelectedFinalIndex =null;
        clear();
    }

    public void bindHighlight(StringProperty highlightProp) {
        highlightProp.addListener((obs, oldVal, newVal) -> {
            currentHighlight = newVal;
            historyChainTable.refresh();
        });
    }

    public void bindSelectedProgramName(StringProperty programNameProp) {
        programNameProp.addListener((obs, oldVal, newVal) -> {
            lastSelectedFinalIndex = null;
            clear();
        });
    }

    // --- internals ---

    private void refreshHistoryChain(int degree){
        if(!holder.hasEngine() || lastSelectedFinalIndex  == null){
            clear();
            return;
        }
        var engine=holder.getEngine();


        var chain=engine.getExpansionHistoryChain(degree,lastSelectedFinalIndex );
        historyChainTable.getItems().setAll(chain);
        historyChainTable.refresh();
    }

    public void clear() {
        historyChainTable.getItems().clear();
        historyChainTable.setPlaceholder(new Label("No history to show"));
    }

}

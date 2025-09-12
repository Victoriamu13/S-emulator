package uiDisplay.components;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.engineFacade.model.InstructionDTO;
import java.util.List;


public class HistoryChainController {

    @FXML private TableView<InstructionDTO> historyChainTable;
    @FXML private TableColumn<InstructionDTO, Number> colIdx;
    @FXML private TableColumn<InstructionDTO, String> colBS;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstr;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    private EngineHolder holder;
    private Integer lastSelectedFinalIndex;


    @FXML
    private void initialize() {
        colIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colInstr.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().cycles()));

        historyChainTable.setPlaceholder(new Label("No history to show"));
    }

    public void attachHistoryChain(InstructionsController instCtrl, IntegerProperty degreeProp,
                                   ProgramControlsController programCtrl){

        instCtrl.setOnInstructionSelected(sel->{
            if(sel!=null) {
                lastSelectedFinalIndex  = sel.index();
                refreshHistoryChain(programCtrl.getCurrentDegree());
            }else{
                lastSelectedFinalIndex  = null;
                clear();
            }
        });
        degreeProp.addListener((obs,oldVal,newVal)->{
            refreshHistoryChain(newVal.intValue());});
    }

    public void setEngineHolder(EngineHolder holder) {
        this.holder = holder;
        this.lastSelectedFinalIndex =null;
        clear();
    }

    public void showInstHistoryChain(List<InstructionDTO> chain){
        if (chain == null || chain.isEmpty()) {
            clear();
            return;
        }
        historyChainTable.getItems().setAll(chain);
    }

    private void refreshHistoryChain(int degree){
        if(!holder.hasEngine() || lastSelectedFinalIndex  == null){
            clear();
            return;
        }
        var engine=holder.getEngine();
        var chain=engine.getExpansionHistoryChain(degree,lastSelectedFinalIndex );

        historyChainTable.getItems().setAll(chain);
    }

    public void clear() {
        historyChainTable.getItems().clear();
        historyChainTable.setPlaceholder(new Label("No history to show"));
    }

}

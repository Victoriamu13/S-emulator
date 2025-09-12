package uiDisplay.components;

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

import java.util.List;
import java.util.function.Consumer;

public class InstructionsController {

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO,Number> colIdx;
    @FXML private TableColumn<InstructionDTO, String> colBS;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstr;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;
    @FXML private Label lblSummary;

    private EngineHolder holder;
    private int currDegree=0;
    private Consumer<InstructionDTO> onInstructionSelected =selIn -> {};

    public void setOnInstructionSelected(Consumer<InstructionDTO> listener){
        this.onInstructionSelected= (listener!=null) ? listener : selIn->{};
    }


    @FXML
    private void initialize(){
        colIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));          // "B"/"S"
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colInstr.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().cycles()));

        // Placeholder
        instructionsTable.setPlaceholder(new Label("No program loaded"));
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) onInstructionSelected.accept(sel);
        });
    }

    public void setEngineHolder(EngineHolder holder) {
        this.holder = holder;
        refreshInstructions();
    }

    public void setDegree(int degree){
        this.currDegree = degree;
        refreshInstructions();
    }

   public void bindDegree(IntegerProperty degreeProp){
        degreeProp.addListener((obs,oldVal,newVal)->{
            this.currDegree=newVal.intValue();
            refreshInstructions();
        });
   }

   public void bindHighlight(StringProperty highlightProp){
       highlightProp.addListener((obs,oldVal,newVal)->{
           if(newVal!=null){
               highlightStr(newVal);
           }
       });
   }

    public InstructionDTO getSelectedInstruction() {
        return instructionsTable.getSelectionModel().getSelectedItem();
    }

    private void refreshInstructions(){
        if(!holder.hasEngine()){
            instructionsTable.getItems().clear();
            lblSummary.setText("No program loaded");
            return;
        }

        var engine=holder.getEngine();
        List<InstructionDTO> rows=engine.getInstructionRows(currDegree);
        instructionsTable.getItems().setAll(rows);

        int total = engine.getInstructionTotal(currDegree);
        int basic = engine.getInstructionBasicCount(currDegree);
        int synth = engine.getInstructionSyntheticCount(currDegree);

        lblSummary.setText(String.format("Total: %d | Basic: %d | Synthetic: %d",total,basic,synth));
    }

    public void  highlightStr(String str){
        instructionsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().remove("highlighted");

                if (item != null && !empty) {
                    if (item.command().contains(str) || item.label().equals(str)) {
                        getStyleClass().add("highlighted");
                    }
                }
            }
        });
    }

}

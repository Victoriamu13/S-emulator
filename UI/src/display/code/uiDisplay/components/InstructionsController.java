package uiDisplay.components;

import engineHolder.EngineHolder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
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
    @FXML private TableColumn<InstructionDTO, String> colCycles;
    @FXML private Label lblSummary;

    private EngineHolder holder;
    private int currDegree=0;
    private String currentHighlight = null;

    private Consumer<InstructionDTO> onInstructionSelected =selIn -> {};

    private final String HIGHLIGHTED = "highlighted";


    @FXML
    private void initialize(){
        setupColumns();
        setupSelectionListener();
        setupRowHighlighting();
        setupPlaceholder();
    }

    // --- setup helpers ---

    private void setupColumns() {
        colIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colInstr.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));
    }

    private void setupSelectionListener() {
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) onInstructionSelected.accept(sel);
        });
    }

    private void setupRowHighlighting() {
        instructionsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty && currentHighlight != null && !currentHighlight.isEmpty()) {
                    String search = currentHighlight.trim();
                    boolean match = (item.command() != null && item.command().trim().equals(search)) ||
                            (item.label() != null && item.label().trim().equals(search));

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

    private void setupPlaceholder() {
        instructionsTable.setPlaceholder(new Label("No program loaded"));
    }

    // --- API for other controllers ---

    public void setOnInstructionSelected(Consumer<InstructionDTO> listener){
        this.onInstructionSelected= (listener!=null) ? listener : selIn->{};
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
           currentHighlight = newVal;
           instructionsTable.refresh();
       });
   }

    public InstructionDTO getSelectedInstruction() {
        return instructionsTable.getSelectionModel().getSelectedItem();
    }

    // --- internals ---

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

    public void clear() {
        instructionsTable.getItems().clear();
        instructionsTable.setPlaceholder(new Label("No program loaded"));
        lblSummary.setText("Total: 0 | Basic: 0 | Synthetic: 0");
    }

}

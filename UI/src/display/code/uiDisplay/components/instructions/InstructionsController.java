package uiDisplay.components.instructions;

import engineHolder.EngineHolder;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
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
    private int currentPc = -1;
    private String currentHighlight = null;
    private Consumer<InstructionDTO> onInstructionSelected =selIn -> {};
    private final String HIGHLIGHTED = "highlighted";
    private final String ACTIVE_ROW = "active-row";


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
                boolean match = false;
                if (item != null && !empty && currentHighlight != null && !currentHighlight.isEmpty()) {
                    String search = currentHighlight.trim();
                    if (item.label() != null && item.label().trim().equals(search)) {
                        match = true;
                    } else if (item.command() != null && item.command().matches(".*\\b" + search + "\\b.*")) {
                        match = true;
                    }
                }

                if (match) {
                    if (!getStyleClass().contains(HIGHLIGHTED)) getStyleClass().add(HIGHLIGHTED);
                } else {
                    getStyleClass().remove(HIGHLIGHTED);
                }

                // === highlight row in Debug ===
                if (item != null && !empty && currentPc >= 0 && item.index() == currentPc) {
                    if (!getStyleClass().contains(ACTIVE_ROW)) {
                        getStyleClass().add(ACTIVE_ROW);
                        playActiveRowEffect(this);
                    }
                } else {
                    getStyleClass().remove(ACTIVE_ROW);
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

    public void bindSelectedProgramName(StringProperty programNameProp) {
        programNameProp.addListener((obs, oldVal, newVal) -> {
            clear();
            if (newVal != null && holder != null && holder.hasEngine()) {
                holder.getEngine().selectProgramOrFunction(newVal);
                refreshInstructions();
            }
        });
    }

    public void bindCurrentPc(IntegerProperty pcProperty) {
        pcProperty.addListener((obs, oldVal, newVal) -> {
            int pc = newVal.intValue();
            this.currentPc = pc;
            instructionsTable.refresh();

                    if (pc >= 0) {
                        instructionsTable.getItems().stream()
                                .filter(row -> row.index() == pc)
                                .findFirst()
                                .ifPresent(row -> {
                                    int rowIndex = instructionsTable.getItems().indexOf(row);
                                    instructionsTable.scrollTo(rowIndex);
                                });
                    }

        });
    }

    // --- internals ---
    private void refreshInstructions(){
        if(!holder.hasEngine()){
            instructionsTable.getItems().clear();
            lblSummary.setText("No program loaded");
            return;
        }

        var engine=holder.getEngine();
        List<InstructionDTO> rows = engine.getInstructionRows(currDegree);

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



    private void playActiveRowEffect(TableRow<InstructionDTO> row) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), row);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);

        st.setOnFinished(e -> row.setEffect(null));

        st.play();
    }
}

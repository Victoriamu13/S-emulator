package uiDisplay.components.instructions;

import engineHolder.EngineHolder;
import javafx.animation.ScaleTransition;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import logic.engineFacade.model.InstructionDTO;
import uiDisplay.design.AnimationManager;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static engineHolder.EngineHolder.hasEngine;


public class InstructionsController {

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO,Number> colIdx;      // index
    @FXML private TableColumn<InstructionDTO, String> colBS;      // basic/synthetic
    @FXML private TableColumn<InstructionDTO, String> colLabel;   // label
    @FXML private TableColumn<InstructionDTO, String> colInstr;   // full command
    @FXML private TableColumn<InstructionDTO, String> colCycles;  // cycles
    @FXML private TableColumn<InstructionDTO, Void> colBreakpoint;  // breakpoint
    @FXML private Label lblSummary;   // summary of instruction types

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
        setupBreakpoints();
        setupSelectionListener();
        setupRowHighlighting();
        setupPlaceholder();
    }

    // ==== Setup helpers ====

    private void setupColumns() {
        colIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().index()));
        colBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().label()));
        colInstr.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().command()));
        colCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cyclesText()));
    }

    // listener that notifies when a new instruction row is selected
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
                int rowIndex = getIndex();
                if (item != null && !empty && currentPc >= 0 && rowIndex == currentPc) {
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

    private void setupBreakpoints() {
        colBreakpoint.setCellFactory(col -> {
            TableCell<InstructionDTO, Void> cell = new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    int modelIdx = modelIndexOf(getTableRow());
                    boolean hasBp = hasEngine(holder) &&
                            holder.getEngine().getBreakpoints().contains(modelIdx);

                    setText(hasBp ? "●" : "");
                    setStyle(hasBp ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-alignment: center;" : "");
                }
            };

            // toggle breakpoint on click
            cell.setOnMouseClicked(e -> {
                if (hasEngine(holder) && cell.getTableRow() != null) {
                    int modelIdx = modelIndexOf(cell.getTableRow());
                    if (modelIdx >= 0) {
                        if (e.isControlDown()) {
                            holder.getEngine().setBreakpoints(Set.of(modelIdx));
                        } else {
                            // toggle add/remove
                            holder.getEngine().toggleBreakpoint(modelIdx);
                        }
                        instructionsTable.refresh();
                    }
                }
            });
            return cell;
        });
    }

    private int modelIndexOf(TableRow<InstructionDTO> row) {
        InstructionDTO item = row.getItem();
        if (item == null) return -1;
        return instructionsTable.getItems().indexOf(item);
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
                holder.getEngine().clearAllBreakpoints();

                if(newVal!=null) {
                    holder.getEngine().selectProgramOrFunction(newVal);
                    refreshInstructions();
                }
            }
        });
    }

    public void bindCurrentPc(IntegerProperty pcProperty) {
        // listener: refresh table + scroll when PC changes in debug mode
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

    // ==== Internals ==
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
        if (!AnimationManager.isAnimationsEnabled()) return;
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

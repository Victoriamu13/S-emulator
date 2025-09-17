package uiDisplay.components.execution;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VarRow{
    private final StringProperty varName;
    private final StringProperty varValue;

    public VarRow(String varName, String varValue) {
        this.varName = new SimpleStringProperty(varName);
        this.varValue = new SimpleStringProperty(varValue);
    }

    public StringProperty varNameProperty() { return varName; }
    public StringProperty varValueProperty() { return varValue; }

    public String getVarName() { return varName.get(); }
    public String getVarValue() { return varValue.get(); }
    public void setVarValue(String value) { this.varValue.set(value); }
}

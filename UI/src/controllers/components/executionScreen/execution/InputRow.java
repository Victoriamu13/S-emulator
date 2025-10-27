package controllers.components.executionScreen.execution;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InputRow {
    private final StringProperty name;
    private final StringProperty value;

    public InputRow(String name){
        this.name=new SimpleStringProperty(name);
        this.value=new SimpleStringProperty("0");
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty valueProperty() { return value; }

    public String getName() { return name.get(); }
    public String getValue() { return value.get(); }

    public void setValue(String v) { value.set(v); }

}

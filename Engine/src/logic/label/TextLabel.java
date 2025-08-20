package logic.label;

public class TextLabel implements SLabel {
    private final String text;

    public TextLabel(String text) {this.text = text;}
    @Override
    public String getLabelRepresentation() {return text;}
}

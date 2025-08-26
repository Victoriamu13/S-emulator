package logic.label;

public interface SLabel {
    String getLabelRepresentation();

    default boolean isNumberLabel() {
        String rep = getLabelRepresentation();
        if (rep == null || rep.isEmpty() || rep.equals("EXIT")) return false;
        if (rep.charAt(0) != 'L') return false;
        for (int i = 1; i < rep.length(); i++) {
            if (!Character.isDigit(rep.charAt(i))) return false;
        }
        return true;
    }
}

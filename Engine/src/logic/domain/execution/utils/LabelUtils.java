package logic.domain.execution.utils;

import logic.domain.instructions.SInstruction;
import logic.domain.label.SLabel;

import java.util.List;
import java.util.Map;

public class LabelUtils {

    private LabelUtils() {}

    // Build L# → index map once per program/function
    public static Map<String,Integer> indexNumericLabels(List<SInstruction> body) {
        java.util.HashMap<String,Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < body.size(); i++) {
            SLabel lbl = body.get(i).getLabel();
            if (lbl.isNumberLabel()) {
                map.put(lbl.getLabelRepresentation(), i);     // "L12" → 12th instruction
            }
        }
        return map;
    }
}

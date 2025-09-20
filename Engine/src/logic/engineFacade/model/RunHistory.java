package logic.engineFacade.model;

import java.io.Serializable;
import java.util.*;

public final class RunHistory implements Serializable {
    private final List<RunRecord> records = new ArrayList<>();
    private static final long serialVersionUID = 1L;

    public RunRecord add(int degree, long[] inputs, long yValue, long cycles, Map<String, Long> finalVars) {
        int nextNo = records.size() + 1;
        long[] inputsCopy = (inputs == null) ? new long[0] : Arrays.copyOf(inputs, inputs.length);
        Map<String, Long> varsCopy = (finalVars == null) ? Map.of() : Map.copyOf(finalVars);

        RunRecord rec = new RunRecord(nextNo, degree, inputsCopy, yValue, cycles,varsCopy);
        records.add(rec);
        return rec;
    }


    //read only mode
    public List<RunRecord> records() {
        return Collections.unmodifiableList(records);
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public void clear() {
        records.clear();
    }
}

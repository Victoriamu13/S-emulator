package logic.engineFacade.runHistory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RunHistory implements Serializable {
    private final List<RunRecord> records = new ArrayList<>();
    private static final long serialVersionUID = 1L;

    public RunRecord add(int degree, long[] inputs, long yValue, long cycles) {
        int nextNo = records.size() + 1;
        long[] inputsCopy = (inputs == null) ? new long[0] : Arrays.copyOf(inputs, inputs.length);
        RunRecord rec = new RunRecord(nextNo, degree, inputsCopy, yValue, cycles);
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

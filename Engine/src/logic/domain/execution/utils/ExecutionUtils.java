package logic.domain.execution.utils;

import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public final class ExecutionUtils {
    private ExecutionUtils() {}

    // Sum cycles safely
    public static long accumulateCycles(long base, long... more) {
        long total = base;
        for (long m : more) total += m;
        return total;
    }

    // Decide next PC based on label/empty/EXIT
    public static int safeJump(int pc, SLabel next, Map<String,Integer> labelIndex) {
        if (next == SpecialLabels.EXIT) return Integer.MIN_VALUE; // exit sentinel

        if (next.isNumberLabel()) return labelIndex.get(next.getLabelRepresentation());

        return pc + 1;                                            // default: linear
    }

    // Format snapshot to report order: y, x1..xN, z1..zN
    public static Map<String, Long> orderVarsForReport(Map<SVars, Long> snap) {
        LinkedHashMap<String,Long> out=new LinkedHashMap<>();
        out.put("y", snap.getOrDefault(SVars.RESULT, 0L));

        TreeMap<Integer,Long> xMap=new TreeMap<>();
        TreeMap<Integer,Long> zMap=new TreeMap<>();

        for(Map.Entry<SVars,Long> e:snap.entrySet()) {
            SVars v = e.getKey();    //x,z
            long val = e.getValue();

            switch (v.getType()) {
                case INPUT -> xMap.put(Integer.parseInt(v.getRepresentation().substring(1)), val);
                case WORK  -> zMap.put(Integer.parseInt(v.getRepresentation().substring(1)), val);
                default -> {}
            }
        }
        xMap.forEach((i, val) -> out.put("x" + i, val));
        zMap.forEach((i, val) -> out.put("z" + i, val));
        return out;
    }
}
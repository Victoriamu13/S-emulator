package logic.engineFacade.api;

import java.util.Comparator;

public class EngineFacadeUtils {

    public static Comparator<String> numericAwareComparator() {
        return (a, b) -> {
            try {
                String prefixA = a.replaceAll("\\d", "");
                String prefixB = b.replaceAll("\\d", "");
                if (prefixA.equals(prefixB)) {
                    int numA = Integer.parseInt(a.replaceAll("\\D", ""));
                    int numB = Integer.parseInt(b.replaceAll("\\D", ""));
                    return Integer.compare(numA, numB);
                }
            } catch (Exception ignored) {}
            return a.compareTo(b);
        };
    }
}

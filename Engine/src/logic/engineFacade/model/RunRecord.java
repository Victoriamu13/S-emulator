package logic.engineFacade.model;

import java.io.Serializable;

public record RunRecord(int runNo, int degree, long[] inputs, long yValue, long cycles ) implements Serializable {
    private static final long serialVersionUID = 1L;
}

package logic.domain.expand.expandProgram;

import logic.domain.instructions.SInstruction;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.program.functions.FunctionLookup;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DegreeCalculator {
    private final ProgramExpander expander;  // expands synthetic instructions

    public DegreeCalculator(ProgramExpander expander) {
        this.expander = expander;
    }

    // Calculate expansion degree of one instruction
    public int degreeOfInstruction(SInstruction ins) {
        List<SInstruction> current = List.of(ins);   // start with original
        int degree = 0;

        // Expand until only basic instructions remain
        while (true) {
            boolean allBasic = true;
            List<SInstruction> next = new ArrayList<>();

            for (SInstruction inst : current) {
                List<SInstruction> expanded = expander.expandOne(inst);
                if (expanded.size() == 1 && expanded.getFirst() == inst) { //if basic
                    next.add(inst);   // basic → keep

                } else {
                    allBasic = false;     // found synthetic → expand further
                    next.addAll(expanded);
                }
            }
            if (allBasic) {break;}    // all are basic → stop
            degree++;      // one more round
            current = next; // continue to next round with collected instructions
        }
        return degree;
    }

    // Max degree across all instructions in program
    public int maxProgramDegree(SProgram program) {
        int max = 0;
        for (SInstruction ins : program.getInstructions()) {
            max = Math.max(max, degreeOfInstruction(ins));
        }
        return max;
    }

    // calculate max degree of program
    public static int calculateMaxDegree(SProgram program){

        if(program==null || program.getInstructions().isEmpty()) return 0;

        ExpansionContext ctx=ExpansionContext.seedFrom(program);
        ProgramExpander expander=new ProgramExpander(ctx);
        DegreeCalculator calculator=new DegreeCalculator(expander);
        return calculator.maxProgramDegree(program);
    }

    // calculate max degrees of function in program
    public static int calculateMaxDegree(List<SInstruction> instructions,FunctionLookup lookup, String name){
        if(instructions==null || instructions.isEmpty()) return 0;

        SProgramImpl tempProg=new SProgramImpl(name);
        tempProg.setFunctionLookup(lookup);
        instructions.forEach(tempProg::addInstruction);

        return calculateMaxDegree(tempProg);
    }


}

package display;

import logic.instructions.info.InstructionInfo;
import logic.program.info.ProgramInfo;

public abstract class BaseConsoleProgramPrinter implements ProgramPrinter {

    @Override
    public final void display(ProgramInfo info) {
        if (info == null) {
            System.out.println("No program found");
            return;
        }
        printHeader(info);
        for (InstructionInfo inst : info.getInstructions()) {
            System.out.println(formatInstruction(inst));
        }
    }

    protected void printHeader(ProgramInfo info) {
        System.out.println("Program Name: " + info.getName());
        System.out.println("Inputs used: " + info.getInputsUsed());
        System.out.println("Labels used: " + info.getLabelsUsed());
        System.out.println("Instructions:");
    }


    protected abstract String formatInstruction(InstructionInfo inst);
}

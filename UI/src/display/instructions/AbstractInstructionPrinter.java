package display.instructions;

import display.console.ConsoleIO;
import logic.instructions.info.InstructionInfo;
import logic.program.info.ProgramInfo;

import java.io.Console;

public abstract class AbstractInstructionPrinter implements InstructionPrinter {
    private final ConsoleIO io;

    public AbstractInstructionPrinter(ConsoleIO io) {
        this.io = io;
    }

    @Override
    public final void display(ProgramInfo info) {
        if (info == null) {
            System.out.println("No program found");
            return;
        }
        printHeader(info);
        for (InstructionInfo inst : info.getInstructions()) {
            io.println(formatInstruction(inst));
            io.println("");
        }
    }

    protected void printHeader(ProgramInfo info) {
        io.println("Program Name: " + info.getName());
        io.println("Inputs used: " + info.getInputsUsed());
        io.println("Labels used: " + info.getLabelsUsed());
        io.println("Instructions:");
    }


    protected abstract String formatInstruction(InstructionInfo inst);
}

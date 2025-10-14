package console.consoleDisplay.instructions;


import console.consoleDisplay.console.ConsoleIO;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

public abstract class AbstractInstructionDTOPrinter implements InstructionDTOPrinter{
    protected final ConsoleIO io;

    public AbstractInstructionDTOPrinter(ConsoleIO io) {
        this.io = io;
    }

    @Override
    public void display(List<InstructionDTO> instructions, String programName,
                              List<String> inputs, List<String> labels) {
        if (instructions == null || instructions.isEmpty()) {
            io.println("No program found");
            return;
        }

        printHeader(programName, inputs, labels);
        for (InstructionDTO inst : instructions) {
          String line=formatInstruction(inst);
           if(line!=null && !line.isBlank() ) {
               io.println(line);
               io.println("");
           }
       }
    }

    protected void printHeader(String programName, List<String> inputs, List<String> labels) {
        io.println("Program Name: " + programName);
        io.println("Inputs used: " + inputs);
        io.println("Labels used: " + labels);
        io.println("Instructions:");
    }

    protected abstract String formatInstruction(InstructionDTO inst);

    protected static String formatInstructionLine(InstructionDTO inst) {
        String number   = String.format("#%d", inst.index());
        String type     = inst.type();
        String labelBox = String.format("[ %-5s ]", inst.label());
        String command  = inst.command();
        String cycles      = inst.cyclesText();

        return String.format("%s(%s)%s%s(%d)", number, type, labelBox, command, cycles);
    }
}
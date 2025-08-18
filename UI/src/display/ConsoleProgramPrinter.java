package display;

import logic.instructions.InstructionInfo;
import logic.program.ProgramInfo;

public class ConsoleProgramPrinter implements ProgramPrinter {

    @Override
    public void display(ProgramInfo info) {
        if(info==null){
            System.out.println("No program found");
            return;
        }
        System.out.println("Program Name: "+info.getName());
        System.out.println("Inputs used: "+info.getInputsUsed());
        System.out.println("Labels used: "+info.getLabelsUsed());
        System.out.println("Instructions:");
        for(InstructionInfo inst : info.getInstructions()){
            System.out.println(formatInstruction(inst));
        }
    }

    private static String formatInstruction(InstructionInfo inst) {
        String number=String.format("#%d",inst.getIndex());
        String type=inst.isSynthetic()? "S" : "B";
        String labelBox=String.format("[ %-5s ]",inst.getLabelName());
        String command=inst.getFullCommand();
        int cycles=inst.getCycles();

        return String.format("%s(%s)%s%s(%d)",number,type,labelBox,command,cycles);
    }


}

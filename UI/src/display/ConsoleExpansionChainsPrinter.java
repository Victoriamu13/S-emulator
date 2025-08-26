package display;

import logic.instructions.info.InstructionInfo;

public class ConsoleExpansionChainsPrinter extends BaseConsoleProgramPrinter  {

    @Override
    protected String formatInstruction(InstructionInfo inst) {
        return inst.getFullCommand();
    }
}

package consoleDisplay.instructions;

import consoleDisplay.console.ConsoleIO;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.InstructionDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpansionChainsPrinter extends AbstractInstructionDTOPrinter {

    private final EngineFacade engine;
    private final int degree;

    public ExpansionChainsPrinter(ConsoleIO io, EngineFacade engine, int degree) {
        super(io);
        this.engine = engine;
        this.degree = degree;
    }

    @Override
    protected String formatInstruction(InstructionDTO inst) {
        List<InstructionDTO> chain = engine.getExpansionHistoryChain(degree, inst.index());

        return chain.stream()
                .map(AbstractInstructionDTOPrinter::formatInstructionLine)
                .collect(Collectors.joining(" >>> "));
    }
}
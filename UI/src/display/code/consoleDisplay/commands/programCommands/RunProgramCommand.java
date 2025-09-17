package consoleDisplay.commands.programCommands;

import consoleDisplay.instructions.ExpansionChainsPrinter;
import consoleDisplay.instructions.InstructionDTOPrinter;
import consoleDisplay.instructions.InstructionListPrinter;

import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.program.info.ProgramInfo;
import consoleDisplay.validation.Validators;
import logic.engineFacade.model.InstructionDTO;

import java.util.List;

import static logic.engineFacade.api.EngineFacadeUtils.normalizeInputsForProgram;

public class RunProgramCommand implements consoleDisplay.commands.UiCommand {
    private final engineHolder.EngineHolder engineHolder;
    private final consoleDisplay.console.ConsoleIO io;

    public RunProgramCommand(engineHolder.EngineHolder engineHolder, consoleDisplay.console.ConsoleIO io) {
        this.engineHolder = engineHolder;
        this.io = io;
    }

    @Override
    public String title() { return "Run program"; }

    @Override
    public boolean isEnabled() { return engineHolder.hasEngine(); }

    @Override
    public void execute() {
        if (!Validators.requireEngineLoaded(engineHolder, io)) {
            return;
        }
        EngineFacade engine = engineHolder.getEngine();

        //1) Choose expansion degree
        int max = engine.getMaxExpansionDegree();
        int used = io.askExpansionDegree(max);

        //2) Print inputs and receive values from user
        List<String> inputsUsed = engine.getInputsUsed(used);
        io.println("\nInputs used: " + inputsUsed);
        long[] rawInputs = io.askCsvLongs("\nEnter inputs as CSV (e.g., 3,5,0) or leave empty:",engine,used);
        long[] inputs = normalizeInputsForProgram(rawInputs, inputsUsed);

        //3) Run program and deliver report
        ExecutionReport report = engine.runWithReport(used, inputs);

        //Write current program run to history
        engineHolder.history().add(used, inputs, report.yValue(), report.totalCycles());

        //4) Present current program
        io.println("\n=== Program executed  (degree " + used + ") ===");
        List<InstructionDTO> rows = engine.getInstructionRows(used);
        String programName = engine.getProgramName();
        List<String> labels = engine.getLabelsUsed(used);
        InstructionDTOPrinter printer = (used == 0)
                ? new InstructionListPrinter(io)
                : new ExpansionChainsPrinter(io, engine, used);
        printer.display(rows, programName, inputsUsed, labels);

        //5) Print program result - y
        io.println("\n=== Run result ===");
        io.println("y = " + report.yValue());

        //6) Print all variables - y,x1...xN,z1....zM
        io.println("\n=== Final variables values (ordered) ===");
        report.finalVars().forEach((name, val) -> io.println(name + " = " + val));

        //7) Print total number of cycles
        io.println("\nTotal cycles: " + report.totalCycles());
    }

}

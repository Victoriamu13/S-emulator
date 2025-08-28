package commands;

import display.EngineHolder;
import display.console.ConsoleIO;
import display.instructions.ExpansionChainsPrinter;
import display.instructions.InstructionListPrinter;
import display.instructions.InstructionPrinter;
import logic.engineFacade.facade.EngineFacade;
import logic.engineFacade.report.ExecutionReport;
import logic.program.info.ProgramInfo;
import validation.Validators;

public class RunProgramCommand implements UiCommand {
    private final EngineHolder engineHolder;
    private final ConsoleIO io;

    public RunProgramCommand(EngineHolder engineHolder, ConsoleIO io) {
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
        EngineFacade engine = engineHolder.get();

        //1) Choose expansion degree
        int max = engine.getMaxExpansionDegree();
        int used = io.askExpansionDegree(max);

        //2) Print inputs and receive values from user
        ProgramInfo infoForRun = engine.getProgramInfo(used);
        io.println("\nInputs used: " + infoForRun.getInputsUsed());
        long[] rawInputs = io.askCsvLongs("\nEnter inputs as CSV (e.g., 3,5,0) or leave empty:");
        long[] inputs = normalizeInputsForProgram(rawInputs, infoForRun);

        //3) Run program and deliver report
        ExecutionReport report = engine.runWithReport(used, inputs);

        //Write current program run to history
        engineHolder.history().add(used, inputs, report.yValue(), report.totalCycles());

        //4) Present current program
        io.println("\n=== Program executed  (degree " + used + ") ===");
        InstructionPrinter printer = (used == 0)
                ? new InstructionListPrinter()
                : new ExpansionChainsPrinter();
        printer.display(infoForRun);

        //5) Print program result - y
        io.println("\n=== Run result ===");
        io.println("y = " + report.yValue());

        //6) Print all variables - y,x1...xN,z1....zM
        io.println("\nFinal variables (ordered): ");
        report.finalVars().forEach((name, val) -> io.println(name + " = " + val));

        //7) Print total number of cycles
        io.println("\nTotal cycles: " + report.totalCycles());
    }


    //-----helper funcs-----
    private static long[] normalizeInputsForProgram(long[] userInputs, ProgramInfo info) {

        int requiredLen = 0;
        for (String s : info.getInputsUsed()) {
            s = s.trim();
            if (s.matches("x\\d+")) {
                int idx = Integer.parseInt(s.substring(1));
                if (idx > requiredLen) {
                    requiredLen = idx;
                }
            }
        }
        return java.util.Arrays.copyOf(userInputs, requiredLen);
    }

}

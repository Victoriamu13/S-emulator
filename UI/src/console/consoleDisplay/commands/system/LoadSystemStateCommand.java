//package console.consoleDisplay.commands.system;
//
//import console.consoleDisplay.commands.UiCommand;
//import console.consoleDisplay.console.ConsoleIO;
//import logic.engineFacade.api.EngineFacade;
//import logic.engineFacade.api.EngineFacadeImpl;
//import logic.engineFacade.model.RunRecord;
//import logic.engineFacade.model.SystemState;
//
//import java.io.ObjectInputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class LoadSystemStateCommand implements UiCommand {
//    private final engineHolder.EngineHolder holder;
//    private final ConsoleIO io;
//
//    public LoadSystemStateCommand(engineHolder.EngineHolder holder, ConsoleIO io) {
//        this.holder = holder; this.io = io;
//    }
//
//    @Override public String title() { return "Load system state"; }
//    @Override public boolean isEnabled() { return true; }
//
//    @Override
//    public void execute() {
//        String base = io.askLine("Enter full path (without extension) to load state: ");
//        if (base == null || base.isBlank()) { io.println("No path provided."); return; }
//
//        try (ObjectInputStream ois =
//                     new ObjectInputStream(Files.newInputStream(Path.of(base.trim() + ".semu")))) {
//            SystemState s = (SystemState) ois.readObject();
//            EngineFacade eng = holder.hasEngine() ? holder.getEngine() : new EngineFacadeImpl();
//
//            if (s.loadedXmlPath != null && !s.loadedXmlPath.isBlank()) {
//                var res = eng.loadProgram(Path.of(s.loadedXmlPath));
//                if (!res.success()) io.println("Warning: failed reloading XML from saved state.");
//            }
//
//            // clears history and load from file
//            holder.set(eng);
//            for(RunRecord r: s.history.records()){
//                holder.history().add(r.degree(),r.inputs(),r.yValue(),r.cycles(),r.finalVars());
//            }
//            io.println("System state loaded.");
//
//        } catch (Exception e) {
//            io.println("Failed to load state: " + e.getMessage());
//        }
//    }
//}

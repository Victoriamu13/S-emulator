package logic.system.programs.functions.repository;

import logic.domain.expand.expandProgram.DegreeCalculator;
import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.variable.SVars;
import logic.engineFacade.api.EngineFacade;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FunctionRepository {
    private static final Map<String, FunctionEntry> functions = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, List<SInstruction>> functionBodies = new ConcurrentHashMap<>();
    private static Map<String, Integer> functionArities = new ConcurrentHashMap<>();

    private FunctionRepository() {
    }

    public static synchronized void addFunctions(FunctionLookup lookup, String uploader, String programName) {
        if (lookup == null) return;

        for (String fnName : lookup.allFunctionNames()) {
            List<SInstruction> body = lookup.bodyOf(fnName);
            if (body == null || body.isEmpty()) continue;

            String userString = lookup.userStringOf(fnName);
            int maxDegree = DegreeCalculator.calculateMaxDegree(body, lookup, fnName);

            if (functions.containsKey(fnName)) continue;
            FunctionEntry funcEntry = new FunctionEntry(fnName, userString, programName, uploader, body.size(), maxDegree);
            functions.put(fnName, funcEntry);
            functionBodies.put(fnName, body);
        }
        UpdateFlagsManager.markUpdated("functions");
    }

    public static synchronized void registerArity(String fnName, int arity) {
        if (fnName == null) return;
        functionArities.put(fnName, arity);
    }

    public static synchronized Integer getArity(String fnName) {
        if (fnName == null) return null;
        return functionArities.get(fnName);
    }

    public static synchronized Collection<FunctionEntry> allFunctions() {
        return functions.values();
    }


    public static synchronized boolean functionExists(String fnName) {
        if (fnName == null) return false;
        return functions.containsKey(fnName);
    }


    public static synchronized List<SInstruction> getFunctionBody(String fnName) {
        if (fnName == null) return List.of();
        return functionBodies.getOrDefault(fnName, List.of());
    }


    public static synchronized boolean compareBodies(List<SInstruction> a, List<SInstruction> b) {
        if (a.size() != b.size()) return false;

        for (int i = 0; i < a.size(); i++) {
            SInstruction aInst = a.get(i);
            SInstruction bInst = b.get(i);

            if (!aInst.getName().equals(bInst.getName())) return false;
            if (!aInst.getLabel().getLabelRepresentation().equals(bInst.getLabel().getLabelRepresentation()))
                return false;
            if (!aInst.getVariable().getRepresentation().equals(bInst.getVariable().getRepresentation())) return false;
        }
        return true;
    }


    public static synchronized int getFuncsContributedBy(String username) {
        if (username == null) return 0;
        int count = 0;
        for (FunctionEntry funcEntry : functions.values()) {
            if (username.equals(funcEntry.uploader())) {
                count++;
            }
        }
        return count;
    }

    // Retrieve FunctionEntry by function name
    public static FunctionEntry getFunctionEntry(String fnName) {
        return functions.get(fnName);
    }

    public static synchronized String getInternalName(String userString) {
        for (FunctionEntry entry : functions.values()) {
            if (entry.userString().equalsIgnoreCase(userString)) {
                return entry.funcName();
            }
        }
        return userString;
    }

    public static FunctionLookup asLookup() {
        return new FunctionLookup() {
            @Override
            public List<SInstruction> bodyOf(String functionName) {
                return getFunctionBody(functionName);
            }

            @Override
            public List<SVars> argsOf(String functionName) {
                return List.of();
            }

            @Override
            public String userStringOf(String functionName) {
                FunctionEntry entry = functions.get(functionName);
                return (entry != null) ? entry.userString() : functionName;
            }

            @Override
            public Set<String> allFunctionNames() {
                return new HashSet<>(functions.keySet());
            }
        };

    }

    public static void setEngineForFunction(String username, String fnName, EngineFacade engine) {
        EngineFacadeManager.registerEngine(username, fnName, engine);
    }

    public static EngineFacade getEngineForFunction(String username, String fnName) {
        return EngineFacadeManager.getEngine(username, fnName);
    }
}

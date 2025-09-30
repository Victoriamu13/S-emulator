package logic.domain.execution.context;

import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputCollector {

    public static Set<String> collectInputs(List<ComposeArgument> args){
        Set<String> inputs = new HashSet<>();

        for(ComposeArgument arg : args){
            if(arg instanceof VarArgument varArg){
                String name = varArg.getName();
                if (name != null && name.matches("x\\d+")) {
                    inputs.add(name);
                }
            }
            else if(arg instanceof FuncCallArgument funcArg){
                inputs.addAll(collectInputs(funcArg.getArguments()));
            }
        }
        return inputs;
    }
}

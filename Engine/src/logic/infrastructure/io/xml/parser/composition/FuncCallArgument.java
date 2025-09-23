package logic.infrastructure.io.xml.parser.composition;

import java.util.List;

public class FuncCallArgument implements ComposeArgument{

    private final String functionName;
    private final List<ComposeArgument> args;

    public FuncCallArgument(String functionName,List<ComposeArgument> args){
        this.functionName=functionName;
        this.args=(args == null) ? java.util.List.of() : java.util.List.copyOf(args);;
    }

    public String getFunctionName(){return functionName;}
    public List<ComposeArgument> getArguments(){return args;}

    @Override public String toString() {
        return "(" + functionName + (args.isEmpty() ? "" : "," + args) + ")";
    }
}

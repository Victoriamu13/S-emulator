package logic.infrastructure.io.xml.parser.composition;

public class VarArgument implements ComposeArgument{

    private final String name;

    public VarArgument(String name){this.name=name;}

    public String getName(){return name;}

    @Override public String toString() { return name; }
}

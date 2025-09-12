package logic.domain.variable;

public interface SVars {
    SVarsType getType();
    String getRepresentation();

    SVars RESULT=new SVarsImpl(SVarsType.RESULT,0); //public static final
}

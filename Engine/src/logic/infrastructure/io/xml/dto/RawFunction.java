package logic.infrastructure.io.xml.dto;

import java.util.List;

public record RawFunction (String name,String userString,  List<RawInstructions> body) { }

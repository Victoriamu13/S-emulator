package logic.infrastructure.io.xml.dto;

import java.util.List;

public record RawFunction (String name, List<RawInstructions> body) { }

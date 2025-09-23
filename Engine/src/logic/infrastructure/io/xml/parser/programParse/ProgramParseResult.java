package logic.infrastructure.io.xml.parser.programParse;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.List;

public record ProgramParseResult(String programName, List<RawInstructions> raw,
                                 List<RawFunction> functions, List<String> errors) { }

/*Checks if the XML file is structurally valid.
-------
Holds:
-------
String programName → the name of the program from the XML attribute.
List<RawInst> rawInstructions → all the raw instructions parsed.
List<String> errors → parsing errors (e.g., malformed XML, missing fields).*/

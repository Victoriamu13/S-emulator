package logic.io.xml;

import java.util.List;

record ParseResult(String programName, List<RawInstructions> raw, List<String> errors) { }

/*Checks if the XML file is structurally valid.
-------
Holds:
-------
String programName → the name of the program from the XML attribute.
List<RawInst> rawInstructions → all the raw instructions parsed.
List<String> errors → parsing errors (e.g., malformed XML, missing fields).*/

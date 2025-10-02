package logic.infrastructure.io.xml.dto;

import java.util.Map;

public record RawInstructions(int line, String typeAttr, String name, String varText, String labelText, Map<String,String> args) { }

/*Represents a single raw instruction as it was parsed from the XML file, before validation and conversion into the real SInstruction objects.
-------
Holds:
-------
int line → the line number in the XML file
String typeAttr → the type attribute from <S-Instruction>
String name → the instruction name (e.g., "INCREASE", "JUMP_NOT_ZERO").
String varText → the variable name text inside <S-Variable> (e.g., "x1", "y").
String labelText → the label attached to the instruction, if any (<S-Label>).
String jnzTarget → the jump target for JUMP_NOT_ZERO (value of JNZLabel).*/




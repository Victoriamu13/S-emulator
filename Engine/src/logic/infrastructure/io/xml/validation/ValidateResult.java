package logic.infrastructure.io.xml.validation;

import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.List;

public record ValidateResult(List<RawInstructions> raw, List<String> errors) { }

/*Checks if the program in the XML file is legal in the S-emulator language.
-------
Holds:
-------
List<RawInst> raw-> This is the list of raw instructions parsed from the XML file.
List<String> errors-> This is a list of all validation error messages found while checking the file.*/
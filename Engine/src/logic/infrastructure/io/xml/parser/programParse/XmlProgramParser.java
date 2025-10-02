package logic.infrastructure.io.xml.parser.programParse;

import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.infrastructure.io.xml.parser.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static logic.infrastructure.io.xml.parser.utils.DomUtils.first;
import static logic.infrastructure.io.xml.parser.utils.DomUtils.readInstructions;

public final class XmlProgramParser {

    // ==== Parse xml file ====
    public ProgramParseResult parse(Path xmlPath){
        List<String> errors = new ArrayList<>();

        Document doc= DomUtils.safeParse(xmlPath,errors);
        if(!errors.isEmpty() || doc==null){
            return new ProgramParseResult(null,List.of(),List.of(),errors);
        }

        Element progEl= first(doc,"S-Program");
        if(progEl==null){
            errors.add("Missing S-Program element.");
            return new ProgramParseResult(null,List.of(),List.of(),errors);
        }

        String programName=progEl.getAttribute("name");

        Element instEl= first(progEl,"S-Instructions");
        if(instEl==null){
            errors.add("Missing S-Instructions element inside S-Program.");
            return new ProgramParseResult(null,List.of(),List.of(),errors);
        }

        List<RawInstructions> raw = readInstructions(instEl);
        if (raw.isEmpty()) {
            errors.add("No S-Instruction elements found inside S-Instructions.");
            return new ProgramParseResult(null,List.of(),List.of(),errors);
        }


        // ==== Parse functions ====
        List<RawFunction> functions = new ArrayList<>();
        Element funcsEl = first(progEl, "S-Functions");
        if (funcsEl != null) {
            NodeList fnNodes = funcsEl.getElementsByTagName("S-Function");
            for (int f = 0; f < fnNodes.getLength(); f++) {
                Element fnEl = (Element) fnNodes.item(f);
                String fnName = fnEl.getAttribute("name");
                String userString = fnEl.getAttribute("user-string");

                Element fnInstEl = first(fnEl, "S-Instructions");
                List<RawInstructions> fnBody = readInstructions(fnInstEl);

                functions.add(new RawFunction(fnName,userString, fnBody));
            }
        }
        return new ProgramParseResult(programName,raw,functions,errors);
    }
}

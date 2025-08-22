package logic.io.xml.parser;

import logic.io.xml.dto.RawInstructions;
import logic.io.xml.parser.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class XmlProgramParser {

    public ParseResult parse(Path xmlPath){
        List<String> errors = new ArrayList<>();

        Document doc= DomUtils.safeParse(xmlPath,errors);
        if(!errors.isEmpty() || doc==null){
            return new ParseResult(null,List.of(),errors);
        }

        Element progEl=DomUtils.first(doc,"S-Program");
        if(progEl==null){
            errors.add("Missing S-Program element.");
            return new ParseResult(null,List.of(),errors);
        }

        String programName=progEl.getAttribute("name");

        Element instEl=DomUtils.first(progEl,"S-Instructions");
        if(instEl==null){
            errors.add("Missing S-Instructions element inside S-Program.");
            return new ParseResult(null,List.of(),errors);
        }

        NodeList instNodes=instEl.getElementsByTagName("S-Instruction");
        if(instNodes.getLength()==0){
            errors.add("No S-Instruction elements found inside S-Instructions.");
            return new ParseResult(null,List.of(),errors);
        }

        List<RawInstructions> raw=new ArrayList<>(instNodes.getLength());
        for(int i=0; i<instNodes.getLength(); i++) {
            Element e = (Element) instNodes.item(i);
            raw.add(new RawInstructions(i + 1,
                    e.getAttribute("type"),
                    e.getAttribute("name"),
                    DomUtils.childText(e, "S-Variable"),
                    DomUtils.childText(e, "S-Label"),
                    DomUtils.allArgs(e)));
        }
        return new ParseResult(programName,raw,errors);
    }
}

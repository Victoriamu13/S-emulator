package logic.io.xml;

import logic.program.SProgram;
import logic.program.SProgramImpl;
import logic.variable.SVars;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class XmlProgramLoader {

    public LoadResult load(Path xmlPath) {
        List<String> errors = new ArrayList<String>();

        //-------Parse XML file-------//

        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Security hardening (prevent XXE etc.)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setExpandEntityReferences(false);

            //xml file will be loaded to doc with DOM Parser
            doc = dbf.newDocumentBuilder().parse(xmlPath.toFile());

        } catch (Exception e) {
            return LoadResult.failed(List.of("Failed to parse XML: " + e.getMessage()));
        }

        // ----- Top-level structure checks -----
        Element progEl = (Element) doc.getElementsByTagName("S-Program").item(0);
        if (progEl == null) {
            return LoadResult.failed(List.of("Missing S-Program element."));
        }

        String programName = progEl.getAttribute("name");
        if (isBlank(programName)) {
            errors.add("Missing 'name' attribute in S-Program element.");
        }

        Element instrsEl = (Element) progEl.getElementsByTagName("S-Instructions").item(0);
        if (instrsEl == null) {
            errors.add("Missing S-Instructions element inside S-Program.");
        } else {
            NodeList instructionNodes = instrsEl.getElementsByTagName("S-Instruction");
            if (instructionNodes.getLength() == 0) {
                errors.add("No S-Instruction elements found inside S-Instructions.");
            }else{ //-----LABELS VALIDATE-----//
                Set<String> defLabels=collectDefinedLabels(instructionNodes,errors);
                Map<Integer,String> refLabels=collectReferencedLabels(instructionNodes);
                validateLabelRefs(refLabels,defLabels,errors);
            }
        }

        if (!errors.isEmpty()) {
            return LoadResult.failed(errors);
        }

        SProgram program = new SProgramImpl(programName.trim());
        return LoadResult.success(program);
    }



//Helper funcs
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String getFirstChildText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent().trim();
    }

    private static Set<String> collectDefinedLabels(NodeList instructionNodes, List<String> errors) {
        Set<String> definedLabels = new LinkedHashSet<String>();
        for(int i=0;i<instructionNodes.getLength();i++) {
            Element instEl= (Element) instructionNodes.item(i);
            String lbl = getFirstChildText(instEl, "S-Label");
            if(isBlank(lbl))continue;

            if("EXIT".equals(lbl)) {
                errors.add("Instruction #" + (i + 1) + ": 'EXIT' cannot be used as a line label.");
            }else if(!lbl.matches("L[1-9][0-9]*")){
                errors.add("Instruction #" + (i + 1) + ": Invalid label '" + lbl + "'. Expected L<number>.");
            }else{
                definedLabels.add(lbl);
            }
        }
        return definedLabels;
    }

    private static Map<Integer,String> collectReferencedLabels(NodeList instructionNodes){
        Map<Integer,String> refs=new LinkedHashMap<>();
        for(int i=0;i<instructionNodes.getLength();i++) {
            Element instEl= (Element) instructionNodes.item(i);
            NodeList args = instEl.getElementsByTagName("S-Instruction-Argument");

            for(int j=0;j<args.getLength();j++) {
                Element argEl= (Element) args.item(j);
                String name=argEl.getAttribute("name");
                if(name!=null && name.toLowerCase(Locale.ROOT).contains("Label")){
                    String val=argEl.getAttribute("value");
                    if(!isBlank(val)){
                        refs.put(i+1,val.trim());
                    }
                }
            }
        }
        return refs;
    }

    private static void validateLabelRefs(Map<Integer,String> refs,Set<String> defined,List<String> errors) {
        for(Map.Entry<Integer,String> e:refs.entrySet()) {
            int line=e.getKey();
            String label=e.getValue();
            if("EXIT".equals(label)) continue;
            if(!defined.contains(label)){
                errors.add("Instruction #" + line + ": Reference to undefined label '" + label + "'.");
            }
        }
    }


}
package logic.infrastructure.io.xml.parser.utils;

import logic.infrastructure.io.xml.dto.RawInstructions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DomUtils {
    private DomUtils() {}

    public static Document safeParse(InputStream inputStream, List<String> errors) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setExpandEntityReferences(false);
            dbf.setXIncludeAware(false);
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            errors.add("Failed to parse XML: " + e.getMessage());
            return null;
        }
    }

    // ==== Get first Element in Document ====
    public static Element first(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagName(tag);
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

    // ==== Get first Element in Section ====
    public static Element first(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

    // ==== Get text content of first Element in Section ====
    public static String childText(Element parent, String tag) {
        Element e = first(parent, tag);
        return e == null ? "" : e.getTextContent().trim();
    }

    // ==== Get all arguments of an instruction ====
    public static Map<String,String> allArgs(Element instEl) {
        Map<String,String> out = new LinkedHashMap<>();
        NodeList args = instEl.getElementsByTagName("S-Instruction-Argument");

        for (int j = 0; j < args.getLength(); j++) {
            Element a = (Element) args.item(j);
            String n=a.getAttribute("name");
            if(n==null || n.isBlank())continue;
            String v=a.getAttribute("value");
            String vText=v==null ? "":v.trim();
            out.put(n.trim(),vText);
        }
        return out;
    }

    // ==== Build RawInstructions from Element ====
    public static RawInstructions rawOf(Element instEl, int line) {
        return new RawInstructions(
                line,
                instEl.getAttribute("type"),
                instEl.getAttribute("name"),
                childText(instEl, "S-Variable"),
                childText(instEl, "S-Label"),
                allArgs(instEl)
        );
    }

    // ==== Read all instructions from <S-Instructions> section ====
    public static List<RawInstructions> readInstructions(Element sInstructionsEl) {
        if (sInstructionsEl == null) return List.of();
        NodeList nodes = sInstructionsEl.getElementsByTagName("S-Instruction");
        List<RawInstructions> out = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            out.add(rawOf(e, i + 1));
        }
        return out;
    }
}

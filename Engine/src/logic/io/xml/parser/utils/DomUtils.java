package logic.io.xml.parser.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DomUtils {
    private DomUtils() {}

    public static Document safeParse(Path xmlPath, List<String> errors) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setExpandEntityReferences(false);
            return dbf.newDocumentBuilder().parse(xmlPath.toFile());
        } catch (Exception e) {
            errors.add("Failed to parse XML: " + e.getMessage());
            return null;
        }
    }
//Get first Element in Document
    public static Element first(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagName(tag);
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

//Get first Element in Section
    public static Element first(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

    public static String childText(Element parent, String tag) {
        Element e = first(parent, tag);
        return e == null ? "" : e.getTextContent().trim();
    }

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
}

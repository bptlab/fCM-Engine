package de.hpi.bpt.fcm.engine.model;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainModel {
    protected DataClazz[] classes;

    public static DomainModel fromFile(File umlFile) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(umlFile);
        Element root = document.getRootElement();
        Namespace xmiNamespace = root.getNamespace("xmi");
        Map<String, List<Element>> elements = root.getChildren("packagedElement").stream()
                .collect(Collectors.groupingBy(element -> element.getAttributeValue("type", xmiNamespace)));
        DomainModel domainModel = new DomainModel();
        List<Element> classElements = elements.get("uml:Class");
        domainModel.classes = new DataClazz[classElements.size()];
        for (int i = 0; i < classElements.size(); i++) {
            DataClazz clazz = new DataClazz();
            clazz.name = classElements.get(i).getAttributeValue("name");
            List<Element> attributeElements = classElements.get(i).getChildren("ownedAttribute");
            clazz.attributes = new String[attributeElements.size()];
            clazz.types = new String[attributeElements.size()];
            clazz.areLists = new boolean[attributeElements.size()];
            for (int j = 0; j < attributeElements.size(); j++) {
                clazz.attributes[j] = attributeElements.get(j).getAttributeValue("name");
                clazz.types[j] = getTypeFor(attributeElements.get(j).getChild("type").getAttributeValue("href"));
                clazz.areLists[j] = !(null == attributeElements.get(j).getChild("upperValue"));
            }
            domainModel.classes[i] = clazz;
        }
        return domainModel;
    }

    private static String getTypeFor(String attribute) {
        if (attribute.contains("Integer")) {
            return "int";
        } else if (attribute.contains("Date")) {
            return "date";
        } else {
            return "string";
        }
    }
}

package dev.aetlas.bootdoctor.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class PomInspector {

    public PomInfo inspect(ProjectContext context) throws IOException {
        if (!context.hasFile("pom.xml")) {
            return PomInfo.invalid("pom.xml not found");
        }

        try (InputStream input =
                java.nio.file.Files.newInputStream(
                        context.resolve(java.nio.file.Path.of("pom.xml")))) {
            DocumentBuilderFactory factory = secureFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            Document document = builder.parse(input);
            Element project = document.getDocumentElement();
            if (project == null || !"project".equals(localName(project))) {
                return PomInfo.invalid("pom.xml root element is not <project>");
            }

            Set<String> dependencies = coordinates(document, "dependency");
            Set<String> plugins = coordinates(document, "plugin");
            String parent = directCoordinate(firstDirectChild(project, "parent"));

            boolean springBootProject =
                    "org.springframework.boot:spring-boot-starter-parent".equals(parent)
                            || dependencies.stream()
                                    .anyMatch(
                                            value -> value.startsWith("org.springframework.boot:"))
                            || plugins.stream()
                                    .anyMatch(value -> value.endsWith(":spring-boot-maven-plugin"))
                            || dependencies.contains(
                                    "org.springframework.boot:spring-boot-dependencies");

            return new PomInfo(true, springBootProject, dependencies, Optional.empty());
        } catch (ParserConfigurationException | SAXException exception) {
            return PomInfo.invalid("pom.xml could not be parsed safely: " + exception.getMessage());
        }
    }

    private DocumentBuilderFactory secureFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private Set<String> coordinates(Document document, String elementName) {
        Set<String> values = new HashSet<>();
        NodeList nodes = document.getElementsByTagNameNS("*", elementName);
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element element) {
                String coordinate = directCoordinate(element);
                if (!coordinate.startsWith(":")) {
                    values.add(coordinate);
                }
            }
        }
        return values;
    }

    private String directCoordinate(Element element) {
        if (element == null) {
            return "";
        }
        return directText(element, "groupId") + ":" + directText(element, "artifactId");
    }

    private Element firstDirectChild(Element parent, String childName) {
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && childName.equals(localName(element))) {
                return element;
            }
        }
        return null;
    }

    private String directText(Element parent, String childName) {
        Element child = firstDirectChild(parent, childName);
        return child == null ? "" : child.getTextContent().trim();
    }

    private String localName(Node node) {
        return node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
    }
}

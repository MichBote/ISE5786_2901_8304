package scene.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import primitives.Color;
import primitives.Point;

/**
 * XML parser that reads a scene definition into a {@link SceneDescriptor}.
 */
final class XmlSceneParser {
   private XmlSceneParser() { }

   static SceneDescriptor parse(Path xmlPath) {
      Document document = parseDocument(xmlPath);
      Element sceneElement = document.getDocumentElement();
      if (sceneElement == null || !"scene".equals(sceneElement.getTagName())) {
         throw new IllegalArgumentException("Root element must be <scene>");
      }

      String name = "Using XML";
      Color background = parseColorAttribute(sceneElement, "background-color", Color.BLACK);

      Color ambientColor = Color.BLACK;
      NodeList ambientNodes = sceneElement.getElementsByTagName("ambient-light");
      if (ambientNodes.getLength() > 0) {
         Element ambientElement = (Element) ambientNodes.item(0);
         ambientColor = parseColorAttribute(ambientElement, "color", Color.BLACK);
      }

      List<GeometryDescriptor> geometryDescriptors = new ArrayList<>();
      NodeList geometriesNodes = sceneElement.getElementsByTagName("geometries");
      if (geometriesNodes.getLength() > 0) {
         Element geometriesElement = (Element) geometriesNodes.item(0);
         NodeList children = geometriesElement.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element element = (Element) node;
            switch (element.getTagName()) {
               case "sphere" -> geometryDescriptors.add(parseSphere(element));
               case "triangle" -> geometryDescriptors.add(parseTriangle(element));
               default -> {
                  // ignore unknown geometry types for forward compatibility
               }
            }
         }
      }

      return new SceneDescriptor(name, background, ambientColor, geometryDescriptors);
   }

   private static Document parseDocument(Path xmlPath) {
      try (InputStream inputStream = Files.newInputStream(xmlPath)) {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

         // Secure processing: prevent XXE
         factory.setExpandEntityReferences(false);
         factory.setNamespaceAware(false);
         try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         } catch (ParserConfigurationException ignored) {
            // Best-effort: some parsers might not support this feature
         }

         DocumentBuilder builder = factory.newDocumentBuilder();
         Document document = builder.parse(inputStream);
         document.getDocumentElement().normalize();
         return document;
      } catch (IOException | ParserConfigurationException | SAXException e) {
         throw new IllegalStateException("Failed to parse XML scene file: " + xmlPath, e);
      }
   }

   private static SphereDescriptor parseSphere(Element sphereElement) {
      Point center = parsePointAttribute(sphereElement, "center");
      double radius = parseDoubleAttribute(sphereElement, "radius");
      return new SphereDescriptor(center, radius);
   }

   private static TriangleDescriptor parseTriangle(Element triangleElement) {
      Point p0 = parsePointAttribute(triangleElement, "p0");
      Point p1 = parsePointAttribute(triangleElement, "p1");
      Point p2 = parsePointAttribute(triangleElement, "p2");
      return new TriangleDescriptor(p0, p1, p2);
   }

   private static double parseDoubleAttribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         throw new IllegalArgumentException("Missing attribute '" + attributeName + "' in <" + element.getTagName() + "> element");
      }
      return Double.parseDouble(value.trim());
   }

   private static Point parsePointAttribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         throw new IllegalArgumentException("Missing attribute '" + attributeName + "' in <" + element.getTagName() + "> element");
      }

      String[] parts = value.trim().split("\\s+");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Expected 3 numbers for point attribute '" + attributeName + "' but got: " + value);
      }
      return new Point(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }

   private static Color parseColorAttribute(Element element, String attributeName, Color defaultColor) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         return defaultColor;
      }

      String[] parts = value.trim().split("\\s+");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Expected 3 numbers for color attribute '" + attributeName + "' but got: " + value);
      }
      return new Color(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }
}

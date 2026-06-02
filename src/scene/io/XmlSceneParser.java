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
import primitives.Double3;
import primitives.Point;
import primitives.Vector;

/**
 * XML parser that reads a scene definition into a {@link SceneDescriptor}.
 */
final class XmlSceneParser {
   /** Utility class constructor. */
   private XmlSceneParser() { }

   /**
    * Parses a scene XML file.
    *
    * @param xmlPath XML scene file path
    * @return parsed scene descriptor
    */
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

      List<LightDescriptor> lightDescriptors = new ArrayList<>();
      NodeList lightsNodes = sceneElement.getElementsByTagName("lights");
      if (lightsNodes.getLength() > 0) {
         Element lightsElement = (Element) lightsNodes.item(0);
         NodeList children = lightsElement.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element element = (Element) node;
            switch (element.getTagName()) {
               case "directional-light" -> lightDescriptors.add(parseDirectionalLight(element));
               case "point-light" -> lightDescriptors.add(parsePointLight(element));
               case "spot-light" -> lightDescriptors.add(parseSpotLight(element));
               default -> {
                  // ignore unknown light types for forward compatibility
               }
            }
         }
      }

      return new SceneDescriptor(name, background, ambientColor, geometryDescriptors, lightDescriptors);
   }

   /**
    * Parses an XML document from disk.
    *
    * @param xmlPath XML file path
    * @return parsed DOM document
    */
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

   /**
    * Parses a sphere element.
    *
    * @param sphereElement sphere XML element
    * @return parsed sphere descriptor
    */
   private static SphereDescriptor parseSphere(Element sphereElement) {
      Point center = parsePointAttribute(sphereElement, "center");
      double radius = parseDoubleAttribute(sphereElement, "radius");
      Color emission = parseColorAttribute(sphereElement, "emission", null);
      Double3 kA = parseDouble3Attribute(sphereElement, "kA");
      Double3 kD = parseDouble3Attribute(sphereElement, "kD");
      Double3 kS = parseDouble3Attribute(sphereElement, "kS");
      Integer nShininess = parseIntAttribute(sphereElement, "nShininess");
      return new SphereDescriptor(center, radius, emission, kA, kD, kS, nShininess);
   }

   /**
    * Parses a triangle element.
    *
    * @param triangleElement triangle XML element
    * @return parsed triangle descriptor
    */
   private static TriangleDescriptor parseTriangle(Element triangleElement) {
      Point p0 = parsePointAttribute(triangleElement, "p0");
      Point p1 = parsePointAttribute(triangleElement, "p1");
      Point p2 = parsePointAttribute(triangleElement, "p2");
      Color emission = parseColorAttribute(triangleElement, "emission", null);
      Double3 kA = parseDouble3Attribute(triangleElement, "kA");
      Double3 kD = parseDouble3Attribute(triangleElement, "kD");
      Double3 kS = parseDouble3Attribute(triangleElement, "kS");
      Integer nShininess = parseIntAttribute(triangleElement, "nShininess");
      return new TriangleDescriptor(p0, p1, p2, emission, kA, kD, kS, nShininess);
   }

   private static DirectionalLightDescriptor parseDirectionalLight(Element element) {
      Color intensity = parseColorAttribute(element, "intensity", Color.BLACK);
      Vector direction = parseVectorAttribute(element, "direction");
      return new DirectionalLightDescriptor(intensity, direction);
   }

   private static PointLightDescriptor parsePointLight(Element element) {
      Color intensity = parseColorAttribute(element, "intensity", Color.BLACK);
      Point position = parsePointAttribute(element, "position");
      Double kC = parseDoubleOptionalAttribute(element, "kC", "kc");
      Double kL = parseDoubleOptionalAttribute(element, "kL", "kl");
      Double kQ = parseDoubleOptionalAttribute(element, "kQ", "kq");
      return new PointLightDescriptor(intensity, position, kC, kL, kQ);
   }

   private static SpotLightDescriptor parseSpotLight(Element element) {
      Color intensity = parseColorAttribute(element, "intensity", Color.BLACK);
      Point position = parsePointAttribute(element, "position");
      Vector direction = parseVectorAttribute(element, "direction");
      Double kC = parseDoubleOptionalAttribute(element, "kC", "kc");
      Double kL = parseDoubleOptionalAttribute(element, "kL", "kl");
      Double kQ = parseDoubleOptionalAttribute(element, "kQ", "kq");
      Integer narrowBeam = parseIntAttribute(element, "narrowBeam");
      return new SpotLightDescriptor(intensity, position, direction, kC, kL, kQ, narrowBeam);
   }

   /**
    * Parses an optional Double3 attribute.
    * <p>
    * Accepts either one number (replicated to all components) or three numbers.
    * </p>
    *
    * @param element XML element containing the attribute
    * @param attributeName attribute name
    * @return parsed Double3, or {@code null} if the attribute is missing
    */
   private static Double3 parseDouble3Attribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         return null;
      }

      String[] parts = value.trim().split("\\s+");
      if (parts.length == 1) {
         return new Double3(Double.parseDouble(parts[0]));
      }
      if (parts.length == 3) {
         return new Double3(
            Double.parseDouble(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2])
         );
      }
      throw new IllegalArgumentException(
         "Expected 1 or 3 numbers for Double3 attribute '" + attributeName + "' but got: " + value
      );
   }

   private static Integer parseIntAttribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         return null;
      }
      return Integer.parseInt(value.trim());
   }

   private static Double parseDoubleOptionalAttribute(Element element, String... attributeNames) {
      for (String name : attributeNames) {
         String value = element.getAttribute(name);
         if (value != null && !value.isBlank()) {
            return Double.parseDouble(value.trim());
         }
      }
      return null;
   }

   private static Vector parseVectorAttribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         throw new IllegalArgumentException(
            "Missing attribute '" + attributeName + "' in <" + element.getTagName() + "> element"
         );
      }

      String[] parts = value.trim().split("\\s+");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Expected 3 numbers for vector attribute '" + attributeName + "' but got: " + value);
      }
      return new Vector(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }

   /**
    * Parses a required double attribute.
    *
    * @param element XML element containing the attribute
    * @param attributeName attribute name
    * @return parsed double value
    */
   private static double parseDoubleAttribute(Element element, String attributeName) {
      String value = element.getAttribute(attributeName);
      if (value == null || value.isBlank()) {
         throw new IllegalArgumentException("Missing attribute '" + attributeName + "' in <" + element.getTagName() + "> element");
      }
      return Double.parseDouble(value.trim());
   }

   /**
    * Parses a required point attribute.
    *
    * @param element XML element containing the attribute
    * @param attributeName attribute name
    * @return parsed point
    */
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

   /**
    * Parses an optional color attribute.
    *
    * @param element XML element containing the attribute
    * @param attributeName attribute name
    * @param defaultColor color returned when the attribute is missing
    * @return parsed color or the default color
    */
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

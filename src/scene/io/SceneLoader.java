package scene.io;

import java.nio.file.Path;

import scene.Scene;

/**
 * Loads {@link Scene} definitions from external files.
 * <p>
 * This class is responsible for locating the input file and delegating the
 * parsing/building work to dedicated components.
 * </p>
 */
public final class SceneLoader {
   private SceneLoader() { }

   /**
    * Loads a scene from an XML file located under the project's {@code xml/}
    * directory.
    *
    * @param  xmlName scene file name without extension (e.g. "basicRenderTestTwoColors")
    * @return         parsed scene
    */
   public static Scene loadFromXml(String xmlName) {
      Path path = Path.of(System.getProperty("user.dir"), "xml", xmlName + ".xml");
      SceneDescriptor descriptor = XmlSceneParser.parse(path);
      return SceneBuilder.build(descriptor);
   }
}

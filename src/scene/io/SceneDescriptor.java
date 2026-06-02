package scene.io;

import java.util.List;

import primitives.Color;

/**
 * Parsed scene data (no rendering logic).
 */
final class SceneDescriptor {
   /** Scene name. */
   final String name;
   /** Background color. */
   final Color background;
   /** Ambient light color. */
   final Color ambientLight;
   /** Parsed geometry descriptors. */
   final List<GeometryDescriptor> geometries;

   /** Parsed external light descriptors. */
   final List<LightDescriptor> lights;

   /**
    * Constructs parsed scene data.
    *
    * @param name scene name
    * @param background background color
    * @param ambientLight ambient light color
    * @param geometries parsed geometry descriptors
    */
   SceneDescriptor(String name, Color background, Color ambientLight, List<GeometryDescriptor> geometries, List<LightDescriptor> lights) {
      this.name = name;
      this.background = background;
      this.ambientLight = ambientLight;
      this.geometries = geometries;
      this.lights = lights;
   }
}

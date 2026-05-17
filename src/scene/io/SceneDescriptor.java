package scene.io;

import java.util.List;

import primitives.Color;

/**
 * Parsed scene data (no rendering logic).
 */
final class SceneDescriptor {
   final String name;
   final Color background;
   final Color ambientLight;
   final List<GeometryDescriptor> geometries;

   SceneDescriptor(String name, Color background, Color ambientLight, List<GeometryDescriptor> geometries) {
      this.name = name;
      this.background = background;
      this.ambientLight = ambientLight;
      this.geometries = geometries;
   }
}

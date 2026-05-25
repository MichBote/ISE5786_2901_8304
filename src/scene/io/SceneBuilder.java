package scene.io;

import geometries.impl.Geometries;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import scene.Scene;

/**
 * Converts parsed {@link SceneDescriptor} data into runtime {@link Scene} objects.
 */
final class SceneBuilder {
   /** Utility class constructor. */
   private SceneBuilder() { }

   /**
    * Builds a runtime scene from parsed scene data.
    *
    * @param descriptor parsed scene descriptor
    * @return runtime scene
    */
   static Scene build(SceneDescriptor descriptor) {
      Scene scene = new Scene(descriptor.name)
         .setBackground(descriptor.background)
         .setAmbientLight(new AmbientLight(descriptor.ambientLight))
         .setGeometries(new Geometries());

      for (GeometryDescriptor geometry : descriptor.geometries) {
         if (geometry instanceof SphereDescriptor sphere) {
            scene.geometries.add(new Sphere(sphere.center(), sphere.radius()));
         } else if (geometry instanceof TriangleDescriptor triangle) {
            scene.geometries.add(new Triangle(triangle.p0(), triangle.p1(), triangle.p2()));
         }
      }

      return scene;
   }
}

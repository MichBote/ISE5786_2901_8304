package scene.io;

import geometries.impl.Geometries;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import primitives.Material;
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
            Sphere s = new Sphere(sphere.center(), sphere.radius());
            if (sphere.emission() != null) {
               s.setEmission(sphere.emission());
            }
            if (sphere.kA() != null) {
               s.setMaterial(new Material().setKA(sphere.kA()));
            }
            scene.geometries.add(s);
         } else if (geometry instanceof TriangleDescriptor triangle) {
            Triangle t = new Triangle(triangle.p0(), triangle.p1(), triangle.p2());
            if (triangle.emission() != null) {
               t.setEmission(triangle.emission());
            }
            if (triangle.kA() != null) {
               t.setMaterial(new Material().setKA(triangle.kA()));
            }
            scene.geometries.add(t);
         }
      }

      return scene;
   }
}

package scene.io;

import geometries.impl.Geometries;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
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
            applyMaterial(s, sphere.kA(), sphere.kD(), sphere.kS(), sphere.kT(), sphere.kR(), sphere.nShininess());
            scene.geometries.add(s);
         } else if (geometry instanceof TriangleDescriptor triangle) {
            Triangle t = new Triangle(triangle.p0(), triangle.p1(), triangle.p2());
            if (triangle.emission() != null) {
               t.setEmission(triangle.emission());
            }
            applyMaterial(t, triangle.kA(), triangle.kD(), triangle.kS(), triangle.kT(), triangle.kR(), triangle.nShininess());
            scene.geometries.add(t);
         }
      }

      for (LightDescriptor light : descriptor.lights) {
         if (light instanceof DirectionalLightDescriptor dl) {
            scene.lights.add(new DirectionalLight(dl.intensity(), dl.direction()));
         } else if (light instanceof PointLightDescriptor pl) {
            PointLight pointLight = new PointLight(pl.intensity(), pl.position());
            if (pl.kC() != null) pointLight.setKc(pl.kC());
            if (pl.kL() != null) pointLight.setKl(pl.kL());
            if (pl.kQ() != null) pointLight.setKq(pl.kQ());
            scene.lights.add(pointLight);
         } else if (light instanceof SpotLightDescriptor sl) {
            SpotLight spotLight = new SpotLight(sl.intensity(), sl.position(), sl.direction());
            if (sl.kC() != null) spotLight.setKc(sl.kC());
            if (sl.kL() != null) spotLight.setKl(sl.kL());
            if (sl.kQ() != null) spotLight.setKq(sl.kQ());
            if (sl.narrowBeam() != null) spotLight.setNarrowBeam(sl.narrowBeam());
            scene.lights.add(spotLight);
         }
      }

      return scene;
   }

   /**
    * Applies optional material values to a geometry.
    *
    * @param geometry geometry to update
    * @param kA optional ambient attenuation factor
    * @param kD optional diffuse attenuation factor
    * @param kS optional specular attenuation factor
    * @param kT optional transparency attenuation factor
    * @param kR optional reflection attenuation factor
    * @param nShininess optional shininess exponent
    */
   private static void applyMaterial(geometries.api.Geometry geometry,
                                    primitives.Double3 kA,
                                    primitives.Double3 kD,
                                    primitives.Double3 kS,
                                    primitives.Double3 kT,
                                    primitives.Double3 kR,
                                    Integer nShininess) {
      if (kA == null && kD == null && kS == null && kT == null && kR == null && nShininess == null) return;

      Material material = new Material();
      if (kA != null) material.setKA(kA);
      if (kD != null) material.setKD(kD);
      if (kS != null) material.setKS(kS);
      if (kT != null) material.setKT(kT);
      if (kR != null) material.setKR(kR);
      if (nShininess != null) material.setShininess(nShininess);
      geometry.setMaterial(material);
   }
}

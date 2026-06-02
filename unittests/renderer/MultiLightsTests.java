package renderer;

import static java.awt.Color.BLUE;

import org.junit.jupiter.api.Test;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;

/**
 * Additional mandatory tests: combine all external light sources in a single image.
 * <p>
 * Data for geometries is copied from {@link LightsTests} (do not change it).
 * </p>
 */
class MultiLightsTests {
   /** Constant for tests resolution */
   private static final int RESOLUTION = 500;

   /** Default constructor to satisfy JavaDoc generator */
   MultiLightsTests() { /* to satisfy JavaDoc generator */ }

   /** Shininess value for most of the geometries in the tests */
   private static final int      SHININESS                 = 301;
   /** Diffusion attenuation factor for some of the geometries in the tests */
   private static final double   KD                        = 0.5;
   /** Specular attenuation factor for some of the geometries in the tests */
   private static final double   KS                        = 0.5;

   /** The triangles' vertices for the tests with triangles (copied from LightsTests) */
   private static final Point[]  VERTICES                  =
      {
        new Point(-110, -110, -150),
        new Point(95, 100, -150),
        new Point(110, -110, -150),
        new Point(-75, 78, 100)
      };

   /** Center of the sphere (copied from LightsTests) */
   private static final Point    SPHERE_CENTER             = new Point(0, 0, -50);
   /** Radius of the sphere (copied from LightsTests) */
   private static final double   SPHERE_RADIUS             = 50D;

   /** Color of the sphere (copied from LightsTests) */
   private static final Color    SPHERE_COLOR              = new Color(BLUE).reduce(2);

   private static Geometry newSphere() {
      return new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
         .setEmission(SPHERE_COLOR)
         .setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS));
   }

   private static Geometry newTriangle1(Material material) {
      return new Triangle(VERTICES[0], VERTICES[1], VERTICES[2]).setMaterial(material);
   }

   private static Geometry newTriangle2(Material material) {
      return new Triangle(VERTICES[0], VERTICES[1], VERTICES[3]).setMaterial(material);
   }

   @Test
   void testSphereAllLights() {
      Scene scene = new Scene("Sphere - all lights")
         .setAmbientLight(new AmbientLight(new Color(30, 30, 30)));

      scene.geometries.add(newSphere());

      // Use 3 different light types with different colors/directions/positions
      scene.lights.add(new DirectionalLight(new Color(300, 200, 100), new Vector(1, -1, -1)));
      scene.lights.add(new PointLight(new Color(500, 200, 200), new Point(-50, -50, 25))
         .setKl(0.001).setKq(0.0002));
      scene.lights.add(new SpotLight(new Color(200, 500, 200), new Point(50, 50, 50), new Vector(-1, -1, -1))
         .setKl(0.001).setKq(0.0001));

      Camera.getBuilder() //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpSize(150, 150).setVpDistance(1000) //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage("lightSphereAll");
   }

   @Test
   void testTrianglesAllLights() {
      Scene scene = new Scene("Triangles - all lights")
         .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

      // Do not reuse shared static geometries when changing material
      Material material = new Material()
         .setKD(new Double3(0.2, 0.6, 0.4))
         .setKS(new Double3(0.2, 0.4, 0.3))
         .setShininess(SHININESS);

      scene.geometries.add(newTriangle1(material), newTriangle2(material));

      scene.lights.add(new DirectionalLight(new Color(250, 300, 400), new Vector(-2, -2, -2)));
      scene.lights.add(new PointLight(new Color(400, 250, 150), new Point(30, 10, -100))
         .setKl(0.001).setKq(0.0002));
      scene.lights.add(new SpotLight(new Color(150, 400, 250), new Point(-40, -20, 50), new Vector(1, 1, -2))
         .setKl(0.001).setKq(0.0001));

      Camera.getBuilder() //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpSize(200, 200).setVpDistance(1000) //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage("lightTrianglesAll");
   }
}

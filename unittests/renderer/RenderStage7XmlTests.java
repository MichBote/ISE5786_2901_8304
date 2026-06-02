package renderer;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.io.SceneLoader;

/**
 * Stage 7 render tests using XML scene loading (bonus from stage 5).
 * Creates the same images as LightsTests and MultiLightsTests but loads scene data from XML.
 */
@SuppressWarnings("java:S109")
class RenderStage7XmlTests {
   /** Default constructor to satisfy JavaDoc generator */
   RenderStage7XmlTests() { /* to satisfy JavaDoc generator */ }

   private static final int RESOLUTION = 500;

   private static void renderSphere(Scene scene, String fileName) {
      Camera.getBuilder() //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpSize(150, 150).setVpDistance(1000) //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage(fileName);
   }

   private static void renderTriangles(Scene scene, String fileName) {
      Camera.getBuilder() //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpSize(200, 200).setVpDistance(1000) //
         .setResolution(RESOLUTION, RESOLUTION) //
         .build() //
         .renderImage() //
         .writeToImage(fileName);
   }

   @Test
   void testSphereDirectionalFromXml() {
      renderSphere(SceneLoader.loadFromXml("stage7_sphere_directional"), "lightSphereDirectional_xml");
   }

   @Test
   void testSpherePointFromXml() {
      renderSphere(SceneLoader.loadFromXml("stage7_sphere_point"), "lightSpherePoint_xml");
   }

   @Test
   void testSphereSpotFromXml() {
      renderSphere(SceneLoader.loadFromXml("stage7_sphere_spot"), "lightSphereSpot_xml");
   }

   @Test
   void testSphereSpotSharpFromXml() {
      renderSphere(SceneLoader.loadFromXml("stage7_sphere_spot_sharp"), "lightSphereSpotSharp_xml");
   }

   @Test
   void testTrianglesDirectionalFromXml() {
      renderTriangles(SceneLoader.loadFromXml("stage7_triangles_directional"), "lightTrianglesDirectional_xml");
   }

   @Test
   void testTrianglesPointFromXml() {
      renderTriangles(SceneLoader.loadFromXml("stage7_triangles_point"), "lightTrianglesPoint_xml");
   }

   @Test
   void testTrianglesSpotFromXml() {
      renderTriangles(SceneLoader.loadFromXml("stage7_triangles_spot"), "lightTrianglesSpot_xml");
   }

   @Test
   void testTrianglesSpotSharpFromXml() {
      renderTriangles(SceneLoader.loadFromXml("stage7_triangles_spot_sharp"), "lightTrianglesSpotSharp_xml");
   }

   @Test
   void testSphereAllLightsFromXml() {
      renderSphere(SceneLoader.loadFromXml("stage7_sphere_all"), "lightSphereAll_xml");
   }

   @Test
   void testTrianglesAllLightsFromXml() {
      renderTriangles(SceneLoader.loadFromXml("stage7_triangles_all"), "lightTrianglesAll_xml");
   }
}

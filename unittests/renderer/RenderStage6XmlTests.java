package renderer;

import static java.awt.Color.WHITE;

import org.junit.jupiter.api.Test;

import primitives.Color;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.io.SceneLoader;

/**
 * Stage 6 render tests using XML scene loading (bonus from stage 5).
 */
@SuppressWarnings("java:S109")
class RenderStage6XmlTests {
   /** Default constructor to satisfy JavaDoc generator */
   RenderStage6XmlTests() { /* to satisfy JavaDoc generator */ }

   /** Resolution (both X and Y) */
   private static final int    RESOLUTION = 1001;
   /** View plane size (both height and width) */
   private static final double SIZE       = 500D;
   /** Distance from camera to view plane */
   private static final double DISTANCE   = 100D;
   /** Grid interval (pixels) */
   private static final int    INTERVAL   = 100;

   /**
    * Build camera and render image with grid.
    *
    * @param scene the scene to be used for the image
    * @param fileName the name of the image file
    */
   private static void createImage(Scene scene, String fileName) {
      Camera.getBuilder() //
         .setResolution(RESOLUTION, RESOLUTION) //
         .setLocation(Point.ZERO).setDirection(new Point(0, 0, -1), Vector.AXIS_Y) //
         .setVpDistance(DISTANCE).setVpSize(SIZE, SIZE) //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage() //
         .printGrid(INTERVAL, new Color(WHITE)) //
         .writeToImage(fileName);
   }

   /** Renders an XML scene with geometry emission. */
   @Test
   void testRenderEmissionColorFromXml() {
      Scene scene = SceneLoader.loadFromXml("stage6Emission");
      createImage(scene, "emission render test xml");
   }

   /** Renders an XML scene with ambient light. */
   @Test
   void testRenderAmbientColorFromXml() {
      Scene scene = SceneLoader.loadFromXml("stage6Ambient");
      createImage(scene, "ambient render test xml");
   }
}

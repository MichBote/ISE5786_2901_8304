package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lighting.DirectionalLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

@SuppressWarnings("java:S109")
class DirectionalLightTests {
   /** Default constructor to satisfy JavaDoc generator */
   DirectionalLightTests() { /* to satisfy JavaDoc generator */ }

   @Test
   void testGetL() {
      DirectionalLight light = new DirectionalLight(new Color(10, 20, 30), new Vector(1, -1, -1));
      assertEquals(new Vector(1, -1, -1).normalize(), light.getL(new Point(1, 2, 3)));
   }

   @Test
   void testGetIntensity() {
      Color intensity = new Color(10, 20, 30);
      DirectionalLight light = new DirectionalLight(intensity, new Vector(0, 0, -1));
      assertEquals(intensity, light.getIntensity(new Point(1, 2, 3)));
   }
}

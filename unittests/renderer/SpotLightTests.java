package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lighting.SpotLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/** Unit tests for {@link SpotLight}. */
@SuppressWarnings("java:S109")
class SpotLightTests {
   /** Default constructor to satisfy JavaDoc generator */
   SpotLightTests() { /* to satisfy JavaDoc generator */ }

   /** Verifies intensity directly in front of the spotlight. */
   @Test
   void testGetIntensityPointInFrontOfSpot() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(intensity, light.getIntensity(new Point(0, 0, -10)));
   }

   /** Verifies that points behind the spotlight receive no light. */
   @Test
   void testGetIntensityPointBehindSpot() {
      SpotLight light = new SpotLight(new Color(100, 100, 100), new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(Color.BLACK, light.getIntensity(new Point(0, 0, 10)));
   }

   /** Verifies that perpendicular points receive no light. */
   @Test
   void testGetIntensityPointAtNinetyDegreesToDirection() {
      SpotLight light = new SpotLight(new Color(100, 100, 100), new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(Color.BLACK, light.getIntensity(new Point(10, 0, 0)));
   }
}

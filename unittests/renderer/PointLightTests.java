package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import lighting.PointLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

@SuppressWarnings("java:S109")
class PointLightTests {
   /** Default constructor to satisfy JavaDoc generator */
   PointLightTests() { /* to satisfy JavaDoc generator */ }

   @Test
   void testGetL() {
      PointLight light = new PointLight(new Color(1, 1, 1), new Point(0, 0, 0));
      assertEquals(new Vector(1, 0, 0), light.getL(new Point(1, 0, 0)));
   }

   @Test
   void testGetLPointCoincidesWithLightPosition() {
      PointLight light = new PointLight(new Color(1, 1, 1), new Point(0, 0, 0));
      assertThrows(IllegalArgumentException.class, () -> light.getL(new Point(0, 0, 0)));
   }

   @Test
   void testGetIntensityNoAttenuationByDefault() {
      Color intensity = new Color(100, 100, 100);
      PointLight light = new PointLight(intensity, new Point(0, 0, 0));
      assertEquals(intensity, light.getIntensity(new Point(10, 0, 0)));
   }

   @Test
   void testGetIntensityWithDistanceAttenuation() {
      Color intensity = new Color(100, 100, 100);
      PointLight light = new PointLight(intensity, new Point(0, 0, 0))
         .setKC(1)
         .setKL(0)
         .setKQ(0.01);

      // d=10 => denominator = 1 + 0 + 0.01*100 = 2 => scale by 0.5
      assertEquals(intensity.scale(0.5), light.getIntensity(new Point(10, 0, 0)));
   }
}

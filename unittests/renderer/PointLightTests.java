package renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import lighting.PointLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link PointLight}.
 * <p>
 * Test casing:
 * TC01 verifies the regular direction vector from the light to an illuminated point.
 * TC02 verifies the boundary case where direction is undefined at the light position.
 * TC03 verifies the equivalence partition with default attenuation.
 * TC04-TC06 verify distance attenuation, original intensity at the source, and legacy setters.
 * </p>
 */
@SuppressWarnings("java:S109")
class PointLightTests {
   /** Default constructor to satisfy JavaDoc generator */
   PointLightTests() { /* to satisfy JavaDoc generator */ }

   /** TC01: Verifies the normalized direction from the source to a point. */
   @Test
   void testGetL() {
      PointLight light = new PointLight(new Color(1, 1, 1), new Point(0, 0, 0));
      assertEquals(new Vector(1, 0, 0), light.getL(new Point(1, 0, 0)));
   }

   /** TC02: Verifies that a coincident point cannot produce a direction vector. */
   @Test
   void testGetLPointCoincidesWithLightPosition() {
      PointLight light = new PointLight(new Color(1, 1, 1), new Point(0, 0, 0));
      assertThrows(IllegalArgumentException.class, () -> light.getL(new Point(0, 0, 0)));
   }

   /** TC03: Verifies the default attenuation factors. */
   @Test
   void testGetIntensityNoAttenuationByDefault() {
      Color intensity = new Color(100, 100, 100);
      PointLight light = new PointLight(intensity, new Point(0, 0, 0));
      assertEquals(intensity, light.getIntensity(new Point(10, 0, 0)));
   }

   /** TC04: Verifies distance-based attenuation. */
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

   /** TC05: Verifies getIntensity returns original intensity at the light position. */
   @Test
   void testGetIntensityPointCoincidesWithLightPosition() {
      Color intensity = new Color(100, 100, 100);
      PointLight light = new PointLight(intensity, new Point(0, 0, 0)).setKC(2);

      assertDoesNotThrow(() -> light.getIntensity(new Point(0, 0, 0)));
      assertEquals(intensity, light.getIntensity(new Point(0, 0, 0)));
   }

   /** TC06: Verifies combined attenuation and legacy setter aliases. */
   @Test
   void testGetIntensityWithCombinedAttenuationAliases() {
      Color intensity = new Color(120, 90, 60);
      PointLight light = new PointLight(intensity, new Point(0, 0, 0))
         .setKc(1)
         .setKl(0.1)
         .setKq(0.01);

      // d=10 => denominator = 1 + 0.1*10 + 0.01*100 = 3
      assertEquals(intensity.scale(1d / 3d), light.getIntensity(new Point(10, 0, 0)));
   }
}

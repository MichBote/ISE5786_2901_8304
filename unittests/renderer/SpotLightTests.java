package renderer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lighting.SpotLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for {@link SpotLight}.
 * <p>
 * Test casing:
 * TC01 verifies full intensity on the spotlight axis.
 * TC02-TC03 verify the outside-cone equivalence partitions.
 * TC04 verifies angular attenuation inside the cone.
 * TC05 verifies narrow-beam exponent behavior.
 * TC06 verifies distance attenuation inherited from {@link lighting.PointLight}.
 * TC07 verifies the coincident-point boundary case returns original intensity.
 * </p>
 */
@SuppressWarnings("java:S109")
class SpotLightTests {
   /** Default constructor to satisfy JavaDoc generator */
   SpotLightTests() { /* to satisfy JavaDoc generator */ }

   /** TC01: Verifies intensity directly in front of the spotlight. */
   @Test
   void testGetIntensityPointInFrontOfSpot() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(intensity, light.getIntensity(new Point(0, 0, -10)));
   }

   /** TC02: Verifies that points behind the spotlight receive no light. */
   @Test
   void testGetIntensityPointBehindSpot() {
      SpotLight light = new SpotLight(new Color(100, 100, 100), new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(Color.BLACK, light.getIntensity(new Point(0, 0, 10)));
   }

   /** TC03: Verifies that perpendicular points receive no light. */
   @Test
   void testGetIntensityPointAtNinetyDegreesToDirection() {
      SpotLight light = new SpotLight(new Color(100, 100, 100), new Point(0, 0, 0), new Vector(0, 0, -1));
      assertEquals(Color.BLACK, light.getIntensity(new Point(10, 0, 0)));
   }

   /** TC04: Verifies angular attenuation for a point inside the spotlight cone. */
   @Test
   void testGetIntensityWithAngularAttenuation() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1));

      assertEquals(intensity.scale(1d / Math.sqrt(2d)), light.getIntensity(new Point(0, 10, -10)));
   }

   /** TC05: Verifies narrow beam exponent concentrates the spotlight. */
   @Test
   void testGetIntensityWithNarrowBeam() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1))
         .setNarrowBeam(2);

      assertEquals(intensity.scale(0.5), light.getIntensity(new Point(0, 10, -10)));
   }

   /** TC06: Verifies spotlight distance attenuation inherited from point light. */
   @Test
   void testGetIntensityWithDistanceAttenuation() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1))
         .setKC(1)
         .setKL(0)
         .setKQ(0.01);

      assertEquals(intensity.scale(0.5), light.getIntensity(new Point(0, 0, -10)));
   }

   /** TC07: Verifies coincident point returns original intensity. */
   @Test
   void testGetIntensityPointCoincidesWithLightPosition() {
      Color intensity = new Color(100, 100, 100);
      SpotLight light = new SpotLight(intensity, new Point(0, 0, 0), new Vector(0, 0, -1));

      assertDoesNotThrow(() -> light.getIntensity(new Point(0, 0, 0)));
      assertEquals(intensity, light.getIntensity(new Point(0, 0, 0)));
   }
}

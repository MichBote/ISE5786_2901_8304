package lighting;

import primitives.Color;

/**
 * Base class for all light types.
 * <p>
 * Stores only the original light intensity (I0).
 * </p>
 */
abstract class Light {
   /** Original light intensity (I0). */
   protected final Color _intensity;

   /**
    * Constructs a light with the given original intensity.
    *
    * @param intensity original light intensity
    */
   protected Light(Color intensity) {
      _intensity = intensity;
   }

   /**
    * Returns the original light intensity (I0).
    *
    * @return original light intensity
    */
   public Color getIntensity() {
      return _intensity;
   }
}

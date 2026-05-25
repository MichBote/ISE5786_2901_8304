package lighting;

import primitives.Color;

/**
 * Represents ambient light in the scene.
 * <p>
 * Ambient light is uniform and directionless and affects all objects equally.
 * At this stage it is represented only by its intensity color.
 * </p>
 */
public class AmbientLight {
   /** Constant representing no ambient light (black). */
   public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

   /** Ambient light intensity. */
   private final Color _intensity;

   /**
    * Constructs an ambient light with the given intensity.
    *
    * @param intensity ambient light intensity
    */
   public AmbientLight(Color intensity) {
      _intensity = intensity;
   }

   /**
    * Returns the ambient light intensity.
    *
    * @return ambient light intensity
    */
   public Color getIntensity() {
      return _intensity;
   }
}

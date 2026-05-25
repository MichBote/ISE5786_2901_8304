package primitives;

/**
 * Surface material coefficients used by the renderer.
 * <p>
 * Stage 6 uses only the ambient-light attenuation coefficient {@link #kA}.
 * Additional coefficients can be added in later stages.
 * </p>
 */
public class Material {
   /** Ambient-light attenuation coefficient. */
   public Double3 kA = Double3.ONE;

   /**
    * Constructs material coefficients with default values.
    */
   public Material() {
   }

   /**
    * Sets a uniform ambient-light attenuation coefficient.
    *
    * @param kA ambient attenuation coefficient
    * @return this material, for chaining
    */
   public Material setKA(double kA) {
      this.kA = new Double3(kA);
      return this;
   }

   /**
    * Sets a per-channel ambient-light attenuation coefficient.
    *
    * @param kA ambient attenuation coefficient
    * @return this material, for chaining
    */
   public Material setKA(Double3 kA) {
      this.kA = kA;
      return this;
   }
}

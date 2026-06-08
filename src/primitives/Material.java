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

   /** Diffuse reflection coefficient (Phong). */
   public Double3 kD = Double3.ZERO;

   /** Specular reflection coefficient (Phong). */
   public Double3 kS = Double3.ZERO;

   /** Shininess factor (Phong). */
   public int nShininess = 0;

   /** Transparency attenuation coefficient. */
   public Double3 kT = Double3.ZERO;

   /** Reflection attenuation coefficient. */
   public Double3 kR = Double3.ZERO;

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

   /**
    * Sets a uniform diffuse reflection coefficient.
    *
    * @param kD diffuse coefficient
    * @return this material, for chaining
    */
   public Material setKD(double kD) {
      this.kD = new Double3(kD);
      return this;
   }

   /**
    * Sets a per-channel diffuse reflection coefficient.
    *
    * @param kD diffuse coefficient
    * @return this material, for chaining
    */
   public Material setKD(Double3 kD) {
      this.kD = kD;
      return this;
   }

   /**
    * Sets a uniform specular reflection coefficient.
    *
    * @param kS specular coefficient
    * @return this material, for chaining
    */
   public Material setKS(double kS) {
      this.kS = new Double3(kS);
      return this;
   }

   /**
    * Sets a per-channel specular reflection coefficient.
    *
    * @param kS specular coefficient
    * @return this material, for chaining
    */
   public Material setKS(Double3 kS) {
      this.kS = kS;
      return this;
   }

   /**
    * Sets the Phong shininess factor.
    *
    * @param nShininess shininess exponent
    * @return this material, for chaining
    */
   public Material setShininess(int nShininess) {
      this.nShininess = nShininess;
      return this;
   }

   /**
    * Sets a uniform transparency coefficient.
    *
    * @param kT transparency coefficient
    * @return this material, for chaining
    */
   public Material setKT(double kT) {
      this.kT = new Double3(kT);
      return this;
   }

   /**
    * Sets a per-channel transparency coefficient.
    *
    * @param kT transparency coefficient
    * @return this material, for chaining
    */
   public Material setKT(Double3 kT) {
      this.kT = kT;
      return this;
   }

   /**
    * Sets a uniform reflection coefficient.
    *
    * @param kR reflection coefficient
    * @return this material, for chaining
    */
   public Material setKR(double kR) {
      this.kR = new Double3(kR);
      return this;
   }

   /**
    * Sets a per-channel reflection coefficient.
    *
    * @param kR reflection coefficient
    * @return this material, for chaining
    */
   public Material setKR(Double3 kR) {
      this.kR = kR;
      return this;
   }
}

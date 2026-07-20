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
    * Glossy reflection blur size on the ray tracer target plane.
    * A value of {@code 0} keeps a perfect reflected ray.
    */
   public double glossyBlur = 0d;

   /**
    * Blurry transparency/refraction blur size on the ray tracer target plane.
    * A value of {@code 0} keeps a perfect transmitted/refracted ray.
    */
   public double transparencyBlur = 0d;

   /** Index of refraction used for transparent materials. */
   public double refractiveIndex = 1.5d;

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

   /**
    * Sets the glossy reflection blur size.
    *
    * @param blur reflection blur size, must be non-negative
    * @return this material, for chaining
    */
   public Material setGlossyBlur(double blur) {
      validateBlur(blur, "Glossy blur");
      glossyBlur = blur;
      return this;
   }

   /**
    * Sets the blurry transparency/refraction blur size.
    *
    * @param blur transparency blur size, must be non-negative
    * @return this material, for chaining
    */
   public Material setTransparencyBlur(double blur) {
      validateBlur(blur, "Transparency blur");
      transparencyBlur = blur;
      return this;
   }

   /**
    * Compatibility alias for {@link #setTransparencyBlur(double)}.
    *
    * @param blur transparency blur size, must be non-negative
    * @return this material, for chaining
    */
   public Material setDiffuseBlur(double blur) {
      return setTransparencyBlur(blur);
   }

   /**
    * Sets the material index of refraction.
    *
    * @param refractiveIndex optical index of refraction, must be positive
    * @return this material, for chaining
    */
   public Material setRefractiveIndex(double refractiveIndex) {
      if (refractiveIndex <= 0) throw new IllegalArgumentException("Refractive index must be positive");
      this.refractiveIndex = refractiveIndex;
      return this;
   }

   /**
    * Validates a material blur size.
    *
    * @param blur blur size
    * @param name value name used in error messages
    */
   private static void validateBlur(double blur, String name) {
      if (!Double.isFinite(blur) || blur < 0) {
         throw new IllegalArgumentException(name + " must be non-negative");
      }
   }
}

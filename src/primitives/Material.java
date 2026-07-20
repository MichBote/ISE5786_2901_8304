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

   /** Diffuse-glass blur radius on the target area plane. */
   public double diffuseGlassRadius = 0;

   /** Distance of the diffuse-glass target area from the hit point. */
   public double diffuseGlassDistance = 100;

   /** Number of rays in diffuse-glass beam (1 means disabled). */
   public int diffuseGlassRays = 1;

   /** Glossy reflection blur radius on the target area plane. */
   public double glossyRadius = 0;

   /** Distance of glossy target area from the hit point. */
   public double glossyDistance = 100;

   /** Number of rays in glossy reflection beam (1 means disabled). */
   public int glossyRays = 1;

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
    * Enables diffuse-glass blur using an angular spread and number of rays.
    *
    * @param blurAngleDegrees maximal blur angle in degrees
    * @param rays             number of rays in the beam (must be at least 1)
    * @return this material, for chaining
    */
   public Material setDiffuseGlass(double blurAngleDegrees, int rays) {
      if (blurAngleDegrees < 0) {
         throw new IllegalArgumentException("blurAngleDegrees must be non-negative");
      }
      if (rays < 1) {
         throw new IllegalArgumentException("rays must be at least 1");
      }
      diffuseGlassRays = rays;
      if (blurAngleDegrees == 0 || rays == 1) {
         diffuseGlassRadius = 0;
         return this;
      }

      double angleRadians = Math.toRadians(blurAngleDegrees);
      diffuseGlassRadius = Math.tan(angleRadians) * diffuseGlassDistance;
      return this;
   }

   /**
    * Enables diffuse-glass blur with explicit radius/distance/rays.
    *
    * @param radius   target area radius
    * @param distance target area distance from the hit point
    * @param rays     number of rays in beam (must be at least 1)
    * @return this material, for chaining
    */
   public Material setDiffuseGlass(double radius, double distance, int rays) {
      if (radius < 0) {
         throw new IllegalArgumentException("radius must be non-negative");
      }
      if (distance <= 0) {
         throw new IllegalArgumentException("distance must be positive");
      }
      if (rays < 1) {
         throw new IllegalArgumentException("rays must be at least 1");
      }

      diffuseGlassRadius = radius;
      diffuseGlassDistance = distance;
      diffuseGlassRays = rays;
      return this;
   }

   /**
    * Enables glossy reflection blur using an angular spread and number of rays.
    *
    * @param blurAngleDegrees maximal blur angle in degrees
    * @param rays number of rays in reflection beam
    * @return this material, for chaining
    */
   public Material setGlossy(double blurAngleDegrees, int rays) {
      if (blurAngleDegrees < 0) {
         throw new IllegalArgumentException("blurAngleDegrees must be non-negative");
      }
      if (rays < 1) {
         throw new IllegalArgumentException("rays must be at least 1");
      }
      glossyRays = rays;
      if (blurAngleDegrees == 0 || rays == 1) {
         glossyRadius = 0;
         return this;
      }

      double angleRadians = Math.toRadians(blurAngleDegrees);
      glossyRadius = Math.tan(angleRadians) * glossyDistance;
      return this;
   }

   /**
    * Enables glossy reflection blur with explicit radius/distance/rays.
    *
    * @param radius target area radius
    * @param distance target area distance
    * @param rays number of rays
    * @return this material, for chaining
    */
   public Material setGlossy(double radius, double distance, int rays) {
      if (radius < 0) {
         throw new IllegalArgumentException("radius must be non-negative");
      }
      if (distance <= 0) {
         throw new IllegalArgumentException("distance must be positive");
      }
      if (rays < 1) {
         throw new IllegalArgumentException("rays must be at least 1");
      }

      glossyRadius = radius;
      glossyDistance = distance;
      glossyRays = rays;
      return this;
   }
}

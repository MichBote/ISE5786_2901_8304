package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Spot light source (point light with a direction).
 */
public class SpotLight extends PointLight {
   /** Spotlight direction (normalized). */
   private final Vector _direction;

   /** Narrow-beam exponent (1 = regular spotlight). */
   private int _narrowBeam = 1;

   /**
    * Constructs a spot light.
    *
    * @param intensity original light intensity
    * @param position light position
    * @param direction spotlight direction (will be normalized)
    */
   public SpotLight(Color intensity, Point position, Vector direction) {
      super(intensity, position);
      _direction = direction.normalize();
   }

   /**
    * Sets the constant attenuation factor.
    *
    * @param kC constant attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKC(double kC) {
      super.setKC(kC);
      return this;
   }

   /**
    * Sets the constant attenuation factor using the legacy method name.
    *
    * @param kC constant attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKc(double kC) {
      super.setKc(kC);
      return this;
   }

   /**
    * Sets the linear attenuation factor.
    *
    * @param kL linear attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKL(double kL) {
      super.setKL(kL);
      return this;
   }

   /**
    * Sets the linear attenuation factor using the legacy method name.
    *
    * @param kL linear attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKl(double kL) {
      super.setKl(kL);
      return this;
   }

   /**
    * Sets the quadratic attenuation factor.
    *
    * @param kQ quadratic attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKQ(double kQ) {
      super.setKQ(kQ);
      return this;
   }

   /**
    * Sets the quadratic attenuation factor using the legacy method name.
    *
    * @param kQ quadratic attenuation factor
    * @return this spotlight, for chaining
    */
   @Override
   public SpotLight setKq(double kQ) {
      super.setKq(kQ);
      return this;
   }

   /**
    * Sets a narrow-beam exponent to concentrate the spotlight into a tighter cone.
    *
    * @param narrowBeam exponent (values {@code <= 1} behave like a regular spotlight)
    * @return this spotlight, for chaining
    */
   public SpotLight setNarrowBeam(int narrowBeam) {
      _narrowBeam = narrowBeam;
      return this;
   }

   /**
    * Returns the spotlight intensity reaching the given point.
    *
    * @param p illuminated point
    * @return attenuated spotlight intensity at {@code p}
    */
   @Override
   public Color getIntensity(Point p) {
      Vector l = getL(p);
      double factor = Math.max(0d, _direction.dotProduct(l));
      if (_narrowBeam > 1) {
         factor = Math.pow(factor, _narrowBeam);
      }
      return super.getIntensity(p).scale(factor);
   }
}

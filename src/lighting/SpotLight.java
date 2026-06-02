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

   @Override
   public SpotLight setKC(double kC) {
      super.setKC(kC);
      return this;
   }

   @Override
   public SpotLight setKc(double kC) {
      super.setKc(kC);
      return this;
   }

   @Override
   public SpotLight setKL(double kL) {
      super.setKL(kL);
      return this;
   }

   @Override
   public SpotLight setKl(double kL) {
      super.setKl(kL);
      return this;
   }

   @Override
   public SpotLight setKQ(double kQ) {
      super.setKQ(kQ);
      return this;
   }

   @Override
   public SpotLight setKq(double kQ) {
      super.setKq(kQ);
      return this;
   }

   /**
    * Sets a narrow-beam exponent to concentrate the spotlight into a tighter cone.
    *
    * @param narrowBeam exponent (values <= 1 behave like a regular spotlight)
    * @return this spotlight, for chaining
    */
   public SpotLight setNarrowBeam(int narrowBeam) {
      _narrowBeam = narrowBeam;
      return this;
   }

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

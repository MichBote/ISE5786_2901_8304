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
   public SpotLight setKL(double kL) {
      super.setKL(kL);
      return this;
   }

   @Override
   public SpotLight setKQ(double kQ) {
      super.setKQ(kQ);
      return this;
   }

   @Override
   public Color getIntensity(Point p) {
      Vector l = getL(p);
      double factor = Math.max(0d, _direction.dotProduct(l));
      return super.getIntensity(p).scale(factor);
   }
}

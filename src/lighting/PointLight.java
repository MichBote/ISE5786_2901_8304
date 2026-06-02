package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Point light source with distance attenuation.
 */
public class PointLight extends Light implements LightSource {
   /** Light position. */
   private final Point _position;

   /** Attenuation factors. */
   private double _kC = 1d;
   private double _kL = 0d;
   private double _kQ = 0d;

   /**
    * Constructs a point light.
    *
    * @param intensity original light intensity
    * @param position light position
    */
   public PointLight(Color intensity, Point position) {
      super(intensity);
      _position = position;
   }

   /** Chained setter for kC. */
   public PointLight setKC(double kC) {
      _kC = kC;
      return this;
   }

   /** Chained setter for kC (legacy naming used by some provided tests). */
   public PointLight setKc(double kC) {
      return setKC(kC);
   }

   /** Chained setter for kL. */
   public PointLight setKL(double kL) {
      _kL = kL;
      return this;
   }

   /** Chained setter for kL (legacy naming used by some provided tests). */
   public PointLight setKl(double kL) {
      return setKL(kL);
   }

   /** Chained setter for kQ. */
   public PointLight setKQ(double kQ) {
      _kQ = kQ;
      return this;
   }

   /** Chained setter for kQ (legacy naming used by some provided tests). */
   public PointLight setKq(double kQ) {
      return setKQ(kQ);
   }

   @Override
   public Vector getL(Point p) {
      return p.subtract(_position).normalize();
   }

   @Override
   public Color getIntensity(Point p) {
      double d = p.distance(_position);
      double d2 = d * d;
      double denominator = alignZero(_kC + _kL * d + _kQ * d2);
      return getIntensity().scale(1d / denominator);
   }
}

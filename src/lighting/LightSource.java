package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.List;

/**
 * A light source that can compute light propagation to a point in space.
 */
public interface LightSource {
   /**
    * Returns a normalized vector representing the light direction from the light
    * source to the illuminated point.
    *
    * @param p illuminated point
    * @return normalized direction from the light source to {@code p}
    */
   Vector getL(Point p);

   /**
    * Returns the light intensity reaching the given point.
    *
    * @param p illuminated point
    * @return intensity at {@code p}
    */
   Color getIntensity(Point p);

   /**
    * Returns the distance from this light source to the given point.
    *
    * @param p illuminated point
    * @return distance to {@code p}; infinity for directional light
    */
   double getDistance(Point p);

   /**
    * Returns sampled light directions from area-light points to the illuminated point.
    * Default implementation returns a single direction (no soft shadows).
    *
    * @param p illuminated point
    * @return list of sampled light directions
    */
   default List<Vector> getLs(Point p) {
      return List.of(getL(p));
   }
}

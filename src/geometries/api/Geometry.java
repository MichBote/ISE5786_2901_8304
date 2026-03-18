package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Base abstract class for all geometric shapes.
 * <p>
 * At this stage the class exposes only a normal computation API.
 * Intersection-related APIs will be added in later stages.
 * </p>
 * @author Michal Berdugo & Bina Cohen
 */
public abstract class Geometry {

   /**
    * Returns the normal vector to the geometry at the given point.
    * @param  point a point on the geometry
    * @return       the geometry normal at {@code point}
    */
   public abstract Vector getNormal(Point point);
}

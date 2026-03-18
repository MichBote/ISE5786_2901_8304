package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a plane in 3D space.
 * <p>
 * A plane is represented by a reference point and a constant normal vector.
 * The normal is stored normalized.
 * </p>
 * @author Michal Berdugo & Bina Cohen
 */
public final class Plane extends Geometry {
   /** A reference point on the plane */
   private final Point  _point;
   /** The plane normal (normalized) */
   private final Vector _normal;

   /**
    * Constructs a plane from three points.
    * @param p1 first point
    * @param p2 second point
    * @param p3 third point
    * @throws IllegalArgumentException if the points are not suitable to define a plane
    */
   public Plane(Point p1, Point p2, Point p3) {
      _point  = p1;
      Vector u = p2.subtract(p1);
      Vector v = p3.subtract(p1);
      _normal = u.crossProduct(v).normalize();
   }

   /**
    * Constructs a plane from a point and a normal vector.
    * The normal is stored normalized.
    * @param point  a point on the plane
    * @param normal plane normal
    */
   public Plane(Point point, Vector normal) {
      _point  = point;
      _normal = normal.normalize();
   }

   @Override
   public Vector getNormal(Point point) { return _normal; }

   @Override
   public String toString() { return "Plane(point=" + _point + ", normal=" + _normal + ")"; }
}

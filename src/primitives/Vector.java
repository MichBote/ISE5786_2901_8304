package primitives;

import static primitives.Util.isZero;

/**
 * Represents a 3D vector in a Cartesian coordinate system.
 * <p>
 * A vector has direction and magnitude. It is implemented as an extension of
 * {@link Point} and reuses the protected {@link Point#xyz_} coordinates.
 * The vector is immutable.
 * </p>
 * <p>
 * Zero vectors are not allowed.
 * </p>
 * @author Michal Berdugo & Bina Cohen
 */
public final class Vector extends Point {
   /** Unit vector on X axis */
   public static final Vector AXIS_X = new Vector(1, 0, 0);
   /** Unit vector on Y axis */
   public static final Vector AXIS_Y = new Vector(0, 1, 0);
   /** Unit vector on Z axis */
   public static final Vector AXIS_Z = new Vector(0, 0, 1);

   /**
    * Constructs a vector from its coordinate values.
    * @param x x component
    * @param y y component
    * @param z z component
    * @throws IllegalArgumentException if the vector is a zero vector
    */
   public Vector(double x, double y, double z) {
      super(x, y, z);
      if (isZero(x) && isZero(y) && isZero(z))
         throw new IllegalArgumentException("Zero vector is not allowed");
   }

   /**
    * Constructs a vector from a {@link Double3} container.
    * @param  xyz the vector components
    * @throws IllegalArgumentException if the vector is a zero vector
    */
   public Vector(Double3 xyz) {
      super(xyz);
      if (Double3.ZERO.equals(xyz))
         throw new IllegalArgumentException("Zero vector is not allowed");
   }

   /**
    * Adds another vector to this vector.
    * @param  other another vector
    * @return       the resulting vector
    */
   public Vector add(Vector other) { return new Vector(xyz_.add(other.xyz_)); }

   /**
    * Scales this vector by a scalar.
    * @param  scalar scaling factor
    * @return        the scaled vector
    */
   public Vector scale(double scalar) { return new Vector(xyz_.scale(scalar)); }

   /**
    * Computes the dot (scalar) product of this vector with another vector.
    * @param  other another vector
    * @return       dot product
    */
   public double dotProduct(Vector other) {
      return xyz_._d1() * other.xyz_._d1() + xyz_._d2() * other.xyz_._d2() + xyz_._d3() * other.xyz_._d3();
   }

   /**
    * Computes the cross (vector) product of this vector with another vector.
    * @param  other another vector
    * @return       cross product vector
    */
   public Vector crossProduct(Vector other) {
      double x = xyz_._d2() * other.xyz_._d3() - xyz_._d3() * other.xyz_._d2();
      double y = xyz_._d3() * other.xyz_._d1() - xyz_._d1() * other.xyz_._d3();
      double z = xyz_._d1() * other.xyz_._d2() - xyz_._d2() * other.xyz_._d1();
      return new Vector(x, y, z);
   }

   /**
    * Returns the squared length of the vector.
    * @return squared length
    */
   public double lengthSquared() {
      double x = xyz_._d1();
      double y = xyz_._d2();
      double z = xyz_._d3();
      return x * x + y * y + z * z;
   }

   /**
    * Returns the length (magnitude) of the vector.
    * @return length
    */
   public double length() { return Math.sqrt(lengthSquared()); }

   /**
    * Normalizes this vector.
    * @return a unit vector in the same direction
    */
   public Vector normalize() { return scale(1d / length()); }

   @Override
   public String toString() { return "Vector" + xyz_; }
}

package geometries.impl;

import geometries.api.Geometry;

/**
 * Base abstract class for radial geometries (geometries defined by a radius).
 * <p>
 * The class stores the radius and its square for efficient computations.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public abstract class RadialGeometry extends Geometry {
    /**
     * Geometry radius
     */
    protected final double _radius;
    /**
     * Radius squared (precomputed)
     */
    protected final double _radiusSquared;

    /**
     * Constructs a radial geometry with the given radius.
     *
     * @param radius the radius
     */
    public RadialGeometry(double radius) {
        _radius = radius;
        _radiusSquared = radius * radius;
    }
}

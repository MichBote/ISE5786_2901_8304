package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * Base abstract class for all geometric shapes.
 * <p>
 * At this stage the class exposes only a normal computation API.
 * Intersection-related APIs are introduced via {@link Intersectable}.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public abstract class Geometry extends Intersectable {
    /** Emission color of the geometry. */
    private Color _emission = Color.BLACK;

    /** Material coefficients of the geometry. */
    private Material _material = new Material();

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    public Geometry() { /* Default constructor to satisfy JavaDoc generator */ }

    /**
     * Returns the geometry emission color.
     *
     * @return emission color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the geometry emission color.
     *
     * @param emission emission color
     * @return this geometry, for chaining
     */
    public Geometry setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Returns the geometry material.
     *
     * @return material coefficients
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Sets the geometry material.
     *
     * @param material material coefficients
     * @return this geometry, for chaining
     */
    public Geometry setMaterial(Material material) {
        _material = material;
        return this;
    }

    /**
     * Returns the normal vector to the geometry at the given point.
     *
     * @param point a point on the geometry
     * @return the geometry normal at {@code point}
     */
    public abstract Vector getNormal(Point point);
}



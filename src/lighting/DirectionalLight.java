package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Directional light source (infinite distance).
 * <p>
 * Direction and intensity are constant for all points in the scene.
 * </p>
 */
public final class DirectionalLight extends Light implements LightSource {
    /**
     * Light direction (normalized).
     */
    private final Vector _direction;

    /**
     * Constructs a directional light.
     *
     * @param intensity original light intensity
     * @param direction light direction (will be normalized)
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    @Override
    public Vector getL(Point p) {
        return _direction;
    }

    @Override
    public Color getIntensity(Point p) {
        return _intensity;
    }

    @Override
    public double getDistance(Point p) {
        return Double.POSITIVE_INFINITY;
    }
}

package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.Blackboard;

import java.util.LinkedList;
import java.util.List;

import static primitives.Util.alignZero;

/**
 * Spot light source (point light with a direction).
 */
public class SpotLight extends PointLight {
    /**
     * Spotlight direction (normalized).
     */
    private final Vector _direction;

    /**
     * Narrow-beam exponent (1 = regular spotlight).
     */
    private int _narrowBeam = 1;

    /**
     * Constructs a spot light.
     *
     * @param intensity original light intensity
     * @param position  light position
     * @param direction spotlight direction (will be normalized)
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    /**
     * Sets the constant attenuation factor.
     *
     * @param kC constant attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKC(double kC) {
        super.setKC(kC);
        return this;
    }

    /**
     * Sets the constant attenuation factor using the legacy method name.
     *
     * @param kC constant attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    /**
     * Sets the linear attenuation factor.
     *
     * @param kL linear attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKL(double kL) {
        return (SpotLight) super.setKL(kL);
    }

    /**
     * Sets the linear attenuation factor using the legacy method name.
     *
     * @param kL linear attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    /**
     * Sets the quadratic attenuation factor.
     *
     * @param kQ quadratic attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKQ(double kQ) {
        return (SpotLight) super.setKQ(kQ);
    }

    /**
     * Sets the quadratic attenuation factor using the legacy method name.
     *
     * @param kQ quadratic attenuation factor
     * @return this spotlight, for chaining
     */
    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }

    /**
     * Sets a narrow-beam exponent to concentrate the spotlight into a tighter cone.
     *
     * @param narrowBeam exponent (values {@code <= 1} behave like a regular spotlight)
     * @return this spotlight, for chaining
     */
    public SpotLight setNarrowBeam(int narrowBeam) {
        _narrowBeam = narrowBeam;
        return this;
    }

    /**
     * Sets soft-shadow sampling for this spotlight.
     *
     * @param radius area radius (0 disables)
     * @param rays number of sampled rays (1 disables)
     * @param shape sampling area shape
     * @param pattern sampling pattern
     * @return this spotlight
     */
    @Override
    public SpotLight setSoftShadows(double radius, int rays, Blackboard.Shape shape, Blackboard.Pattern pattern) {
        super.setSoftShadows(radius, rays, shape, pattern);
        return this;
    }

    /**
     * Returns the spotlight intensity reaching the given point.
     *
     * @param p illuminated point
     * @return attenuated spotlight intensity at {@code p}
     */
    @Override
    public Color getIntensity(Point p) {
        Vector l;
        try {
            l = getL(p);
        } catch (IllegalArgumentException exception) {
            // At the light position the direction is undefined; per the light
            // propagation boundary case, intensity remains the original I0.
            return super.getIntensity(p);
        }

        double factor = alignZero(_direction.dotProduct(l));
        if (factor <= 0) return Color.BLACK;
        if (_narrowBeam > 1) {
            factor = Math.pow(factor, _narrowBeam);
        }
        return super.getIntensity(p).scale(factor);
    }

    @Override
    public List<Vector> getLs(Point p) {
        if (_softShadowRadius == 0 || _softShadowRays <= 1) {
            return List.of(getL(p));
        }

        List<Point> points = sampleLightPoints(_direction, p.hashCode());
        var vectors = new LinkedList<Vector>();
        for (Point lightPoint : points) {
            vectors.add(p.subtract(lightPoint).normalize());
        }
        return vectors;
    }
}

package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.Blackboard;

import java.util.LinkedList;
import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Point light source with distance attenuation.
 */
public class PointLight extends Light implements LightSource {
    /**
     * Light position.
     */
    private final Point _position;

    /** Area-light radius for soft shadows (0 disables). */
    protected double _softShadowRadius = 0;

    /** Number of sampled light directions for soft shadows. */
    protected int _softShadowRays = 1;

    /** Sampling shape for soft-shadow area light. */
    protected Blackboard.Shape _softShadowShape = Blackboard.Shape.CIRCLE;

    /** Sampling pattern for soft-shadow area light. */
    protected Blackboard.Pattern _softShadowPattern = Blackboard.Pattern.JITTER;

    /**
     * Constant attenuation factor.
     */
    private double _kC = 1d;
    /**
     * Linear attenuation factor.
     */
    private double _kL = 0d;
    /**
     * Quadratic attenuation factor.
     */
    private double _kQ = 0d;

    /**
     * Constructs a point light.
     *
     * @param intensity original light intensity
     * @param position  light position
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    /**
     * Sets soft-shadow area-light sampling for this light source.
     *
     * @param radius area radius (0 disables)
     * @param rays number of sampled rays (1 disables)
     * @param shape sampling area shape
     * @param pattern sampling pattern
     * @return this point light, for chaining
     */
    public PointLight setSoftShadows(double radius, int rays, Blackboard.Shape shape, Blackboard.Pattern pattern) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative");
        }
        if (rays < 1) {
            throw new IllegalArgumentException("rays must be at least 1");
        }
        _softShadowRadius = radius;
        _softShadowRays = rays;
        _softShadowShape = shape == null ? Blackboard.Shape.CIRCLE : shape;
        _softShadowPattern = pattern == null ? Blackboard.Pattern.JITTER : pattern;
        return this;
    }

    /**
     * Returns light position for subclasses.
     *
     * @return this light's position
     */
    protected Point getPosition() {
        return _position;
    }

    /**
     * Sets the constant attenuation factor.
     *
     * @param kC constant attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKC(double kC) {
        _kC = kC;
        return this;
    }

    /**
     * Sets the constant attenuation factor using the legacy method name.
     *
     * @param kC constant attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKc(double kC) {
        return setKC(kC);
    }

    /**
     * Sets the linear attenuation factor.
     *
     * @param kL linear attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKL(double kL) {
        _kL = kL;
        return this;
    }

    /**
     * Sets the linear attenuation factor using the legacy method name.
     *
     * @param kL linear attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKl(double kL) {
        return setKL(kL);
    }

    /**
     * Sets the quadratic attenuation factor.
     *
     * @param kQ quadratic attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKQ(double kQ) {
        _kQ = kQ;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor using the legacy method name.
     *
     * @param kQ quadratic attenuation factor
     * @return this point light, for chaining
     */
    public PointLight setKq(double kQ) {
        return setKQ(kQ);
    }

    @Override
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        double d2 = p.distanceSquared(_position);
        if (isZero(d2)) return _intensity;

        double d = Math.sqrt(d2);
        double denominator = alignZero(_kC + _kL * d + _kQ * d2);
        return _intensity.scale(1d / denominator);
    }

    @Override
    public double getDistance(Point p) {
        return _position.distance(p);
    }

    @Override
    public List<Vector> getLs(Point p) {
        if (_softShadowRadius == 0 || _softShadowRays <= 1) {
            return List.of(getL(p));
        }

        List<Point> lightPoints = sampleLightPoints(getL(p), p.hashCode());

        var vectors = new LinkedList<Vector>();
        for (Point lightPoint : lightPoints) {
            vectors.add(p.subtract(lightPoint).normalize());
        }
        return vectors;
    }

    /**
     * Samples area-light points on a plane orthogonal to the provided axis.
     *
     * @param axis plane normal
     * @param seed deterministic seed
     * @return sampled points on light area
     */
    protected List<Point> sampleLightPoints(Vector axis, long seed) {
        Vector helper = Math.abs(axis.dotProduct(Vector.AXIS_Y)) < 0.9 ? Vector.AXIS_Y : Vector.AXIS_X;
        Vector x = axis.crossProduct(helper).normalize();
        Vector y = axis.crossProduct(x).normalize();

        Blackboard board = new Blackboard(_softShadowShape, _softShadowPattern, _softShadowRays, seed);
        return board.samplePoints(_position, x, y, _softShadowRadius);
    }
}

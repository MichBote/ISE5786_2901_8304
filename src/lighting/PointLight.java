package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

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
}

package scene.io;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Marker interface for parsed external light source definitions.
 */
sealed interface LightDescriptor permits DirectionalLightDescriptor, PointLightDescriptor, SpotLightDescriptor { }

/**
 * Parsed directional light definition.
 *
 * @param intensity original light intensity
 * @param direction light direction
 */
record DirectionalLightDescriptor(Color intensity, Vector direction) implements LightDescriptor { }

/**
 * Parsed point light definition.
 *
 * @param intensity original light intensity
 * @param position light position
 * @param kC attenuation kC (optional)
 * @param kL attenuation kL (optional)
 * @param kQ attenuation kQ (optional)
 */
record PointLightDescriptor(Color intensity, Point position, Double kC, Double kL, Double kQ) implements LightDescriptor { }

/**
 * Parsed spot light definition.
 *
 * @param intensity original light intensity
 * @param position light position
 * @param direction spot direction
 * @param kC attenuation kC (optional)
 * @param kL attenuation kL (optional)
 * @param kQ attenuation kQ (optional)
 * @param narrowBeam narrow beam exponent (optional)
 */
record SpotLightDescriptor(Color intensity, Point position, Vector direction, Double kC, Double kL, Double kQ, Integer narrowBeam)
   implements LightDescriptor { }

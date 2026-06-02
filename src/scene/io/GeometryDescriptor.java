package scene.io;

import primitives.Color;
import primitives.Double3;
import primitives.Point;

/**
 * Marker interface for parsed geometry definitions.
 */
sealed interface GeometryDescriptor permits SphereDescriptor, TriangleDescriptor { }

/**
 * Parsed sphere definition.
 *
 * @param center sphere center
 * @param radius sphere radius
 */
record SphereDescriptor(Point center, double radius, Color emission, Double3 kA, Double3 kD, Double3 kS, Integer nShininess)
	implements GeometryDescriptor { }

/**
 * Parsed triangle definition.
 *
 * @param p0 first triangle vertex
 * @param p1 second triangle vertex
 * @param p2 third triangle vertex
 */
record TriangleDescriptor(Point p0, Point p1, Point p2, Color emission, Double3 kA, Double3 kD, Double3 kS, Integer nShininess)
	implements GeometryDescriptor { }


package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * Base class for ray tracers.
 * <p>
 * A ray tracer receives a {@link Ray} and returns the {@link Color} that should
 * be written to the corresponding pixel.
 * </p>
 */
abstract class RayTracerBase {
   /** Scene to trace rays in. */
   protected final Scene _scene;

   /**
    * Constructs a ray tracer for a given scene.
    *
    * @param scene the scene
    */
   RayTracerBase(Scene scene) {
      _scene = scene;
   }

   /**
    * Traces a ray and returns the computed color.
    *
    * @param ray ray to trace
    * @return resulting color
    */
   abstract Color traceRay(Ray ray);

   /**
    * Preprocesses intersection data that is independent of a specific light source.
    *
    * @param intersection the intersection to fill (cache)
    * @param v viewer/ray direction vector
    * @return {@code true} if the intersection is valid for local lighting calculations
    */
   protected boolean preprocessIntersection(Intersection intersection, Vector v) {
      intersection.v = v;
      intersection.normal = intersection.geometry.getNormal(intersection.point);
      intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
      return intersection.vNormal != 0;
   }

   /**
    * Preprocesses per-light-source data for a given intersection.
    * <p>
    * The light source and viewer must be on the same side of the surface; otherwise
    * the light contribution is ignored.
    * </p>
    *
    * @param intersection the intersection to fill (cache)
    * @param light the current light source
    * @return {@code true} if the light contributes to the intersection
    */
   protected boolean preprocessLightSource(Intersection intersection, LightSource light) {
      intersection.light = light;
      intersection.l = light.getL(intersection.point);
      intersection.lNormal = alignZero(intersection.l.dotProduct(intersection.normal));
      return intersection.lNormal * intersection.vNormal > 0;
   }
}

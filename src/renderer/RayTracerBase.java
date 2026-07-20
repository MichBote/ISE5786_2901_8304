package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.List;

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
    * Traces a beam of rays and returns the averaged color.
    *
    * @param rays rays to trace
    * @return averaged color of the beam
    */
   Color traceRays(List<Ray> rays) {
      if (rays == null || rays.isEmpty()) {
         throw new IllegalArgumentException("rays must not be null or empty");
      }

      Color color = Color.BLACK;
      for (Ray ray : rays) {
         color = color.add(traceRay(ray));
      }
      return color.scale(1d / rays.size());
   }

   /**
    * Preprocesses intersection data that is independent of a specific light source.
    *
    * @param intersection the intersection to fill (cache)
    * @param ray ray that produced the intersection
    * @return {@code true} if the intersection is valid for local lighting calculations
    */
   protected boolean preprocessIntersection(Intersection intersection, Ray ray) {
      intersection.v = ray.direction();
      intersection.normal = intersection.geometry.getNormal(intersection.point);
      intersection.nv = alignZero(intersection.normal.dotProduct(intersection.v));
      return intersection.nv != 0;
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
   protected boolean setLightSource(Intersection intersection, LightSource light) {
      intersection.light = light;
      intersection.l = light.getL(intersection.point);
      intersection.nl = alignZero(intersection.normal.dotProduct(intersection.l));
      return intersection.nl * intersection.nv > 0;
   }

   /**
    * Preprocesses per-light-source data with an explicit sampled light direction.
    *
    * @param intersection the intersection to fill
    * @param light the current light source
    * @param l sampled light direction from light area point to shaded point
    * @return {@code true} if the sampled light direction contributes
    */
   protected boolean setLightDirection(Intersection intersection, LightSource light, Vector l) {
      intersection.light = light;
      intersection.l = l;
      intersection.nl = alignZero(intersection.normal.dotProduct(intersection.l));
      return intersection.nl * intersection.nv > 0;
   }
}

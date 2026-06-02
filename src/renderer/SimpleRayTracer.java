package renderer;

import java.util.List;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * A basic ray tracer implementation.
 * <p>
 * At this stage, the color is determined by geometry emission and attenuated
 * ambient light.
 * </p>
 */
class SimpleRayTracer extends RayTracerBase {

   /**
    * Constructs a simple ray tracer.
    *
    * @param scene the scene
    */
   SimpleRayTracer(Scene scene) {
      super(scene);
   }

   @Override
   Color traceRay(Ray ray) {
      List<Intersection> intersections = _scene.geometries.calcIntersections(ray);
      if (intersections == null) {
         return _scene.background;
      }

      Intersection closest = ray.findClosestIntersection(intersections);
      return preprocessIntersection(closest, ray) ? calcColor(closest) : Color.BLACK;
   }

   /**
    * Computes the color at an intersection point.
    *
    * @param intersection geometry-aware intersection point
    * @return computed color
    */
   private Color calcColor(Intersection intersection) {
      return _scene.ambientLight.getIntensity().scale(intersection.material.kA)
         .add(calcLocalEffects(intersection));
   }

   /**
    * Calculates local lighting effects (emission + diffuse + specular) from all external lights.
    *
    * @param intersection geometry-aware intersection point
    * @return local lighting color
    */
   private Color calcLocalEffects(Intersection intersection) {
      Color color = intersection.geometry.getEmission();

      for (var lightSource : _scene.lights) {
         if (!setLightSource(intersection, lightSource)) continue;

         Color lightIntensity = lightSource.getIntensity(intersection.point);
         Double3 factor = calcDiffuse(intersection).add(calcSpecular(intersection));
         color = color.add(lightIntensity.scale(factor));
      }

      return color;
   }

   /**
    * Computes the diffuse reflection factor (kD * |l·n|).
    *
    * @param intersection geometry-aware intersection point
    * @return diffuse reflection factor
    */
   private Double3 calcDiffuse(Intersection intersection) {
      double ln = intersection.nl;
      double factor = ln < 0 ? -ln : ln;
      return intersection.material.kD.scale(factor);
   }

   /**
    * Computes the specular reflection factor (kS * (max(0, -v·r))^nShininess).
    *
    * @param intersection geometry-aware intersection point
    * @return specular reflection factor
    */
   private Double3 calcSpecular(Intersection intersection) {
      // r = reflection of the incident direction (-l) around the normal
      Vector r;
      try {
         r = intersection.l.scale(-1)
            .add(intersection.normal.scale(2d * intersection.nl));
      } catch (IllegalArgumentException ex) {
         return Double3.ZERO;
      }

      double minusVR = alignZero(intersection.v.scale(-1).dotProduct(r));
      if (minusVR <= 0) return Double3.ZERO;

      int nShininess = intersection.material.nShininess;
      if (nShininess <= 0) return Double3.ZERO;

      double factor = Math.pow(minusVR, nShininess);
      return intersection.material.kS.scale(factor);
   }

}

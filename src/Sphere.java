import javafx.beans.binding.Bindings;
import javafx.geometry.Point3D;
import javafx.scene.effect.Light;

import java.awt.*;

/**
 * A simple sphere described by its center, radius and material.
 */
public class Sphere {
    public Point3D center;
    public double radius;
    public Material material = new Material();

    private double square(double a) {
        return a*a;
    }

    /**
     * Finds where the provided ray hits this sphere. If it does not hit the sphere, null is returned
     */
    public Point3D intersectWithRay(Ray ray) {
//        // TODO: why doesn't this work?
//        // The following was derived by solving the equation:
//        // || center - (ray.origin + t*ray.direction) ||^2 = radius^2
//        double delta = square(ray.unitDirection.dotProduct(center.subtract(ray.origin))) - GeometryHelpers.vectorLengthSquared(center.subtract(ray.origin)) + square(radius);
//        if (delta < 0) {
//            // No intersection
//            return null;
//        }
//        double dFromRayStart = (ray.unitDirection.dotProduct(center.subtract(ray.origin))) - delta;
//        if (dFromRayStart < 0) {
//            // The sphere is on the other side of the ray, no intersection
//            return null;
//        }
//        return ray.getPointAtDistance(dFromRayStart);

        Point3D co = ray.origin.subtract(center);
        double b = 2 * (co.dotProduct(ray.unitDirection));
        double c = co.dotProduct(co) - radius*radius;
        double delta = b*b - 4*c; // Since a is 1 (unitDirection dot unitDirection0

        if (delta < 0) return null;

        double sqrtDelta = Math.sqrt(delta);
        double negT = (-b - sqrtDelta) / 2;
        double posT = (-b + sqrtDelta) / 2;

        if(negT < 0 && posT < 0) {
            // The sphere is behind the ray origin
            return null;
        }

        double dFromRayStart;
        if(negT < 0 && posT > 0) {
            // We hit the sphere from the inside
            dFromRayStart = posT;
        } else {
            // Take the closer point of intersection
            dFromRayStart = negT;
        }
        return  ray.getPointAtDistance(dFromRayStart);
    }

    /**
     * @param p Point on the sphere
     * @return The normal to the sphere at the given point
     */
    public Point3D normalAtPoint(Point3D p) {
        return p.subtract(center).multiply(1.0/radius);
    }

    private LightIntensity computeTextureDiffuseReflectivity(Point3D viewedPoint) {
        Point3D centeredPoint = viewedPoint.subtract(center);

        // Converting from equirectangular sphere projection
        double lat = 0.5 * (centeredPoint.getY() / radius + 1) * Math.PI;
        double lon = Math.atan(centeredPoint.getX()/centeredPoint.getZ()) + Math.PI * 0.5;

        int pixelY = material.texture.getHeight() - (int) Math.floor(lat / Math.PI * material.texture.getHeight());
        int pixelX = (int) Math.floor(lon / (Math.PI) * material.texture.getWidth());

        // The floating point arithmetic may cause an off by one error. That is fine, apart from if we were
        // going to go out of bounds of our texture:
        if (pixelX < 0) {
            pixelX = 0;
        } else if (pixelX >= material.texture.getWidth()) {
            pixelX = material.texture.getWidth() - 1;
        }
        if (pixelY < 0) {
            pixelY = 0;
        } else if (pixelY >= material.texture.getHeight()) {
            pixelY = material.texture.getHeight() - 1;
        }

        Color pixel = new Color(material.texture.getRGB(pixelX, pixelY));
        LightIntensity result = new LightIntensity();
        result.setColor(pixel);
        return result;
    }

    /**
     * Computes the color of the specified point on the sphere using the material information and data about a single
     * light source.
     * @param viewedPoint The world coordinates of a point on the sphere for which we are finding the color
     * @param camera The position of the observer
     * @param light The light source used for finding the color of the point
     * @param ambientLight The intensity of the ambient light
     * @return The color of the specified point on the sphere
     */
    public Color computeColor(Point3D viewedPoint, Point3D camera, LightSource light, LightIntensity ambientLight) {
        double colorReflectivityR = material.diffuseReflectivityR;
        double colorReflectivityG = material.diffuseReflectivityG;
        double colorReflectivityB = material.diffuseReflectivityB;

        if (material.texture != null) {
            LightIntensity texelIntensity = computeTextureDiffuseReflectivity(viewedPoint);
            colorReflectivityR *= texelIntensity.red;
            colorReflectivityG *= texelIntensity.green;
            colorReflectivityB *= texelIntensity.blue;
        }

        Point3D lightRay = viewedPoint.subtract(light.position);
        double lightDistance = lightRay.magnitude();
        Point3D lightDir = lightRay.multiply(1.0/lightDistance);

        Point3D normalToViewedPoint = normalAtPoint(viewedPoint);

        // Prepare the diffuse component
        double normalDotLightRay = normalToViewedPoint.dotProduct((lightDir.multiply(-1)));
        if (normalDotLightRay < 0) {
            normalDotLightRay = 0;
        }
        double diffuseR = normalDotLightRay * colorReflectivityR * light.intensities.red;
        double diffuseG = normalDotLightRay * colorReflectivityG * light.intensities.green;
        double diffuseB = normalDotLightRay * colorReflectivityB * light.intensities.blue;

        // Prepare the specular component
        // Calculate reflected ray direction
        Point3D flippedLightDir = lightDir.multiply(-1);
        Point3D reflectedRayDirection = flippedLightDir.subtract(
                flippedLightDir.subtract(GeometryHelpers.projectVectorOntoVector(flippedLightDir, normalToViewedPoint))
                        .multiply(2));
        Point3D directionToCamera = camera.subtract(viewedPoint).normalize();

        double rDotV = reflectedRayDirection.dotProduct(directionToCamera);
        double specularCoefficient = Math.pow(rDotV, material.gloss);
        if (rDotV < 0) {
            specularCoefficient = 0;
        }
        double specularR = specularCoefficient * light.intensities.red * material.glossIntensityR;
        double specularG = specularCoefficient * light.intensities.green * material.glossIntensityG;
        double specularB = specularCoefficient * light.intensities.blue * material.glossIntensityB;

        // ambient light:
        double ambientR = ambientLight.red * colorReflectivityR;
        double ambientG = ambientLight.red * colorReflectivityG;
        double ambientB = ambientLight.red * colorReflectivityB;

        // combined:
        double r = diffuseR + specularR + ambientR;
        double g = diffuseG + specularG + ambientG;
        double b = diffuseB + specularB + ambientB;

        // It is possible that some of our intensities went over 1.0, we need to clip them
        if (r > 1.0) {
            r = 1.0;
        }
        if (g > 1.0) {
            g = 1.0;
        }
        if (b > 1.0) {
            b = 1.0;
        }

        return new Color((float) r, (float) g, (float) b);
    }
}

import javafx.beans.binding.Bindings;
import javafx.geometry.Point3D;
import javafx.scene.effect.Light;

import java.awt.*;

/**
 * A simple sphere described by its center, radius and material.
 */
public class Sphere implements Shape3D{
    public Point3D center;
    public double radius;

    private double square(double a) {
        return a*a;
    }

    public Sphere(Point3D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Sphere() {}

    /**
     * @param p Point on the sphere
     * @return The normal to the sphere at the given point
     */
    public Point3D normalAtPoint(Point3D p) {
        return p.subtract(center).multiply(1.0/radius);
    }

//    private LightIntensity computeTextureDiffuseReflectivity(Point3D viewedPoint) {
//        Point3D centeredPoint = viewedPoint.subtract(center);
//
//        // Converting from equirectangular sphere projection
//        double lat = 0.5 * (centeredPoint.getY() / radius + 1) * Math.PI;
//        double lon = Math.atan(centeredPoint.getX()/centeredPoint.getZ()) + Math.PI * 0.5;
//
//        int pixelY = material.texture.getHeight() - (int) Math.floor(lat / Math.PI * material.texture.getHeight());
//        int pixelX = (int) Math.floor(lon / (Math.PI) * material.texture.getWidth());
//
//        // The floating point arithmetic may cause an off by one error. That is fine, apart from if we were
//        // going to go out of bounds of our texture:
//        if (pixelX < 0) {
//            pixelX = 0;
//        } else if (pixelX >= material.texture.getWidth()) {
//            pixelX = material.texture.getWidth() - 1;
//        }
//        if (pixelY < 0) {
//            pixelY = 0;
//        } else if (pixelY >= material.texture.getHeight()) {
//            pixelY = material.texture.getHeight() - 1;
//        }
//
//        Color pixel = new Color(material.texture.getRGB(pixelX, pixelY));
//        LightIntensity result = new LightIntensity();
//        result.setColor(pixel);
//        return result;
//    }

    /**
     * @param ray: The ray that interacts with this particular sphere.
     * @return IntersectionPoint between the Sphere and the Ray projected
     */
    @Override
    public IntersectionPoint castRay(Ray ray) {

        Point3D co = ray.origin.subtract(center);
        double b = 2 * (co.dotProduct(ray.unitDirection));
        double c = co.dotProduct(co) - radius*radius;
        double delta = b*b - 4*c; // Since a is 1 (unitDirection dot unitDirection0

        if (delta < 0) return null;

        double sqrtDelta = Math.sqrt(delta);
        double negT = (-b - sqrtDelta) / 2;
        double posT = (-b + sqrtDelta) / 2;

        if(negT <= 0 && posT <= 0) {
            // The sphere is behind the ray origin
            return null;
        }

        double dFromRayStart;
        boolean collidedInside = false;
        if(negT <= 0 && posT > 0) {
            // We hit the sphere from the inside
            dFromRayStart = posT;
            collidedInside = true;
        } else {
            // Take the closer point of intersection
            dFromRayStart = negT;
        }
        IntersectionPoint pointOfIntersection = new IntersectionPoint(ray.getPointAtDistance(dFromRayStart), collidedInside, this);
        return pointOfIntersection;
    }

    /**
     * @param pointInShape Point on the sphere
     * @return point3D, The normal to the sphere at the given point
     */
    @Override
    public Point3D getNormalAtPoint(Point3D pointInShape) {
        return pointInShape.subtract(center).multiply(1.0/radius);
    }

    public static class SphereSolid extends Solid {
        Sphere sphere;

        SphereSolid(Sphere sphere, Material material) {
            super(sphere, material);
            this.sphere = sphere;
        }

        @Override
        public LightIntensity getDiffuseReflectivityAtPoint(Point3D p) {
            return getMaterial().diffuseReflectivity;
        }
    }
}

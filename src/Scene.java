import javafx.geometry.Point3D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Scene {
    Camera camera;
    ArrayList<Shape3D> shapes = new ArrayList<>();
    ArrayList<LightSource> lightSources = new ArrayList<>();
    private int width, height;
    private double exposure;
    private LightIntensity ambientLight;

    public Scene() {
        width = 800;
        height = 600;
        camera = new Camera(
                new Point3D(0, 0, -5),
                new Point3D(0,0,0),
                new Point3D(0,1,0),
                Math.PI * 0.5, width, height
        );
        exposure = 1.0;
        ambientLight = new LightIntensity();
        ambientLight.red = 0.1;
        ambientLight.green = 0.1;
        ambientLight.blue = 0.1;

        lightSources.add(new LightSource(new Point3D(-5, 0, 0), 1.0, 1.0, 1.0));

        shapes.add(new Sphere(new Point3D(0,0,0), 1));
        shapes.add(new Sphere(new Point3D(-1,0,0), 0.5));
    }

    /**
     * Computes the color of the specified point on the sphere using the material information and data about a single
     * light source.
     * @param viewedPoint The world coordinates of a point on the sphere for which we are finding the color
     * @param shape The shape to be displayed
     * @param light The light source used for finding the color of the point
     * @param ambientLight The intensity of the ambient light
     * @return The color of the specified point on the sphere
     */
    private LightIntensity computeIntensity(Point3D viewedPoint, Shape3D shape, LightSource light, LightIntensity ambientLight) {
        double colorReflectivityR = shape.getMaterial().diffuseReflectivityR;
        double colorReflectivityG = shape.getMaterial().diffuseReflectivityG;
        double colorReflectivityB = shape.getMaterial().diffuseReflectivityB;

        Point3D lightRay = viewedPoint.subtract(light.position);
        double lightDistance = lightRay.magnitude();
        Point3D lightDir = lightRay.multiply(1.0/lightDistance);

        Point3D normalToViewedPoint = shape.getNormalAtPoint(viewedPoint);

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
        Point3D directionToCamera = camera.getPosition().subtract(viewedPoint).normalize();

        double rDotV = reflectedRayDirection.dotProduct(directionToCamera);
        double specularCoefficient = Math.pow(rDotV, shape.getMaterial().gloss);
        if (rDotV < 0) {
            specularCoefficient = 0;
        }
        double specularR = specularCoefficient * light.intensities.red * shape.getMaterial().glossIntensityR;
        double specularG = specularCoefficient * light.intensities.green * shape.getMaterial().glossIntensityG;
        double specularB = specularCoefficient * light.intensities.blue * shape.getMaterial().glossIntensityB;

        // ambient light:
        double ambientR = ambientLight.red * colorReflectivityR;
        double ambientG = ambientLight.red * colorReflectivityG;
        double ambientB = ambientLight.red * colorReflectivityB;

        // combined:
        double r = diffuseR + specularR + ambientR;
        double g = diffuseG + specularG + ambientG;
        double b = diffuseB + specularB + ambientB;

        LightIntensity result = new LightIntensity();
        result.red = r;
        result.green = g;
        result.blue = b;

        return result;
    }

    public LightIntensity traceRay(Ray ray) {
        double closestPointDistanceSquared = Double.POSITIVE_INFINITY;
        IntersectionPoint closestPoint = null;
        for (Shape3D shape : shapes) {
            IntersectionPoint intersectionPoint = shape.castRay(ray);
            if (intersectionPoint != null) {
                double distanceToIntersectionSquared =
                        GeometryHelpers.vectorLengthSquared(intersectionPoint.pointOfIntersection.subtract(camera.getPosition()));
                if (distanceToIntersectionSquared < closestPointDistanceSquared) {
                    closestPointDistanceSquared = distanceToIntersectionSquared;
                    closestPoint = intersectionPoint;
                }
            }
        }

        if (closestPoint == null) {
            return LightIntensity.makeZero();
        }
        return computeIntensity(closestPoint.pointOfIntersection, closestPoint.shape, lightSources.get(0), ambientLight);
    }

    public BufferedImage render() {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // We will calculate the color for each pixel separately
        for (int x = 0; x < canvas.getWidth(); ++x) {
            for (int y = 0; y < canvas.getHeight(); ++y) {
                Ray ray = camera.getRayForPixel(x, y);
                LightIntensity intensity = traceRay(ray);
                Color pixelColor = intensity.translateToRGB(1.0 / exposure);
                canvas.setRGB(x, y, pixelColor.getRGB());
            }
        }
        return canvas;
    }
}

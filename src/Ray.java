import javafx.geometry.Point3D;

/**
 * Simple class representing a directed ray in 3-space
 */
public class Ray {
    // The starting point of the ray
    public Point3D origin;

    // The direction of the vector - must be a unit vector
    public Point3D unitDirection;

    /**
     * @param startPoint The starting point of the ray
     * @param unitDirection The direction of the vector - must be a unit vector
     */
    public Ray(Point3D startPoint, Point3D unitDirection) {
        this.origin = startPoint;
        this.unitDirection = unitDirection;
    }

    /**
     * Creates a new ray starting at a point and going through another point
     * @param startPoint The starting point of the ray
     * @param pointOnRay An additional point on the ray
     * @return A new ray starting at a point and going through another point
     */
    public static Ray makeRayFromTwoPoints(Point3D startPoint, Point3D pointOnRay) {
        return new Ray(startPoint, pointOnRay.subtract(startPoint).normalize());
    }

    public Ray() {
        origin = new Point3D(0,0,0);
        unitDirection = new Point3D(1,0,0);
    }

    public void shift(double amount) {
        origin = getPointAtDistance(amount);
    }

    /**
     * Finds a point on the ray at a given distance
     * @param dFromRayStart The distance from the origin of the ray
     * @return The point on the ray that is exactly dFromRayStart away from the ray's origin
     */
    public Point3D getPointAtDistance(double dFromRayStart) {
        return origin.add(unitDirection.multiply(dFromRayStart));
    }
}

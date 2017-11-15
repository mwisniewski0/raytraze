import javafx.geometry.Point3D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;

public class GeometryHelpers {
    /** Rotates a point about another point by a given angle
     * @param toRotate The rotated point
     * @param pivot The point about which the rotation is performed
     * @param angle The angle of the rotation
     * @return The rotated point
     */
    public static Point2D.Double rotatePointAboutPoint(Point2D.Double toRotate, Point2D.Double pivot, double angle) {
        // Clone toRotate so that the original does not get changed
        toRotate = clonePoint(toRotate);

        // translate to the center
        toRotate.x -= pivot.x;
        toRotate.y -= pivot.y;

        // rotate around the center
        // (Can provide my derivation if needed)
        Point2D.Double result = new Point2D.Double();
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        result.x = toRotate.x * cos - toRotate.y * sin;
        result.y = toRotate.x * sin + toRotate.y * cos;

        // Undo the initial translation
        result.x += pivot.x;
        result.y += pivot.y;

        return result;
    }

    /**
     * Creates a copy of the provided point
     * @param toClone The point to copy
     * @return A copy of the provided point
     */
    public static Point2D.Double clonePoint(Point2D.Double toClone) {
        return new Point2D.Double(toClone.x, toClone.y);
    }

    /**
     * Creates a regular polygon using a single side
     * @param sideStart The starting point of the initial side
     * @param sideEnd The ending point of the initial side
     * @param sidesCount Number of sides in the polygon (note, minimum 3 sides are required)
     * @return An array of points that contains the points of the resultant regular polygon
     */
    public static Point2D.Double[] buildRegularPolygonOnSide(Point2D.Double sideStart, Point2D.Double sideEnd, int sidesCount) {
        if (sidesCount < 3) {
            throw new IllegalArgumentException("A 2D polygon needs to have at least 3 sides." +
                    "sidesCount needs to be >= 3");
        }

        Point2D.Double[] result = new Point2D.Double[sidesCount];

        // Set the first two elements to copies of the parameters. This way changes to the arguments won't be reflected
        // in our polygon
        result[0] = clonePoint(sideStart);
        result[1] = clonePoint(sideEnd);

        // This method will work by rotating a point of the polygon about the next point.
        // The angle below is derived as follows:
        //   - Circumscribe a circle on the regular polygon
        //   - Consider the triangles created by connecting any of the sides to the center of the circle
        //       - These triangles are all congruent, isosceles, with the angle attached to the circle center being
        //         360 deg / number of sides. Thus, the other angles are (180 - (360 / number of sides)) / 2
        //   - The angle between the sides is equal to double the angle of the isosceles triangle, therefore we get:
        //     180 - (360 / number of sides)
        double angleBetweenSides = Math.PI - ((2 * Math.PI) / sidesCount);
        for (int i = 2; i < sidesCount; ++i) {
            result[i] = rotatePointAboutPoint(result[i-2], result[i-1], angleBetweenSides);
        }

        return result;
    }

    /**
     * Converts an array of Point2D.Double points to a Polygon with integer coefficients
     * @param points An array of points to be converted
     * @return The derived polygon
     */
    public static Polygon arrayOfPointsToPolygon(Point2D.Double[] points) {
        Polygon result = new Polygon();
        for (Point2D.Double point : points) {
            result.addPoint((int) Math.floor(point.x + 0.5), (int) Math.floor(point.y + 0.5));
        }
        return result;
    }

    /**
     * Given a pre-existing side of a triangle, finds the third point such that:
     *   - distance from sideEnd to the next point is nextSideLength
     *   - the angle <| sideStart, sideEnd, next point is the provided angleWithNextSide
     */
    public static Point2D.Double completeTriangle(Point2D.Double sideStart, Point2D.Double sideEnd, double nextSideLength, double angleWithNextSide) {
        // We will be modifying sideStart. We will clone it, so that the changes are not visible outside of the function
        sideStart = clonePoint(sideStart);

        // Translate to the origin
        sideStart.x -= sideEnd.x;
        sideStart.y -= sideEnd.y;

        // Scale so that the distance between sideStart and sideEnd is nextSideLength
        double sideLength = Math.sqrt(sideStart.x * sideStart.x + sideStart.y * sideStart.y);
        Point2D.Double scaledSideStart = new Point2D.Double();
        scaledSideStart.x = sideStart.x / sideLength * nextSideLength;
        scaledSideStart.y = sideStart.y / sideLength * nextSideLength;

        // Rotate the newly created point, to get the next point
        Point2D.Double rotatedPoint = rotatePointAboutPoint(scaledSideStart, new Point2D.Double(0, 0), angleWithNextSide);

        // Undo the translation
        rotatedPoint.x += sideEnd.x;
        rotatedPoint.y += sideEnd.y;

        return rotatedPoint;
    }

    /**
     * Creates a circle with the specified center and radius
     * @param center The center of the circle
     * @param radius The radius of the circle
     * @return A circle with the specified center and radius
     */
    public static Ellipse2D.Double makeCircle(Point2D.Double center, double radius) {
        return new Ellipse2D.Double(center.x - radius, center.y - radius, radius*2, radius*2);
    }

    /**
     * Projects a vector onto another vector. The new vector is returned and the passed vectors are not modified.
     * @param toProject The vector to be projected onto another vector
     * @param onto The vector onto which we are projecting.
     * @return The projected vector
     */
    public static Point3D projectVectorOntoVector(Point3D toProject, Point3D onto) {
        return onto.multiply(toProject.dotProduct(onto) / vectorLengthSquared(onto));
    }

    /**
     * Projects the provided vector onto a plane with the provided normal vector.
     * @param toProject The vector to be projected
     * @param planeNormal The normal vector of the plane onto which we are projecting
     * @return The projected vector
     */
    public static Point3D projectVectorOntoPlane(Point3D toProject, Point3D planeNormal) {
        return toProject.subtract(projectVectorOntoVector(toProject, planeNormal));
    }

    /**
     * Returns the squared length of the provided vector
     */
    public static double vectorLengthSquared(Point3D v) {
        return v.dotProduct(v);
    }

    /**
     * Refract's a ray according to snell's law
     * @param incidence The direction of the incidence ray
     * @param normal The normal of the refracting surface
     * @param fromIndex Refractive index of the material from which we are transferring
     * @param toIndex Refractive index of the material to which we are transferring
     * @return
     */
    public static Point3D refract(Point3D incidence, Point3D normal, double fromIndex, double toIndex) {
        // Snell's law: sin(a1)/sin(a2) = index2/index1
        // Thanks to a dude on SO: https://stackoverflow.com/a/29758766
        double r = fromIndex/toIndex;
        Point3D negNormal = new Point3D(0,0,0).subtract(normal);
        double c = normal.dotProduct(incidence);

        Point3D refracted = incidence.multiply(r).add(normal.multiply(r*c - Math.sqrt(1 - r*r*(1-c*c))));
        return refracted.normalize();
    }


    /*
    * Randomizes three components for a vector and returns it.*/
    public static Point3D randVector() {
        Point3D randVector = new Point3D(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
        return randVector;
    }

    /**
     * Reflects the given vector through the given normal
     * @param incidence The direction of the incidence ray
     * @param normal The normal of the reflecting surface
     * @return The reflected vector
     */
    public static Point3D reflect(Point3D incidence, Point3D normal) {
        return incidence.subtract(
                incidence.subtract(GeometryHelpers.projectVectorOntoVector(incidence, normal)).multiply(2))
                .multiply(-1);
    }
}

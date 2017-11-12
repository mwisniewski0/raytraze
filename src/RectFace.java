import javafx.geometry.Point3D;

import java.awt.geom.Point2D;

public class RectFace implements Shape3D {
    public Material material = new Material();

    private Point3D topLeft;
    private Point3D down;
    private Point3D right;
    private Point3D normal;
    private double width, height;

    public RectFace(Point3D topLeft, Point3D topRight, Point3D bottomLeft) {
        this.right = topRight.subtract(topLeft);
        this.width = right.magnitude();

        this.down = bottomLeft.subtract(topLeft);
        this.height = down.magnitude();

        this.normal = this.down.crossProduct(this.right).normalize();
        this.topLeft = topLeft;
    }

    private Point2D.Double getPointInLocalCoordinates(Point3D pointOnFace) {
        Point3D vectorFromTopLeft = pointOnFace.subtract(topLeft);
        double xCoordinate = GeometryHelpers.projectVectorOntoVector(vectorFromTopLeft, right).magnitude();
        double yCoordinate = GeometryHelpers.projectVectorOntoVector(vectorFromTopLeft, down).magnitude();
        return new Point2D.Double(xCoordinate, yCoordinate);
    }

    @Override
    public IntersectionPoint castRay(Ray ray) {
        double t = (topLeft.dotProduct(normal) - ray.origin.dotProduct(normal))/ray.unitDirection.dotProduct(normal);
        if (t < 0) {
            return null;
        }
        Point3D pointOfIntersection = ray.getPointAtDistance(t);

        Point2D.Double localCoords = getPointInLocalCoordinates(pointOfIntersection);
        if (localCoords.x < 0 || localCoords.x > width || localCoords.y < 0 || localCoords.y > height) {
            return null;
        }

        boolean hitFromInside = normal.dotProduct(ray.unitDirection) < 0;
        return new IntersectionPoint(pointOfIntersection, hitFromInside, this);
    }

    @Override
    public Point3D getNormalAtPoint(Point3D pointInShape) {
        return normal;
    }

    @Override
    public Material getMaterial() {
        return material;
    }
}

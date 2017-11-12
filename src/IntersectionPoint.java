import javafx.geometry.Point3D;

public class IntersectionPoint {
    public Point3D pointOfIntersection;
    public boolean collidedInside;
    public Shape3D shape;

    public IntersectionPoint(Point3D pointOfIntersection, boolean collidedInside, Shape3D shape) {
        this.pointOfIntersection = pointOfIntersection;
        this.collidedInside = collidedInside;
        this.shape = shape;
    }

    public Point3D getNormal() {
        return this.shape.getNormalAtPoint(this.pointOfIntersection);
    }

    public IntersectionPoint(){

    }
}

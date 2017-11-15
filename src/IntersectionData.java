import javafx.geometry.Point3D;

/**
 * The elementary step to any shading model requires as much information from the Intersection before preceding
 * further despite the depth of shading algorithm/optimizations utilized. Moreover, IntersectionData is a class
 * that stores all of the data which we implement to all of the elements in our scene through the Shape3D interface.
 * @author Pietro
 */
public class IntersectionData {
    public Point3D pointOfIntersection;
    public boolean collidedInside;
    public Shape3D shape;

    public IntersectionData(Point3D pointOfIntersection, boolean collidedInside, Shape3D shape) {
        this.pointOfIntersection = pointOfIntersection;
        this.collidedInside = collidedInside;
        this.shape = shape;
    }

    public Point3D getNormal() {
        return this.shape.getNormalAtPoint(this.pointOfIntersection);
    }

    public IntersectionData(){}
}

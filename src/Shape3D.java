import javafx.geometry.Point3D;

abstract public class Shape3D {
    public Material material;

    abstract public IntersectionPoint castRay(Ray ray);
    abstract public Point3D getNormalAtPoint(Point3D pointInShape);
}


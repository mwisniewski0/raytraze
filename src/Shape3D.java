import javafx.geometry.Point3D;

interface Shape3D {
    public IntersectionPoint castRay(Ray ray);
    public Point3D getNormalAtPoint(Point3D pointInShape);
    public Material getMaterial();
}


import javafx.geometry.Point3D;

interface Shape3D {
    IntersectionPoint castRay(Ray ray);
    Point3D getNormalAtPoint(Point3D pointInShape);
}


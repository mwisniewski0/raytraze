import javafx.geometry.Point3D;

abstract public class Solid {
    public class Intersection {
        public IntersectionPoint info;
        public Solid intersectedSolid;

        public Intersection(IntersectionPoint info, Solid solid) {
            this.info = info;
            this.intersectedSolid = solid;
        }

        public Intersection() {}
    }

    private Shape3D shape;
    private Material material;

    public Solid(Shape3D shape, Material material) {
        this.shape = shape;
        this.material = material;
    }

    public Solid() {}

    public Intersection castRay(Ray ray) {
        IntersectionPoint intersection = shape.castRay(ray);
        if (intersection == null) {
            return null;
        }
        return new Intersection(intersection, this);
    }

    public Shape3D getShape() {
        return shape;
    }

    public Material getMaterial() {
        return material;
    }

    abstract public LightIntensity getDiffuseReflectivityAtPoint(Point3D p);
}

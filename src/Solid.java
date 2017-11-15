import javafx.geometry.Point3D;

/**
 * Solid represents a Solid that can be placed inside a scene. Each solid has to have a shape and a material it is made
 * out of.
 */
abstract public class Solid {
    // Used to find intersections with Solids
    public class Intersection {
        public IntersectionData info;
        public Solid intersectedSolid;

        public Intersection(IntersectionData info, Solid solid) {
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

    /**
     * Casts a ray in the direction of the solid and returns the Intersection info. If no intersection happened, null
     * is returned
     */
    public Intersection castRay(Ray ray) {
        IntersectionData intersection = shape.castRay(ray);
        if (intersection == null) {
            return null;
        }
        return new Intersection(intersection, this);
    }

    /**
     * Returns the shape of this solid
     */
    public Shape3D getShape() {
        return shape;
    }

    /**
     * Returns the material of this solid
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Computes the DiffuseReflectivity coefficient at a given point. Necessary for texturing.
     */
    abstract public LightIntensity getDiffuseReflectivityAtPoint(Point3D p);
}

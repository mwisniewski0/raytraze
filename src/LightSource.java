import javafx.geometry.Point3D;

/**
 * Small structure describing a rectangular light source
 */
public class LightSource {
    /**
     * Describes the intersection
     */
    public class Intersection {
        public IntersectionData info;
        public LightSource intersectedLight;

        public Intersection(IntersectionData info, LightSource source) {
            this.info = info;
            this.intersectedLight = source;
        }

        public Intersection() {}
    }

    public LightIntensity intensity = new LightIntensity();
    private RectFace shape;

    /**
     * Returns a random point on the surface of the light source
     */
    public Point3D getRandomPoint() {
        double x = shape.getWidth() * Math.random();
        double y = shape.getHeight() * Math.random();

        return shape.getWorldPointAt(x, y);
    }

    public LightSource(LightIntensity intensity, RectFace shape) {
        this.intensity = intensity;
        this.shape = shape;
    }

    public RectFace getShape() {
        return shape;
    }

    /**
     * Casts a ray in the direction of the light source, and checks whether it collides with the light source.
     */
    public Intersection castRay(Ray ray) {
        IntersectionData intersection = shape.castRay(ray);
        if (intersection == null) {
            return null;
        }
        return new Intersection(intersection, this);
    }
}

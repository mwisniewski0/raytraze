import javafx.geometry.Point3D;

/**
 * Small structure describing a point light source
 */
public class LightSource {
    public class Intersection {
        public IntersectionPoint info;
        public LightSource intersectedLight;

        public Intersection(IntersectionPoint info, LightSource source) {
            this.info = info;
            this.intersectedLight = source;
        }

        public Intersection() {}
    }

    public LightIntensity intensity = new LightIntensity();
    private RectFace shape;

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

    public Intersection castRay(Ray ray) {
        IntersectionPoint intersection = shape.castRay(ray);
        if (intersection == null) {
            return null;
        }
        return new Intersection(intersection, this);
    }
}

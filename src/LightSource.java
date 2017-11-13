import javafx.geometry.Point3D;

/**
 * Small structure describing a point light source
 */
public class LightSource {
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
}

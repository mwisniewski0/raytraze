import javafx.geometry.Point3D;

/**
 * Small structure describing a point light source
 */
public class LightSource {
    public LightIntensity intensities = new LightIntensity();
    public Point3D position;

    public LightSource(Point3D position, double r, double g, double b) {
        this.position = position;
        this.intensities.red = r;
        this.intensities.green = g;
        this.intensities.blue = b;
    }
}

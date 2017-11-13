import javafx.geometry.Point3D;

import java.awt.image.BufferedImage;

/**
 * A small structure describing the material out of which an object is made
 */
public class Material {
    public LightIntensity diffuseReflectivity;
    public double gloss;
    public double glossIntensityR, glossIntensityG, glossIntensityB;
    public LightIntensity passthroughIntensity;
    public double refractionIndex;

    BufferedImage texture;

    public Material() {
        diffuseReflectivity = new LightIntensity();
        diffuseReflectivity.red = diffuseReflectivity.green = diffuseReflectivity.blue = 1.0;
        gloss = 20;
        glossIntensityR = glossIntensityG = glossIntensityB = 1.0;
        passthroughIntensity = new LightIntensity();
        refractionIndex = 1.0;
    }
}

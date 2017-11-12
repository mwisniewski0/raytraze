import java.awt.image.BufferedImage;

/**
 * A small structure describing the material out of which an object is made
 */
public class Material {
    public double diffuseReflectivityR, diffuseReflectivityG, diffuseReflectivityB;
    public double gloss;
    public double glossIntensityR, glossIntensityG, glossIntensityB;
    
    BufferedImage texture;

    public Material() {
        diffuseReflectivityR = diffuseReflectivityG = diffuseReflectivityB = 1.0;
        gloss = 20;
        glossIntensityR = glossIntensityG = glossIntensityB = 1.0;
    }
}

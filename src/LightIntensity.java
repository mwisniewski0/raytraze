import java.awt.*;

/**
 * Represents light intensity using floating point numbers. Notice that an RGB color cannot be used to accurately model
 * light intensity, since it has a maximum value.
 */
public class LightIntensity {
    public double blue;
    public double green;
    public double red;

    /**
     * @return The color representation of this LightIntensity
     */
    public Color getColor() {
        return new Color((float) red, (float) green, (float) blue);
    }

    /**
     * Maps the provided color to a LightIntensity (a max Color brightness (255) is mapped to 1.
     */
    public void setColor(Color color) {
        red = ((double) color.getRed()) / 255;
        green = ((double) color.getGreen()) / 255;
        blue = ((double) color.getBlue()) / 255;
    }
}

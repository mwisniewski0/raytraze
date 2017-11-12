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

    /**
     * Turns this LightIntensity to a color displayable on the screen. whiteIntensity controls the exposure/shutter.
     * @param whiteIntensity the maximum intensity of light - that is, everything above whiteIntensity will be turned
     *                       into 255 in the final RGB color.
     */
    public Color translateToRGB(double whiteIntensity) {
        int redComponent = getRGBComponentFromIntensity(red, whiteIntensity);
        int greenComponent = getRGBComponentFromIntensity(green, whiteIntensity);
        int blueComponent = getRGBComponentFromIntensity(blue, whiteIntensity);
        return new Color(redComponent, greenComponent, blueComponent);
    }

    private int getRGBComponentFromIntensity(double intensity, double maxIntensity) {
        int rgbComponent = (int) (intensity / maxIntensity * 255);
        if (rgbComponent > 255) {
            rgbComponent = 255;
        }
        return rgbComponent;
    }

    public LightIntensity multiply(double factor) {
        LightIntensity result = new LightIntensity();
        result.red = red * factor;
        result.green = green * factor;
        result.blue = blue * factor;
        return result;
    }

    public LightIntensity add(LightIntensity other) {
        LightIntensity result = new LightIntensity();
        result.red = red + other.red;
        result.green = green + other.green;
        result.blue = blue + other.blue;
        return result;
    }

    static public LightIntensity makeZero() {
        LightIntensity result = new LightIntensity();
        result.red = result.green = result.blue = 0;
        return result;
    }

    public boolean isZero() {
        double epsilon = 0.000001;
        return red + green + blue < epsilon;
    }

    public LightIntensity multiply(LightIntensity other) {
        LightIntensity result = new LightIntensity();
        result.red = red * other.red;
        result.green = green * other.green;
        result.blue = blue * other.blue;
        return result;
    }
}

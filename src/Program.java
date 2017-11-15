import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The main class of the program which and displays the scene.
 */
public class Program {
    public static void main(String[] args) {
        // Closing any of the windows results in the program terminating.
        JFrame displayFrame = new JFrame();

        Scene scene = new Scene();
        displayFrame.add(scene);
        displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        displayFrame.pack();
        displayFrame.setVisible(true);

//        scene.makePrettyStuff();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class Program {
    public static void main(String[] args) {
        // We need two windows - one for the scene and one for the GUI. otherwise it's very messy.
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

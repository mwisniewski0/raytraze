import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class Program {
    public static void main(String[] args) {
        // We need two windows - one for the scene and one for the GUI. otherwise it's very messy.
        // Closing any of the windows results in the program terminating.
        JFrame displayFrame = new JFrame();

        Scene scene = new Scene();
        BufferedImage render = scene.render();
        JPanel displayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(render,0, render.getHeight(), render.getWidth(), -render.getHeight(), null);
            }
        };
        displayPanel.setPreferredSize(new Dimension(render.getWidth(), render.getHeight()));
        displayFrame.add(displayPanel);
        displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        displayFrame.pack();
        displayFrame.setVisible(true);
    }
}

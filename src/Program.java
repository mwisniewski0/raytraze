import javax.swing.*;


public class Program {
    public static void main(String[] args) {
        // We need two windows - one for the scene and one for the GUI. otherwise it's very messy.
        // Closing any of the windows results in the program terminating.
        JFrame sceneFrame = new JFrame();
        sceneFrame.setTitle("Scene");
        sceneFrame.setSize(500,500);

        //Sets the window to close when upper right corner clicked.
        sceneFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SphereScene sphereScene = new SphereScene();
        sceneFrame.add(sphereScene);
        sceneFrame.pack();
        sceneFrame.setResizable(true);

        // -----------------------------------------------
        JFrame guiFrame = new JFrame();
        guiFrame.setTitle("GUI");
        guiFrame.setSize(500,500);

        //Sets the window to close when upper right corner clicked.
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        guiFrame.add(new SphereGUI(sphereScene));
        guiFrame.pack(); // resizes to preferred size for components.
        guiFrame.setResizable(true);

        // Make the windows visible
        JOptionPane.showMessageDialog(
                null,
                "CAMERA CONTROLS:\n" +
                        "W - move forward \n" +
                        "S - move backward \n" +
                        "A - move left \n" +
                        "D - move right \n" +
                        "Q - rotate left \n" +
                        "E - rotate right \n" +
                        "R - rotate up \n" +
                        "F - rotate down \n\n" +
                        "LIGHT SOURCE CONTROLS:\n" +
                        "I - move up \n" +
                        "K - move down \n" +
                        "J - move left \n" +
                        "L - move right \n" +
                        "U - move closer \n" +
                        "O - move away\n\n" +
                        "Be sure to add use a texture, it looks much better that way!",
                "DopeSphere manual",
                JOptionPane.INFORMATION_MESSAGE
        );
        guiFrame.setVisible(true);
        sceneFrame.setVisible(true);
    }
}

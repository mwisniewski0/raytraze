import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SphereGUI extends JPanel {
    SphereScene scene;

    JColorChooser lightColorChooser = new JColorChooser();
    JColorChooser diffuseChooser = new JColorChooser();
    JColorChooser specularChooser = new JColorChooser();
    JColorChooser ambientChooser = new JColorChooser(new Color(0.3f, 0.3f, 0.3f));
    SpinnerNumberModel glossModel = new SpinnerNumberModel(20, 1, 1000, 0.5);
    JButton setTexture = new JButton("Choose texture (equirectangular projection):");
    JButton removeTexture = new JButton("Remove texture");

    public SphereGUI(SphereScene scene) {
        this.scene = scene;
        prepareElements();
        prepareListeners();
    }

    private void prepareListeners() {
        ChangeListener onSettingsChanged = e -> {
            scene.setLightColor(lightColorChooser.getColor());
            scene.setAmbientLightColor(ambientChooser.getColor());
            scene.setSphereColor(diffuseChooser.getColor());
            scene.setSphereGlossIntensity(specularChooser.getColor());
            scene.setSphereGlossiness((double) glossModel.getValue());
            scene.repaint();
        };

        lightColorChooser.getSelectionModel().addChangeListener(onSettingsChanged);
        ambientChooser.getSelectionModel().addChangeListener(onSettingsChanged);
        specularChooser.getSelectionModel().addChangeListener(onSettingsChanged);
        diffuseChooser.getSelectionModel().addChangeListener(onSettingsChanged);
        glossModel.addChangeListener(onSettingsChanged);

        setTexture.addActionListener(e -> {
            JFileChooser destinationChooser = new JFileChooser();
            destinationChooser.showOpenDialog(this);
            File inputFile = destinationChooser.getSelectedFile();
            try {
                BufferedImage texture = ImageIO.read(inputFile);
                scene.setSphereTexture(texture);
                scene.repaint();
            } catch (IOException error) {
                JOptionPane.showMessageDialog(
                        this,
                        "Bummer!",
                        "Could not load the file",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        removeTexture.addActionListener(e -> {
            scene.setSphereTexture(null);
            scene.repaint();
        });
    }

    private void prepareElements() {
        this.setLayout(new BorderLayout());

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

        panel1.add(setTexture);
        panel1.add(removeTexture);

        panel1.add(new JLabel("Light color: "));
        panel1.add(lightColorChooser);
        lightColorChooser.setPreviewPanel(new JPanel());

        panel1.add(new JLabel("Diffuse coefficients: "));
        panel1.add(diffuseChooser);
        diffuseChooser.setPreviewPanel(new JPanel());

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

        panel2.add(new JLabel("Glossiness: "));
        panel2.add(new JSpinner(glossModel));

        panel2.add(new JLabel("Specular coefficients: "));
        panel2.add(specularChooser);
        specularChooser.setPreviewPanel(new JPanel());

        panel2.add(new JLabel("Ambient light color: "));
        panel2.add(ambientChooser);
        ambientChooser.setPreviewPanel(new JPanel());

        this.add(panel1, BorderLayout.WEST);
        this.add(panel2, BorderLayout.EAST);
    }
}

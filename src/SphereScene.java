import javafx.geometry.Point3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Builds a raytraced scene with one light source and one sphere
 */
public class SphereScene extends JPanel implements KeyListener, ComponentListener {
    private Sphere sphere;
    private Camera camera;
    private LightSource light;

    private LightIntensity ambientLight = new LightIntensity();

    private BufferedImage canvas;

    private int width = 800, height = 600;
    private final double LIGHT_MOVEMENT = 0.05;
    private final double ROTATION_STEP = 0.05;
    private final double CAMERA_MOVE_STEP = 0.05;

    public SphereScene() {
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(width, height));
        this.setFocusable(true);
        setUpScene();
        recreateCanvas();
        this.addKeyListener(this);
        this.addComponentListener(this);

        setUpTimer();
    }

    private void setUpTimer() {
        Timer timer = new Timer(50, e -> {
            update();
        });
        timer.setInitialDelay(2000);
        timer.start();
    }

    private void recreateCanvas() {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private void setUpScene() {
        this.light = new LightSource(new Point3D(-4, 0, 0), 1.0, 1.0, 1.0);

        // Some default values
        this.sphere = new Sphere();
        this.sphere.radius = 1.0;
        this.sphere.center = new Point3D(0,0,0);
        this.sphere.material.diffuseReflectivityR = 1.0;
        this.sphere.material.diffuseReflectivityG = 1.0;
        this.sphere.material.diffuseReflectivityB = 1.0;
        this.sphere.material.glossIntensityR = 1.0;
        this.sphere.material.glossIntensityG = 1.0;
        this.sphere.material.glossIntensityB = 1.0;
        this.sphere.material.gloss = 20.0;

        this.ambientLight.red = 0.3;
        this.ambientLight.green = 0.3;
        this.ambientLight.blue = 0.3;

        this.camera = new Camera(
                new Point3D(0,0, 4),
                new Point3D(0,0,0),
                new Point3D(0,1,0),
                Math.PI * .5,
                width,
                height
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // We will calculate the color for each pixel separately
        for (int x = 0; x < canvas.getWidth(); ++x) {
            for (int y = 0; y < canvas.getHeight(); ++y) {
                // Find the point on the sphere that can be seen in this pixel. Null is returned if this pixel does not
                // show the sphere
                Point3D spherePoint = sphere.intersectWithRay(camera.getRayForPixel(x, y));

                // Do the same for the light source (we want it to be visible to see if the scene is rendered correctly.
                Sphere lightSourceSphere = new Sphere();
                lightSourceSphere.radius = 0.1;
                lightSourceSphere.center = light.position;
                Point3D lightSourcePoint = lightSourceSphere.intersectWithRay(camera.getRayForPixel(x, y));

                // Draw either the sphere or the light source, depending on which one is closer (or color the pixel with
                // background color, if neither sphere nor light source is present in this pixel
                if (spherePoint == null && lightSourcePoint == null) {
                    canvas.setRGB(x, y, getBackground().getRGB());
                } else if (spherePoint == null && lightSourcePoint != null) {
                    canvas.setRGB(x, y, light.intensities.getColor().getRGB());
                } else if (spherePoint != null && lightSourcePoint == null) {
                    drawSpherePoint(x, y, spherePoint);
                } else {
                    if (camera.getPosition().distance(lightSourcePoint) < camera.getPosition().distance(spherePoint)) {
                        canvas.setRGB(x, y, light.intensities.getColor().getRGB());
                    } else {
                        drawSpherePoint(x, y, spherePoint);
                    }
                }
            }
        }

        g.drawImage(canvas,0, height, width, -height, null);
    }

    private void drawSpherePoint(int x, int y, Point3D spherePoint) {
        canvas.setRGB(x, y, sphere.computeColor(
                spherePoint, camera.getPosition(), light, ambientLight).getRGB());
    }

    public void setSphereMaterial(Material material) {
        this.sphere.material = material;
    }

    public void setSphereTexture(BufferedImage texture) {
        this.sphere.material.texture = texture;
    }

    public void setSphereGlossiness(double gloss) {
        this.sphere.material.gloss = gloss;
    }

    public void setSphereGlossIntensity(Color intensity) {
        this.sphere.material.glossIntensityR = intensity.getRed() / 255.0;
        this.sphere.material.glossIntensityG = intensity.getGreen() / 255.0;
        this.sphere.material.glossIntensityB = intensity.getBlue() / 255.0;
    }

    public void setSphereColor(Color color) {
        this.sphere.material.diffuseReflectivityR = color.getRed() / 255.0;
        this.sphere.material.diffuseReflectivityG = color.getGreen() / 255.0;
        this.sphere.material.diffuseReflectivityB = color.getBlue() / 255.0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    private boolean isKeyDown(int keyCode) {
        return keyboardState.containsKey(keyCode) && keyboardState.get(keyCode);
    }

    private void update() {
        boolean repaintNeeded = false;
        if (isKeyDown(KeyEvent.VK_I)) {
            light.position = light.position.add(0, LIGHT_MOVEMENT, 0);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_K)) {
            light.position = light.position.add(0, -LIGHT_MOVEMENT, 0);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_J)) {
            light.position = light.position.add(-LIGHT_MOVEMENT, 0, 0);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_L)) {
            light.position = light.position.add(LIGHT_MOVEMENT, 0, 0);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_U)) {
            light.position = light.position.add(0, 0, LIGHT_MOVEMENT);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_O)) {
            light.position = light.position.add(0, 0, -LIGHT_MOVEMENT);
            repaintNeeded = true;
        }

        // Camera controls
        if (isKeyDown(KeyEvent.VK_R)) {
            camera.rotateVertical(ROTATION_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_F)) {
            camera.rotateVertical(-ROTATION_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_Q)) {
            camera.rotateHorizontal(-ROTATION_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_E)) {
            camera.rotateHorizontal(ROTATION_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_W)) {
            camera.moveForward(CAMERA_MOVE_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_S)) {
            camera.moveBackward(CAMERA_MOVE_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_A)) {
            camera.moveLeft(CAMERA_MOVE_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_D)) {
            camera.moveRight(CAMERA_MOVE_STEP);
            repaintNeeded = true;
        }
        if (isKeyDown(KeyEvent.VK_SHIFT)) {
            camera.moveForward(-CAMERA_MOVE_STEP);
            repaintNeeded = true;
        }

        if (repaintNeeded) {
            repaint();
        }
    }

    private HashMap<Integer, Boolean> keyboardState = new HashMap<>();

    @Override
    public void keyPressed(KeyEvent e) {
        if (keyboardState.containsKey(e.getKeyCode())) {
            keyboardState.replace(e.getKeyCode(), true);
        } else {
            keyboardState.put(e.getKeyCode(), true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keyboardState.containsKey(e.getKeyCode())) {
            keyboardState.replace(e.getKeyCode(), false);
        }
    }

    public void setLightColor(Color color) {
        light.intensities.setColor(color);
    }

    public void setAmbientLightColor(Color color) {
        ambientLight.setColor(color);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        width = getWidth();
        height = getHeight();
        recreateCanvas();
        camera.changeSize(width, height);
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}

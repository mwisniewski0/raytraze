import javafx.geometry.Point3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

public class Scene extends JPanel implements KeyListener, ComponentListener {
    private static final double AIR_REFRACTION_INDEX = 1.0;
    private int width = 800, height = 600;
    private final double LIGHT_MOVEMENT = 0.05;
    private final double ROTATION_STEP = 0.05;
    private final double CAMERA_MOVE_STEP = 0.05;

    BufferedImage canvas;

    private static final double MINIMUM_RAY_LENGTH = 0.0001;
    Camera camera;
    ArrayList<Shape3D> shapes = new ArrayList<>();
    ArrayList<LightSource> lightSources = new ArrayList<>();
    private double exposure;
    private LightIntensity ambientLight;

    private LightIntensity computeDirectDiffuse(IntersectionPoint point) {
        Point3D target = point.pointOfIntersection;
        LightIntensity result = LightIntensity.makeZero();

        for (LightSource light : lightSources) {
            Point3D vectorToLight = light.position.subtract(target);

            Ray rayToLight = new Ray(target, vectorToLight.normalize());
            IntersectionPoint intersectionPoint = castRayOnShapes(rayToLight);
            if (intersectionPoint == null) {
                double normalDotLightRay = point.shape.getNormalAtPoint(point.pointOfIntersection).dotProduct((rayToLight.unitDirection));
                if (normalDotLightRay < 0) {
                    normalDotLightRay = 0;
                }
                LightIntensity illumination = new LightIntensity();
                illumination.red = point.shape.getMaterial().diffuseReflectivityR * light.intensities.red;
                illumination.green = point.shape.getMaterial().diffuseReflectivityG * light.intensities.green;
                illumination.blue = point.shape.getMaterial().diffuseReflectivityB * light.intensities.blue;

                illumination = illumination.multiply(normalDotLightRay);

                result = result.add(illumination);
            }
        }

        return result;
    }

    public Scene() {
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(width, height));
        this.setFocusable(true);
        setUpScene();
        recreateCanvas();
        this.addKeyListener(this);
        this.addComponentListener(this);
        this.setUpTimer();
    }

    private void setUpScene() {
        camera = new Camera(
                new Point3D(0, 0, -5),
                new Point3D(0,0,0),
                new Point3D(0,1,0),
                Math.PI * 0.5, width, height
        );

        exposure = 1.0;
        ambientLight = new LightIntensity();
        ambientLight.red = 0.1;
        ambientLight.green = 0.1;
        ambientLight.blue = 0.1;

        lightSources.add(new LightSource(new Point3D(-1, -3, -5), 1.0, 1.0, 1.0));

        shapes.add(new Sphere(new Point3D(0,0,0), 1));

        Sphere transparent = new Sphere(new Point3D(-1.3,0,-0.2), 0.5);
        transparent.material.diffuseReflectivityR = 0.0;
        transparent.material.diffuseReflectivityG = 0.0;
        transparent.material.diffuseReflectivityB = 0.0;
        transparent.material.passthroughIntensity.red =   0.9;
        transparent.material.passthroughIntensity.green = 0.9;
        transparent.material.passthroughIntensity.blue =  0.9;
        transparent.material.refractionIndex = 1.33;

        shapes.add(transparent);
        shapes.add(new Sphere(new Point3D(0,0,80.5), 80));
    }

    /**
     * Computes the color of the specified point on the sphere using the material information and data about a single
     * light source.
     * @param viewedPoint The world coordinates of a point on the sphere for which we are finding the color
     * @param shape The shape to be displayed
     * @param light The light source used for finding the color of the point
     * @param ambientLight The intensity of the ambient light
     * @return The color of the specified point on the sphere
     */
    private LightIntensity computeIntensity(Point3D viewedPoint, Shape3D shape, LightSource light, LightIntensity ambientLight) {
        double colorReflectivityR = shape.getMaterial().diffuseReflectivityR;
        double colorReflectivityG = shape.getMaterial().diffuseReflectivityG;
        double colorReflectivityB = shape.getMaterial().diffuseReflectivityB;

        Point3D lightRay = viewedPoint.subtract(light.position);
        double lightDistance = lightRay.magnitude();
        Point3D lightDir = lightRay.multiply(1.0/lightDistance);

        Point3D normalToViewedPoint = shape.getNormalAtPoint(viewedPoint);

        // Prepare the diffuse component
        double normalDotLightRay = normalToViewedPoint.dotProduct((lightDir.multiply(-1)));
        if (normalDotLightRay < 0) {
            normalDotLightRay = 0;
        }
        double diffuseR = normalDotLightRay * colorReflectivityR * light.intensities.red;
        double diffuseG = normalDotLightRay * colorReflectivityG * light.intensities.green;
        double diffuseB = normalDotLightRay * colorReflectivityB * light.intensities.blue;

        // Prepare the specular component
        // Calculate reflected ray direction
        Point3D flippedLightDir = lightDir.multiply(-1);
        Point3D reflectedRayDirection = flippedLightDir.subtract(
                flippedLightDir.subtract(GeometryHelpers.projectVectorOntoVector(flippedLightDir, normalToViewedPoint))
                        .multiply(2));
        Point3D directionToCamera = camera.getPosition().subtract(viewedPoint).normalize();

        double rDotV = reflectedRayDirection.dotProduct(directionToCamera);
        double specularCoefficient = Math.pow(rDotV, shape.getMaterial().gloss);
        if (rDotV < 0) {
            specularCoefficient = 0;
        }
        double specularR = specularCoefficient * light.intensities.red * shape.getMaterial().glossIntensityR;
        double specularG = specularCoefficient * light.intensities.green * shape.getMaterial().glossIntensityG;
        double specularB = specularCoefficient * light.intensities.blue * shape.getMaterial().glossIntensityB;

        // ambient light:
        double ambientR = ambientLight.red * colorReflectivityR;
        double ambientG = ambientLight.red * colorReflectivityG;
        double ambientB = ambientLight.red * colorReflectivityB;

        // combined:
        double r = diffuseR + specularR + ambientR;
        double g = diffuseG + specularG + ambientG;
        double b = diffuseB + specularB + ambientB;

        LightIntensity result = new LightIntensity();
        result.red = r;
        result.green = g;
        result.blue = b;

        return result;
    }

    int refrCount = 0;
    public LightIntensity traceRay(Ray ray) {
        ray.shift(MINIMUM_RAY_LENGTH);

        boolean wasRefr = false;
        IntersectionPoint closestPoint = castRayOnShapes(ray);

        LightIntensity result = LightIntensity.makeZero();
        if (closestPoint != null) {
            if (!closestPoint.shape.getMaterial().passthroughIntensity.isZero()) {
                Point3D refractedRayDirection;
                if (closestPoint.collidedInside) {
                    // Getting out of the shape
                    refractedRayDirection = GeometryHelpers.refract(
                            ray.unitDirection, closestPoint.getNormal(), closestPoint.shape.getMaterial().refractionIndex, AIR_REFRACTION_INDEX);
                } else {
                    // Entering the shape
                    refractedRayDirection = GeometryHelpers.refract(
                            ray.unitDirection, closestPoint.getNormal(), AIR_REFRACTION_INDEX, closestPoint.shape.getMaterial().refractionIndex);
                }
                refrCount += 1;
                Ray refractedRay = new Ray(closestPoint.pointOfIntersection, refractedRayDirection);
                result = result.add(traceRay(refractedRay).multiply(closestPoint.shape.getMaterial().passthroughIntensity));
                if (result.isZero()) {
                    int a = 1;
                }
                refrCount -= 1;
                wasRefr = true;
            }
            result = result.add(computeDirectDiffuse(closestPoint));
        }
        if (closestPoint != null && closestPoint.shape == shapes.get(1)) {
            int a = 1;
        }
        return result;
    }

    private IntersectionPoint castRayOnShapes(Ray ray) {
        double closestPointDistanceSquared = Double.POSITIVE_INFINITY;
        IntersectionPoint closestPoint = null;
        for (Shape3D shape : shapes) {
            IntersectionPoint intersectionPoint = shape.castRay(ray);
            if (intersectionPoint != null) {
                double distanceToIntersectionSquared =
                        GeometryHelpers.vectorLengthSquared(intersectionPoint.pointOfIntersection.subtract(ray.origin));

                // To avoid hitting the shape from which the ray was shot, the ray has to have a minimum length
                if (distanceToIntersectionSquared > MINIMUM_RAY_LENGTH*MINIMUM_RAY_LENGTH) {
                    if (distanceToIntersectionSquared < closestPointDistanceSquared) {
                        closestPointDistanceSquared = distanceToIntersectionSquared;
                        closestPoint = intersectionPoint;
                    }
                }
            }
        }
        return closestPoint;
    }

    int fpsCount = 0;
    Long initialTime = null;
    public void render(BufferedImage canvas) {
        if (initialTime == null) {
            initialTime = System.nanoTime();
        }
        fpsCount += 1;

        // We will calculate the color for each pixel separately
//        IntStream.range(0,canvas.getWidth()).parallel().forEach(x->{
//            for (int y = 0; y < canvas.getHeight(); ++y) {
//                if (x == 0 && y == 251) {
//                    int a = 1;
//                }
//                Ray ray = camera.getRayForPixel(x, y);
//                LightIntensity intensity = traceRay(ray);
//                Color pixelColor = intensity.translateToRGB(1.0 / exposure);
//                canvas.setRGB(x, y, pixelColor.getRGB());
//            }
//        });

        for (int x = 0; x < canvas.getWidth(); ++x) {
            for (int y = 0; y < canvas.getHeight(); ++y) {
                if (x == 0 && y == 251) {
                    int a = 1;
                }
                Ray ray = camera.getRayForPixel(x, y);
                LightIntensity intensity = traceRay(ray);
                Color pixelColor = intensity.translateToRGB(1.0 / exposure);
                canvas.setRGB(x, y, pixelColor.getRGB());
            }
        }

        if (fpsCount % 60 == 0) {
            long newTime = System.nanoTime();
            System.out.println((double)(newTime - initialTime) / 1000000.0);
            initialTime = newTime;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(canvas);
        g.drawImage(canvas,0, canvas.getHeight(), canvas.getWidth(), -canvas.getHeight(), null);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private boolean isKeyDown(int keyCode) {
        return keyboardState.containsKey(keyCode) && keyboardState.get(keyCode);
    }

    private void update() {
        LightSource light = lightSources.get(0);

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
}

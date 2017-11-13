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
    private static final int LIGHT_SAMPLES_PER_LIGHT = 1;
    private int width = 800, height = 600;

    private final double ROTATION_STEP = 0.1;
    private final double CAMERA_MOVE_STEP = 0.5;

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
            LightIntensity intensityFromThisLight = LightIntensity.makeZero();

            for (int i = 0; i < LIGHT_SAMPLES_PER_LIGHT; ++i) {
                Point3D lightSamplePos = light.getRandomPoint();
                Point3D vectorToLight = lightSamplePos.subtract(target);

                Ray rayToLight = new Ray(target, vectorToLight.normalize());
                IntersectionPoint intersectionPoint = castRayOnShapes(rayToLight);
                if (intersectionPoint == null ||
                        intersectionPoint.pointOfIntersection.distance(target) > lightSamplePos.distance(target)) {
                    double normalDotLightRay = point.shape.getNormalAtPoint(point.pointOfIntersection).dotProduct((rayToLight.unitDirection));
                    if (normalDotLightRay < 0) {
                        normalDotLightRay *= -1;
                    }
                    LightIntensity illumination = point.shape.getMaterial().diffuseReflectivity.multiply(light.intensity.red);
                    illumination = illumination.multiply(normalDotLightRay);

                    intensityFromThisLight = intensityFromThisLight.add(illumination);
                }
            }

            intensityFromThisLight = intensityFromThisLight.multiply(1.0 / LIGHT_SAMPLES_PER_LIGHT);
            result = result.add(intensityFromThisLight);
        }

        result = result.add(ambientLight.multiply(point.shape.getMaterial().diffuseReflectivity));
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
                new Point3D(0, 0, 5),
                new Point3D(0,0,0),
                new Point3D(0,1,0),
                Math.PI * 0.5, width, height
        );

        camera.rotateVertical(Math.PI * 0.4);

        exposure = 1.0;
        ambientLight = new LightIntensity();
        ambientLight.red =   0.1;
        ambientLight.green = 0.1;
        ambientLight.blue =  0.1;

        lightSources.add(
                new LightSource(
                        new LightIntensity(1.3, 1.3, 1.3),
                        new RectFace(
                                new Point3D(-0.5, -6, -0.5),
                                new Point3D(0.5,  -6, -0.5),
                                new Point3D(-0.5, -6, .5)
                        )
                ));

        shapes.add(new Sphere(new Point3D(0,0,0), 1));

        Sphere transparent = new Sphere(new Point3D(-1.3,0,-0.2), 0.5);
        transparent.material.diffuseReflectivity.red = 0.0;
        transparent.material.diffuseReflectivity.green = 0.0;
        transparent.material.diffuseReflectivity.blue = 0.0;
        transparent.material.passthroughIntensity.red =   0.9;
        transparent.material.passthroughIntensity.green = 0.9;
        transparent.material.passthroughIntensity.blue =  0.9;
        transparent.material.refractionIndex = 1.33;

        shapes.add(transparent);

        Box boudingBox = new Box(
                new Point3D(-10,10, 10),
                new Point3D(-10,10, -10),
                new Point3D(10,10, 10),
                new Point3D(-10,-10, 10)
        );
        boudingBox.left.material.diffuseReflectivity = new LightIntensity(1.0,1.0, 0);
        boudingBox.right.material.diffuseReflectivity = new LightIntensity(1.0,0.0, 1.0);
        boudingBox.front.material.diffuseReflectivity = new LightIntensity(1.0,0, 0);
        boudingBox.back.material.diffuseReflectivity = new LightIntensity(1.0,0.0, 0);
        boudingBox.top.material.diffuseReflectivity = new LightIntensity(1.0,1.0, 1.0);
        boudingBox.bottom.material.diffuseReflectivity = new LightIntensity(1.0,1.0, 1.0);

        for (RectFace boxFace : boudingBox.getFaceList()) {
            shapes.add(boxFace);
        }
    }

    int refrCount = 0;
    public LightIntensity traceRay(Ray ray) {


        boolean wasRefr = false;
        IntersectionPoint shapeIntersection = castRayOnShapes(ray);
        IntersectionPoint lightIntersection = castRayOnLights(ray);

        LightIntensity result = LightIntensity.makeZero();
        if (shapeIntersection == null && lightIntersection == null) {
            // Nothing to do
        } else if (shapeIntersection == null && lightIntersection != null) {
            result = result.add(lightSources.get(0).intensity);
        } else if (shapeIntersection != null & lightIntersection == null) {
            result = handleShapeRayHit(ray, shapeIntersection, result);
        } else if (shapeIntersection.pointOfIntersection.distance(ray.origin) < lightIntersection.pointOfIntersection.distance(ray.origin)) {
            result = handleShapeRayHit(ray, shapeIntersection, result);
        } else {
            result = result.add(lightSources.get(0).intensity);
        }
        return result;
    }

    private LightIntensity handleShapeRayHit(Ray ray, IntersectionPoint shapeIntersection, LightIntensity result) {
        ray = ray.getShifted(MINIMUM_RAY_LENGTH);

        if (!shapeIntersection.shape.getMaterial().passthroughIntensity.isZero()) {
            Point3D refractedRayDirection;
            if (shapeIntersection.collidedInside) {
                // Getting out of the shape
                refractedRayDirection = GeometryHelpers.refract(
                        ray.unitDirection, shapeIntersection.getNormal(), shapeIntersection.shape.getMaterial().refractionIndex, AIR_REFRACTION_INDEX);
            } else {
                // Entering the shape
                refractedRayDirection = GeometryHelpers.refract(
                        ray.unitDirection, shapeIntersection.getNormal(), AIR_REFRACTION_INDEX, shapeIntersection.shape.getMaterial().refractionIndex);
            }
            refrCount += 1;
            Ray refractedRay = new Ray(shapeIntersection.pointOfIntersection, refractedRayDirection);
            result = result.add(traceRay(refractedRay).multiply(shapeIntersection.shape.getMaterial().passthroughIntensity));
            if (result.isZero()) {
                int a = 1;
            }
            refrCount -= 1;
        }
        result = result.add(computeDirectDiffuse(shapeIntersection));
        return result;
    }

    private IntersectionPoint castRayOnShapes(Ray ray) {
        ray = ray.getShifted(MINIMUM_RAY_LENGTH);

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

    private IntersectionPoint castRayOnLights(Ray ray) {
        double closestPointDistanceSquared = Double.POSITIVE_INFINITY;
        IntersectionPoint closestPoint = null;
        for (LightSource light : lightSources) {
            Shape3D shape = light.getShape();

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
                if (x == 355 && y == 368) {
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
        boolean repaintNeeded = false;

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

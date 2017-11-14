import javafx.geometry.Point3D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

public class Scene extends JPanel implements KeyListener, ComponentListener {
    private static final double AIR_REFRACTION_INDEX = 1.0;
    private static int LIGHT_SAMPLES_PER_LIGHT = 1;
    private static final int MAX_TRACE_DEPTH = 3;
    private static final int MONTE_CARLO_SAMPLES = 400;
    private int width = 400, height = 300;

    private final double ROTATION_STEP = 0.1;
    private final double CAMERA_MOVE_STEP = 0.5;

    BufferedImage canvas;

    private static final double MINIMUM_RAY_LENGTH = 0.0001;
    Camera camera;
    ArrayList<Solid> solids = new ArrayList<>();
    ArrayList<LightSource> lightSources = new ArrayList<>();
    private double exposure;
    private LightIntensity ambientLight;

    private LightIntensity computeDirectDiffuse(Solid.Intersection intersection) {
        Point3D target = intersection.info.pointOfIntersection;
        LightIntensity result = LightIntensity.makeZero();

        LightIntensity diffuseReflectivity = intersection.intersectedSolid.getDiffuseReflectivityAtPoint(intersection.info.pointOfIntersection);
        for (LightSource light : lightSources) {
            LightIntensity intensityFromThisLight = LightIntensity.makeZero();

            for (int i = 0; i < LIGHT_SAMPLES_PER_LIGHT; ++i) {
                Point3D lightSamplePos = light.getRandomPoint();
                Point3D vectorToLight = lightSamplePos.subtract(target);

                Ray rayToLight = new Ray(target, vectorToLight.normalize());
                Solid.Intersection solidIntersection = castRayOnSolids(rayToLight);
                if (solidIntersection == null ||
                        solidIntersection.info.pointOfIntersection.distance(target) > lightSamplePos.distance(target)) {
                    double normalDotLightRay = intersection.info.getNormal().dotProduct((rayToLight.unitDirection));
                    if (normalDotLightRay < 0) {
                        normalDotLightRay *= -1;
                    }
                    LightIntensity illumination = diffuseReflectivity.multiply(light.intensity);
                    illumination = illumination.multiply(normalDotLightRay);

                    intensityFromThisLight = intensityFromThisLight.add(illumination);
                }
            }

            intensityFromThisLight = intensityFromThisLight.multiply(1.0 / LIGHT_SAMPLES_PER_LIGHT);
            result = result.add(intensityFromThisLight);
        }

        result = result.add(ambientLight.multiply(diffuseReflectivity));
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

    public void makePrettyStuff() {
        LIGHT_SAMPLES_PER_LIGHT = 100;
        for (int i = 0; i < 30; ++i) {
            camera.moveBackward(.1);
            render(canvas);
            try {
                ImageIO.write(canvas, "png", new File("C:\\class_work\\" + new Integer(i).toString() +".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpScene() {
        camera = new Camera(
                new Point3D(0, 0, 9),
                new Point3D(0,0,0),
                new Point3D(0,1,0),
                Math.PI * 0.5, width, height
        );
        camera.moveForward(1.0);

        exposure = 1.0;
        ambientLight = new LightIntensity();
        ambientLight.red =   0.2;
        ambientLight.green = 0.2;
        ambientLight.blue =  0.2;

        lightSources.add(
                new LightSource(
                        new LightIntensity(.7,  .7, .7),
                        new RectFace(
                                new Point3D(-.1, 9.99, -5.0),
                                new Point3D(.1,  9.99, -5.0),
                                new Point3D(-.1, 9.99, -5.1)
                        )
                ));

//        lightSources.add(
//                new LightSource(
//                        new LightIntensity(1.2, 1.2, 1.2),
//                        new RectFace(
//                                new Point3D(-3.1, 2.99, -5.0),
//                                new Point3D(-2.9,  2.99, -5.0),
//                                new Point3D(-3.1, 2.99, -5.1)
//                        )
//                ));

        Material reflective = new Material();
        reflective.diffuseReflectivity.red = 0.2;
        reflective.diffuseReflectivity.green = 0.2;
        reflective.diffuseReflectivity.blue  = 0.2;
        reflective.directReflectivity = LightIntensity.makeUniformRGB(0.5);


        Material glass = new Material();
        glass.diffuseReflectivity.red = 0.0;
        glass.diffuseReflectivity.green = 0.0;
        glass.diffuseReflectivity.blue = 0.0;
        glass.passthroughIntensity.red =   0.9;
        glass.passthroughIntensity.green = 0.9;
        glass.passthroughIntensity.blue =  0.9;
        glass.directReflectivity = LightIntensity.makeUniformRGB(0.1);
        glass.refractionIndex = 1.33;

        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-5, -3, -5), 2.5), glass));
        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(5, -3, -5), 2.5), new Material()));

//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-9,0), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-9,0), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-9,0), 1), reflective));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-9,2), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-9,2), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-9,2), 1), glass));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-9,4), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-9,4), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-9,4), 1), reflective));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-6,0), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-6,0), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-6,0), 1), glass));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-6,2), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-6,2), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-6,2), 1), reflective));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-6,4), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-6,4), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-6,4), 1), glass));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-3,0), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-3,0), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-3,0), 1), reflective));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-3,2), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-3,2), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-3,2), 1), glass));
//
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(0,-3,4), 1), reflective));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-2,-3,4), 1), glass));
//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4,-3,4), 1), reflective));

//        solids.add(new Sphere.SphereSolid(new Sphere(new Point3D(-4.3,0,-0.2), 1), glass));

        Box boundingBox = new Box(
                new Point3D(-10,10, 10),
                new Point3D(-10,10, -10),
                new Point3D(10,10, 10),
                new Point3D(-10,-10, 10)
        );

        Material leftWallMaterial = new Material();
        leftWallMaterial.diffuseReflectivity = new LightIntensity(.7,.7, 0.3);
//        leftWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        Material rightWallMaterial = new Material();
        rightWallMaterial.diffuseReflectivity = new LightIntensity(.7,0.3, .7);
//        rightWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        Material frontWallMaterial = new Material();
        frontWallMaterial.diffuseReflectivity = new LightIntensity(0.3,0.3, 0.3);
//        frontWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        Material backWallMaterial = new Material();
        backWallMaterial.diffuseReflectivity = new LightIntensity(.7,0.3, 0.3);
//        backWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        Material topWallMaterial = new Material();
        topWallMaterial.diffuseReflectivity = new LightIntensity(.7,.7, .7);
//        topWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        Material bottomWallMaterial = new Material();
        bottomWallMaterial.diffuseReflectivity = new LightIntensity(.7,.7, .7);
//        bottomWallMaterial.directReflectivity = LightIntensity.makeUniformRGB(.3);

        try {
            bottomWallMaterial.texture = ImageIO.read(new File("C:\\Class_work\\checkerboard.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        solids.add(new RectFace.FaceSolid(boundingBox.left, leftWallMaterial));
        solids.add(new RectFace.FaceSolid(boundingBox.right, rightWallMaterial));
        solids.add(new RectFace.FaceSolid(boundingBox.front, frontWallMaterial));
        solids.add(new RectFace.FaceSolid(boundingBox.back, backWallMaterial));
        solids.add(new RectFace.FaceSolid(boundingBox.top, topWallMaterial));
        solids.add(new RectFace.FaceSolid(boundingBox.bottom, bottomWallMaterial));

        RectFace divider = new RectFace(
                new Point3D(-10, 0, 0),
                new Point3D(10, 0, 0),
                new Point3D(-10, 10, 0)
        );
//        solids.add(new RectFace.FaceSolid(divider, bottomWallMaterial));
    }

    public LightIntensity traceRay(Ray ray, int currentTraceDepth) {
        if (currentTraceDepth > MAX_TRACE_DEPTH) {
            return LightIntensity.makeZero();
        }

        currentTraceDepth += 1;

        Solid.Intersection solidIntersection = castRayOnSolids(ray);
        LightSource.Intersection lightIntersection = castRayOnLights(ray);

        LightIntensity result = LightIntensity.makeZero();
        if (solidIntersection == null && lightIntersection == null) {
            // Nothing to do
        } else if (solidIntersection == null && lightIntersection != null) {
            result = result.add(lightSources.get(0).intensity);
        } else if (solidIntersection != null & lightIntersection == null) {
            result = handleSolidRayHit(ray, solidIntersection, result, currentTraceDepth);
        } else if (solidIntersection.info.pointOfIntersection.distance(ray.origin) < lightIntersection.info.pointOfIntersection.distance(ray.origin)) {
            result = handleSolidRayHit(ray, solidIntersection, result, currentTraceDepth);
        } else {
            result = result.add(lightSources.get(0).intensity);
        }

        return result;
    }

    private LightIntensity handleSolidRayHit(Ray ray, Solid.Intersection intersection, LightIntensity result, int currentTraceDepth) {
        ray = ray.getShifted(MINIMUM_RAY_LENGTH);

        if (!intersection.intersectedSolid.getMaterial().passthroughIntensity.isZero()) {
            result = result.add(handleRefractedRay(ray, intersection, currentTraceDepth)
                    .multiply(intersection.intersectedSolid.getMaterial().passthroughIntensity));
        }
        if (!intersection.intersectedSolid.getMaterial().directReflectivity.isZero()) {
            result = result.add(handleReflectedRay(ray, intersection.info, currentTraceDepth)
                    .multiply(intersection.intersectedSolid.getMaterial().directReflectivity));
        }
        result = result.add(computeDirectDiffuse(intersection));
        result = result.add(computeIndirectDiffuse(intersection, currentTraceDepth));
        return result;
    }

    private LightIntensity computeIndirectDiffuse(Solid.Intersection intersection, int currentTraceDepth) {
        if (MONTE_CARLO_SAMPLES == 0) {
            return LightIntensity.makeZero();
        }

        LightIntensity averageResult = LightIntensity.makeZero();
        double totalWeight = 0;

        //Utilizes Monte Carlo approach
        for(int MCSample = 0; MCSample < MONTE_CARLO_SAMPLES; ++MCSample) {
            Point3D randomVector = GeometryHelpers.randVector().normalize();
            Ray ray = new Ray(intersection.info.pointOfIntersection, randomVector);

            double rayDotNormal = randomVector.dotProduct(intersection.info.getNormal()) * -1;
            if (rayDotNormal <= 0) {
                // Retry
                MCSample -= 1;
                continue;
            }
            totalWeight += rayDotNormal;
            LightIntensity intensity = traceRay(ray, currentTraceDepth);
            intensity = intensity.multiply(rayDotNormal).multiply(intersection.intersectedSolid.getDiffuseReflectivityAtPoint(intersection.info.pointOfIntersection));

            averageResult = averageResult.add(intensity);
        }
        return averageResult.multiply(1.0/totalWeight);
    }

    private LightIntensity handleReflectedRay(Ray ray, IntersectionPoint shapeIntersection, int currentTraceDepth) {
        Point3D reflectedRayDir = GeometryHelpers.reflect(ray.unitDirection, shapeIntersection.getNormal());
        Ray reflectedRay = new Ray(shapeIntersection.pointOfIntersection, reflectedRayDir);
        return traceRay(reflectedRay, currentTraceDepth);
    }

    private LightIntensity handleRefractedRay(Ray ray, Solid.Intersection solidIntersection, int currentTraceDepth) {
        Point3D refractedRayDirection;
        if (solidIntersection.info.collidedInside) {
            // Getting out of the shape
            refractedRayDirection = GeometryHelpers.refract(
                    ray.unitDirection, solidIntersection.info.getNormal(), solidIntersection.intersectedSolid.getMaterial().refractionIndex, AIR_REFRACTION_INDEX);
        } else {
            // Entering the shape
            refractedRayDirection = GeometryHelpers.refract(
                    ray.unitDirection, solidIntersection.info.getNormal(), AIR_REFRACTION_INDEX, solidIntersection.intersectedSolid.getMaterial().refractionIndex);
        }
        Ray refractedRay = new Ray(solidIntersection.info.pointOfIntersection, refractedRayDirection);
        return traceRay(refractedRay, currentTraceDepth);
    }

    private Solid.Intersection castRayOnSolids(Ray ray) {
        ray = ray.getShifted(MINIMUM_RAY_LENGTH);

        double closestPointDistanceSquared = Double.POSITIVE_INFINITY;
        Solid.Intersection closestIntersection = null;
        for (Solid solid : solids) {
            Solid.Intersection intersection = solid.castRay(ray);
            if (intersection != null) {
                double distanceToIntersectionSquared =
                        GeometryHelpers.vectorLengthSquared(intersection.info.pointOfIntersection.subtract(ray.origin));

                // To avoid hitting the shape from which the ray was shot, the ray has to have a minimum length
                if (distanceToIntersectionSquared > MINIMUM_RAY_LENGTH*MINIMUM_RAY_LENGTH) {
                    if (distanceToIntersectionSquared < closestPointDistanceSquared) {
                        closestPointDistanceSquared = distanceToIntersectionSquared;
                        closestIntersection = intersection;
                    }
                }
            }
        }
        return closestIntersection;
    }

    private LightSource.Intersection castRayOnLights(Ray ray) {
        double closestPointDistanceSquared = Double.POSITIVE_INFINITY;
        LightSource.Intersection closestIntersection = null;
        for (LightSource light : lightSources) {
            LightSource.Intersection intersection = light.castRay(ray);
            if (intersection != null) {
                double distanceToIntersectionSquared =
                        GeometryHelpers.vectorLengthSquared(intersection.info.pointOfIntersection.subtract(ray.origin));

                // To avoid hitting the shape from which the ray was shot, the ray has to have a minimum length
                if (distanceToIntersectionSquared > MINIMUM_RAY_LENGTH*MINIMUM_RAY_LENGTH) {
                    if (distanceToIntersectionSquared < closestPointDistanceSquared) {
                        closestPointDistanceSquared = distanceToIntersectionSquared;
                        closestIntersection = intersection;
                    }
                }
            }
        }
        return closestIntersection;
    }

    int fpsCount = 0;
    Long initialTime = null;
    public void render(BufferedImage canvas) {
        if (initialTime == null) {
            initialTime = System.nanoTime();
        }
        fpsCount += 1;

        // We will calculate the color for each pixel separately
        IntStream.range(0,canvas.getWidth()).parallel().forEach(x->{
            for (int y = 0; y < canvas.getHeight(); ++y) {
                Ray ray = camera.getRayForPixel(x, y);
                LightIntensity intensity = traceRay(ray, 0);
                Color pixelColor = intensity.translateToRGB(1.0 / exposure);
                canvas.setRGB(x, y, pixelColor.getRGB());
            }
            System.out.println(x);
        });

//        for (int x = 0; x < canvas.getWidth(); ++x) {
//            System.out.println(x);
//            for (int y = 0; y < canvas.getHeight(); ++y) {
//                Ray ray = camera.getRayForPixel(x, y);
//                LightIntensity intensity = traceRay(ray, 0);
//                Color pixelColor = intensity.translateToRGB(1.0 / exposure);
//                canvas.setRGB(x, y, pixelColor.getRGB());
//            }
//        }

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

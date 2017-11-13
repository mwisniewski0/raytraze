import javafx.geometry.Point3D;


/**
 * Camera represents a two dimensional view of the 3D world. Since this camera is used mostly for ray-tracing, it does
 * not need to provide functionality such as transforming 3D points into 2D points - instead it returns rays for
 * specific pixels.
 */
public class Camera {
    private Point3D lookingAt;
    private Point3D cameraPoint;
    private Point3D upDirection;
    private double horizontalAngleOfView;
    private int width;
    private int height;
    PerspectiveCanvas canvas;

    /**
     * @param cameraPoint Location of the camera/eye of the observer
     * @param lookingAt The point towards which the observer is oriented
     * @param upDir The upwards direction to properly orient the perspective plane. This vector will be projected onto
     *              the perspective plane, therefore it does not need to be necessarily parallel to the perspective
     *              plane
     * @param horizontalAngleOfView The angle of view of the resultant perspective plane. Needs to be provided in
     *                              radians
     * @param width The width of the resultant perspective plane in pixels
     * @param height The height of the resultant perspective plane in pixels
     */
    public Camera(Point3D cameraPoint, Point3D lookingAt, Point3D upDir, double horizontalAngleOfView, int width, int height) {
        this.cameraPoint = cameraPoint;
        this.lookingAt = lookingAt;
        this.horizontalAngleOfView = horizontalAngleOfView;
        this.width = width;
        this.height = height;

        Point3D canvasNormal = calculateCanvasNormal();
        upDirection = GeometryHelpers.projectVectorOntoPlane(upDir, canvasNormal).normalize();

        refreshCanvas();
    }

    private Point3D calculateCanvasNormal() {
        return lookingAt.subtract(cameraPoint);
    }

    /**
     * Changes the size of the perspective plane. Useful when the containing window is resized. Note that the angle of
     * view stays the same.
     * @param width The new width of the perspective plane in pixels
     * @param height The new height of the perspective plane in pixels
     */
    public void changeSize(int width, int height) {
        this.width = width;
        this.height = height;
        refreshCanvas();
    }

    private void refreshCanvas() {
        canvas = new PerspectiveCanvas(
                cameraPoint, lookingAt, upDirection, horizontalAngleOfView, (double) width / height);
    }

    /**
     * Returns the ray from the eye of the observer that goes through the specified pixel on the perspective plane
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     * @return A ray from the eye of the observer through the specified pixel.
     */
    public Ray getRayForPixel(int x, int y) {
        Point3D pointOnCanvas = canvas.getWorldPointFromCanvasCoord(
                (double) (x) / width, (double) y / height);
        return Ray.makeRayFromTwoPoints(cameraPoint, pointOnCanvas);
    }

    /**
     * @return The position of the observer
     */
    public Point3D getPosition() {
        return cameraPoint;
    }

    /**
     * Rotates the camera along the horizontal axis (around the vertical axis). The rotation is to the right.
     * @param angle The angle of rotation in radians.
     */
    public void rotateHorizontal(double angle) {
        Transform3D transform = Transform3D.rotateCCWAroundAxis(cameraPoint, cameraPoint.add(upDirection), angle);
        lookingAt = transform.transform(lookingAt);
        refreshCanvas();
    }

    /**
     * Rotates the camera along the vertical axis (around the horizontal axis). The rotation is upwards.
     * @param angle The angle of rotation in radians.
     */
    public void rotateVertical(double angle) {
        Point3D canvasNormal = calculateCanvasNormal();
        Point3D rotAxis = upDirection.crossProduct(canvasNormal);
        Transform3D transform = Transform3D.rotateCCWAroundAxis(cameraPoint, cameraPoint.add(rotAxis), angle);

        // Attach upDirection to lookingAt before rotation
        upDirection = upDirection.add(lookingAt);
        lookingAt = transform.transform(lookingAt);
        upDirection = transform.transform(upDirection);
        upDirection = upDirection.subtract(lookingAt);
        refreshCanvas();
    }

    /**
     * Moves the observer forward (towards the direction in which they're looking).
     * @param amount The amount of displacement in world coordinates
     */
    public void moveForward(double amount) {
        Point3D displacement = calculateCanvasNormal().normalize().multiply(amount);
        lookingAt = lookingAt.add(displacement);
        cameraPoint = cameraPoint.add(displacement);
        refreshCanvas();
    }

    /**
     * Moves the observer backward (in the opposite direction from the one in which they're looking).
     * @param amount The amount of displacement in world coordinates
     */
    public void moveBackward(double amount) {
        Point3D displacement = calculateCanvasNormal().normalize().multiply(-amount);
        lookingAt = lookingAt.add(displacement);
        cameraPoint = cameraPoint.add(displacement);
        refreshCanvas();
    }

    /**
     * Moves the observer to the left.
     * @param amount The amount of displacement in world coordinates
     */
    public void moveLeft(double amount) {
        Point3D displacement = canvas.getHorizontalVector().multiply(-amount);
        lookingAt = lookingAt.add(displacement);
        cameraPoint = cameraPoint.add(displacement);
        refreshCanvas();
    }

    /**
     * Moves the observer to the right.
     * @param amount The amount of displacement in world coordinates
     */
    public void moveRight(double amount) {
        Point3D displacement = canvas.getHorizontalVector().multiply(amount);
        lookingAt = lookingAt.add(displacement);
        cameraPoint = cameraPoint.add(displacement);
        refreshCanvas();
    }
}

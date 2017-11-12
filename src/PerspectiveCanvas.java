import javafx.geometry.Point3D;

/**
 * Perspective canvas is the 2-dimensional canvas inside a 3-dimensional world, used for projecting objects onto a
 * two dimensional surface.
 */
public class PerspectiveCanvas {
    private Point3D center;
    private Point3D rightDirection;
    private Point3D upDirection;
    private double width, height;

    /**
     * @param cameraPoint Location of the camera/eye of the observer
     * @param lookingAt The point towards which the observer is oriented
     * @param upDir The upwards direction to properly orient the perspective plane. This vector will be projected onto
     *              the perspective plane, therefore it does not need to be necessarily parallel to the perspective
     *              plane
     * @param horizontalAngleOfView The angle of view of the resultant perspective plane. Needs to be provided in
     *                              radians
     * @param aspectRatio the ratio of the canvas's width to its height
     */
    public PerspectiveCanvas(Point3D cameraPoint, Point3D lookingAt, Point3D upDir, double horizontalAngleOfView, double aspectRatio) {
        center = new Point3D(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());

        Point3D canvasNormal = lookingAt.subtract(cameraPoint);
        upDirection = upDir;
        rightDirection = canvasNormal.crossProduct(upDirection).normalize();
        width = 2 * Math.tan(horizontalAngleOfView * 0.5) * canvasNormal.magnitude();
        height = width/aspectRatio;
    }

    /**
     * Returns a point in the 3D world given coordinates on the 2D canvas. Note that the coordinates of the canvas are
     * scales so that 0 means left-most (or bottom-most) and 1 means right-most (or top-most).
     * @param xRatioCoord A number from 0 to 1 representing the x coordinate on the canvas
     * @param yRatioCoord A number from 0 to 1 representing the y coordinate on the canvas
     * @return The location of the specified point on the canvas in the 3D world coordinates.
     */
    public Point3D getWorldPointFromCanvasCoord(double xRatioCoord, double yRatioCoord) {
        Point3D xDisplacement = rightDirection.multiply((xRatioCoord - 0.5) * width);
        Point3D yDisplacement = upDirection.multiply((yRatioCoord - 0.5) * height);
        return center.add(xDisplacement).add(yDisplacement);
    }

    /**
     * @return The horizontal vector parallel to the top and bottom edges of the canvas
     */
    public Point3D getHorizontalVector() {
        return new Point3D(rightDirection.getX(), rightDirection.getY(), rightDirection.getZ());
    }
}

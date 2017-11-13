import javafx.geometry.Point3D;

import java.awt.*;
import java.awt.geom.Point2D;

public class RectFace implements Shape3D {
    private Point3D topLeft;
    private Point3D down;
    private Point3D right;
    private Point3D normal;
    private double width, height;

    public RectFace(Point3D topLeft, Point3D topRight, Point3D bottomLeft) {
        this.right = topRight.subtract(topLeft);
        this.width = right.magnitude();

        this.down = bottomLeft.subtract(topLeft);
        this.height = down.magnitude();

        this.normal = this.down.crossProduct(this.right).normalize();
        this.topLeft = topLeft;
    }

    private Point2D.Double getPointInLocalCoordinates(Point3D pointOnFace) {
        Point3D vectorFromTopLeft = pointOnFace.subtract(topLeft);

        Point3D xOrientedComponent = GeometryHelpers.projectVectorOntoVector(vectorFromTopLeft, right);
        Point3D yOrientedComponent = vectorFromTopLeft.subtract(xOrientedComponent);

        double xCoordinate, yCoordinate;

        if (right.getX() != 0) {
            xCoordinate = xOrientedComponent.getX() / right.getX() * width;
        } else if (right.getY() != 0) {
            xCoordinate = xOrientedComponent.getY() / right.getY() * width;
        } else {
            xCoordinate = xOrientedComponent.getZ() / right.getZ() * width;
        }

        if (down.getX() != 0) {
            yCoordinate = yOrientedComponent.getX() / down.getX() * height;
        } else if (down.getY() != 0) {
            yCoordinate = yOrientedComponent.getY() / down.getY() * height;
        } else {
            yCoordinate = yOrientedComponent.getZ() / down.getZ() * height;
        }

        return new Point2D.Double(xCoordinate, yCoordinate);
    }

    @Override
    public IntersectionPoint castRay(Ray ray) {
        double t = (topLeft.dotProduct(normal) - ray.origin.dotProduct(normal))/ray.unitDirection.dotProduct(normal);
        if (t < 0) {
            return null;
        }
        Point3D pointOfIntersection = ray.getPointAtDistance(t);

        Point2D.Double localCoords = getPointInLocalCoordinates(pointOfIntersection);
        if (localCoords.x < 0 || localCoords.x > width || localCoords.y < 0 || localCoords.y > height) {
            return null;
        }

        boolean hitFromInside = normal.dotProduct(ray.unitDirection) < 0;
        return new IntersectionPoint(pointOfIntersection, hitFromInside, this);
    }

    @Override
    public Point3D getNormalAtPoint(Point3D pointInShape) {
        return normal;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Point3D getWorldPointAt(double x, double y) {
        return topLeft.add(right.multiply(x)).add(down.multiply(y));
    }

    public static class FaceSolid extends Solid {
        RectFace face;

        public FaceSolid(RectFace face, Material material) {
            super(face, material);
            this.face = face;
        }

        @Override
        public LightIntensity getDiffuseReflectivityAtPoint(Point3D p) {
            LightIntensity intensity = getMaterial().diffuseReflectivity;

            if (getMaterial().texture != null) {
                Point2D localCoordsPoint = face.getPointInLocalCoordinates(p);
                int xPixel = (int) Math.round(localCoordsPoint.getX() / face.width * getMaterial().texture.getWidth());
                int yPixel = (int) Math.round(localCoordsPoint.getY() / face.height * getMaterial().texture.getHeight());
                if (xPixel < 0) {
                    xPixel = 0;
                }
                if (xPixel >= getMaterial().texture.getWidth()) {
                    xPixel = getMaterial().texture.getWidth() - 1;
                }

                if (yPixel < 0) {
                    yPixel = 0;
                }
                if (yPixel >= getMaterial().texture.getHeight()) {
                    yPixel = getMaterial().texture.getHeight() - 1;
                }

                LightIntensity textureIntensity = new LightIntensity();
                textureIntensity.setColor(new Color(getMaterial().texture.getRGB(xPixel, yPixel)));

                intensity = intensity.multiply(textureIntensity);
            }

            return intensity;
        }
    }
}

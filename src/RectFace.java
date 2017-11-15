import javafx.geometry.Point3D;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * RectFace is the class used to define the contained planes that can be used for the Cornell Box or a compose any object
 * that can be composed of several planes or boxes. These rectangular faces are treated as planes in the mathematical calculations
 * needed for Ray Tracing purposes. As our scene can contain many objects, it is crucial to be able to differentiate every different
 * plane out of the multiple objects that may be placed in the scene, which belongs to our Shape Interface.
 * @author Pietro
 */
public class RectFace implements Shape3D {
    private Point3D topLeft;
    private Point3D down;
    private Point3D right;
    private Point3D normal;
    private double width, height;

    /**
     * With rendering in mind, we want to describe a rectangular plane with the least amount of computations possible. By defining only
     * 3 vectors we are able to derive the width and height dimensions alongside the normal to the contained rectangular face,
     * thus book-keeping all of the information necessary for ray interactions and shading them in the future.
     * Given an arbitrary rectangular plane,
     * @param topLeft The top left vertex of the rectangular face
     * @param topRight The top right vertex of the rectangular face
     * @param bottomLeft The bottom left vertex of the rectangular face
     */
    public RectFace(Point3D topLeft, Point3D topRight, Point3D bottomLeft) {
        this.right = topRight.subtract(topLeft);
        this.width = right.magnitude();

        this.down = bottomLeft.subtract(topLeft);
        this.height = down.magnitude();

        this.normal = this.down.crossProduct(this.right).normalize();
        this.topLeft = topLeft;
    }

    /**
     * Obtains a point relative to the positioning of a specific rectangular face.
     * @param pointOnFace an arbitrary point that contained by the face
     * @return Point2D containing conversion of pointOnFace parameter relative to the origin of the plane.
     */
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

    /**
     * castRay is a method that will emulate the casting a light ray onto this particular rectangular face and calculate
     * the Intersection Point if the ray intercepts with the object and a boolean depending on the side the ray came from.
     * @param ray A ray object that is defined given a point of origin and a unit direction vector.
     * @return IntersectionPoint data type specified in the Shape3D interface.
     */
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

    /**
     * getNormalAtPoint obtains the normal vector to any provided point inside the face, as part of the Shape3D
     * interface as we want all normals for any object in the shading scene.
     * @param pointInShape an arbitrary point contained in the rectangular face.
     * @return The normal vector for that given point.
     */
    @Override
    public Point3D getNormalAtPoint(Point3D pointInShape) {
        return normal;
    }

    //Helper Methods:
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Point3D getWorldPointAt(double x, double y) {
        return topLeft.add(right.multiply(x)).add(down.multiply(y));
    }

    /**
     * The FaceSolid class inherits from the Solid abstract interface for the purpose of handling different material diffusion
     * reflectivity depending on the texture that can be set to any particular face of any rectangular object in our shading scene.
     */
    public static class FaceSolid extends Solid {
        RectFace face;

        public FaceSolid(RectFace face, Material material) {
            super(face, material);
            this.face = face;
        }

        /**
         * DiffuseReflexivity takes into account the material the face is made out of in our scene, as it would have different
         * ray interactions, and thus requiring different treatment when shading.
         * @param p a given 3D point inside the face to be shaded.
         * @return The specific diffuseIntensity shading component for a given pixel p.
         */
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

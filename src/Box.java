import javafx.geometry.Point3D;

/**
 * Box is the class that utilizes the RectFace class objects in order to populate the background and surroundings of our scenes.
 * Our scene is often referred to as the "Cornell Box", in where a simple rectangular box is set up with a square planar light source
 * coming directly overhead.
 * @author Pietro
 */
public class Box {
    public RectFace front;
    public RectFace back;
    public RectFace left;
    public RectFace right;
    public RectFace top;
    public RectFace bottom;

    /**
     * A basic box can built from the vertices that are described by the following vectors, which can derive the remaining
     * necessary vectors when initializing the box.
     * @param topLeftFront The vector that maps to a vertex with a negative x, positive y and positive z components.
     * @param topLeftBack The vector that maps to a vertex with a negative x, positive y and negative z components.
     * @param topRightFront The vector that maps to a vertex with a positive x, positive y, and positive z components.
     * @param bottomLeftFront The vector that maps to a vertex with a negative x, negative y, and positive z component.
     */
    public Box(Point3D topLeftFront, Point3D topLeftBack, Point3D topRightFront, Point3D bottomLeftFront) {
        Point3D topRightBack = topLeftBack.add(topRightFront.subtract(topLeftFront));
        Point3D bottomRightFront = bottomLeftFront.add(topRightFront.subtract(topLeftFront));
        Point3D bottomRightBack = bottomRightFront.add(topLeftBack.subtract(topLeftFront));
        Point3D bottomLeftBack = bottomLeftFront.add(topLeftBack.subtract(topLeftFront));

        //Given these 8 vectors, we can now create 6 faces for the box.
        front = new RectFace(topLeftFront, topRightFront, bottomLeftFront);
        back = new RectFace(topRightBack, topLeftBack, bottomRightBack);
        left = new RectFace(topLeftBack, topLeftFront, bottomLeftBack);
        right = new RectFace(topRightFront, topRightBack, bottomRightFront);
        top = new RectFace(topLeftBack, topRightBack, topLeftFront);
        bottom = new RectFace(bottomLeftFront, bottomRightFront, bottomLeftBack);
    }

    //Helper function that returns an array that contains all of the Rectangle Faces.
    public RectFace[] getFaceList() {
        RectFace[] result = new RectFace[] {
          front, back, left, right, top, bottom
        };
        return result;
    }
}

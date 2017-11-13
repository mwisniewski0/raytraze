import javafx.geometry.Point3D;

public class Box {
    public RectFace front;
    public RectFace back;
    public RectFace left;
    public RectFace right;
    public RectFace top;
    public RectFace bottom;

    public Box(Point3D topLeftFront, Point3D topLeftBack, Point3D topRightFront, Point3D bottomLeftFront) {
        Point3D topRightBack = topLeftBack.add(topRightFront.subtract(topLeftFront));
        Point3D bottomRightFront = bottomLeftFront.add(topRightFront.subtract(topLeftFront));
        Point3D bottomRightBack = bottomRightFront.add(topLeftBack.subtract(topLeftFront));
        Point3D bottomLeftBack = bottomLeftFront.add(topLeftBack.subtract(topLeftFront));

        front = new RectFace(topLeftFront, topRightFront, bottomLeftFront);
        back = new RectFace(topRightBack, topLeftBack, bottomRightBack);
        left = new RectFace(topLeftBack, topLeftFront, bottomLeftBack);
        right = new RectFace(topRightFront, topRightBack, bottomRightFront);
        top = new RectFace(topLeftBack, topRightBack, topLeftFront);
        bottom = new RectFace(bottomLeftFront, bottomRightFront, bottomLeftBack);
    }

    public RectFace[] getFaceList() {
        RectFace[] result = new RectFace[] {
          front, back, left, right, top, bottom
        };
        return result;
    }
}

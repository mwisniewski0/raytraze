import javafx.geometry.Point3D;

public class Transform3D {
    double matrix[][] = new double[4][4];

    private double getAffineVectorCoord(Point3D vector, int index) {
        switch (index) {
            case 0:
                return vector.getX();
            case 1:
                return vector.getY();
            case 2:
                return vector.getZ();
            case 3:
                return 1.0;
        }
        throw new IllegalArgumentException("Index out of bounds");
    }

    /**
     * Transforms the provided point using this matrix. Note that the passed point will not be modified.
     * @param toTransform The point to transform
     * @return The point after the transformation
     */
    public Point3D transform(Point3D toTransform) {
        double[] affineCoords = new double[4];
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                affineCoords[y] += matrix[x][y] * getAffineVectorCoord(toTransform, x);
            }
        }
        return new Point3D(
                affineCoords[0]/affineCoords[3],
                affineCoords[1]/affineCoords[3],
                affineCoords[2]/affineCoords[3]
        );
    }

    /**
     * Builds a Transform3D that rotates around the specified axis counter clock-wise by the provided angle.
     * @param axisStart A point on the desired axis of rotation.
     * @param axisEnd A point on the desired axis of rotation that is different from axisStart
     * @param angle The angle of the rotation. The input will be treated as if it was in radians.
     * @return The rotation matrix
     */
    public static Transform3D rotateCCWAroundAxis(Point3D axisStart, Point3D axisEnd, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // Unit vector from origin
        Point3D a = axisEnd.subtract(axisStart).normalize();

        // According to the book
        Transform3D result = new Transform3D();
        result.matrix = new double[][] {
                {cos + (1-cos)*a.getX()*a.getX(), (1 - cos)*a.getX()*a.getY() - sin * a.getZ(), (1-cos)*a.getX()*a.getZ() + sin*a.getY(), 0},
                {(1-cos)*a.getX()*a.getY() + sin*a.getZ(), cos + (1-cos)*a.getY()*a.getY(), (1-cos)*a.getY()*a.getZ() - sin*a.getX(), 0},
                {(1-cos)*a.getX()*a.getZ() - sin*a.getY(), (1-cos)*a.getY()*a.getZ() + sin*a.getX(), cos + (1-cos)*a.getZ()*a.getZ(), 0},
                {0,0,0,1},
        };

        // If the line is not going through the origin, we need to first translate to the origin, and then translate
        // back
        result.concatenate(Transform3D.getTranslateInstance(axisStart.multiply(-1)));
        result.preConcatenate(Transform3D.getTranslateInstance(axisStart));
        return result;
    }

    /**
     * Multiplies this matrix by the provided matrix on the right. The result will be saved in this matrix.
     * @param other The matrix to multiply with
     */
    public void concatenate(Transform3D other) {
        this.matrix = this.multiply(other).matrix;
    }

    /**
     * Multiplies this matrix by the provided matrix on the left. The result will be saved in this matrix.
     * @param other The matrix to multiply with
     */
    public void preConcatenate(Transform3D other) {
        this.matrix = other.multiply(this).matrix;
    }

    /**
     * Multiplies this matrix by the provided matrix on the right. Neither this nor the provided matrix will be
     * modified
     * @param other The matrix to multiply with
     * @return The multiplied matrix
     */
    public Transform3D multiply(Transform3D other) {
        Transform3D result = new Transform3D();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 4; ++i) {
                    result.matrix[x][y] += this.matrix[i][y] * other.matrix[x][i];
                }
            }
        }
        return result;
    }

    /**
     * @param translation
     * @return
     */
    public static Transform3D getTranslateInstance(Point3D translation) {
        Transform3D result = new Transform3D();
        result.matrix[0][0] = 1;
        result.matrix[3][0] = translation.getX();

        result.matrix[1][1] = 1;
        result.matrix[3][1] = translation.getY();

        result.matrix[2][2] = 1;
        result.matrix[3][2] = translation.getZ();

        result.matrix[3][3] = 1;
        return result;
    }

    /**
     * Creates a new identity matrix
     * @return The identity matrix
     */
    public static Transform3D getIdentityMatrix() {
        Transform3D result = new Transform3D();
        result.matrix[0][0] = 1.0;
        result.matrix[1][1] = 1.0;
        result.matrix[2][2] = 1.0;
        result.matrix[3][3] = 1.0;
        return result;
    }

    /**
     * Creates a transform that scales points by the provided ratios
     * @param scaleX The scale factor in the x-direction
     * @param scaleY The scale factor in the y-direction
     * @param scaleZ The scale factor in the z-direction
     * @return A transform that scales points by the provided ratios
     */
    public static Transform3D getScaleInstance(double scaleX, double scaleY, double scaleZ) {
        Transform3D result = new Transform3D();
        result.matrix[0][0] = scaleX;
        result.matrix[1][1] = scaleY;
        result.matrix[2][2] = scaleZ;
        result.matrix[3][3] = 1.0;
        return result;
    }
}

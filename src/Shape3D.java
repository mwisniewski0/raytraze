import javafx.geometry.Point3D;

/**
 * Shape3D is a design choice taken when tasked with making a shading model with different shapes and multiple
 * objects contained in a specific scene. Given our goal to give modularity to push our lighting model as far
 * as it can possibly go, we needed to be able to model different behaviours in any shape in our scene.
 *
 * The main information that is shared in common by all the objects in the scene is the ray intersection data
 * calculations (stored in the IntersectionData class) in order to draw the scene and normalPoints per pixel
 * for the shading model purposes.
 * @author Pietro
 */
interface Shape3D {
    IntersectionData castRay(Ray ray);
    Point3D getNormalAtPoint(Point3D pointInShape);
}


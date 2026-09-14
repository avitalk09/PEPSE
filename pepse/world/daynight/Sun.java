package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/** Class containing just one single static function used to create a gram object representing the sun in our
 *  simulator. */
public class Sun {

    /** Tag to set the sun with*/
    private static final String SUN_TAG = "sun";
    /** Constant for the size of the sun. */
    private static final float SUN_SIZE = 60;
    /** Height at which the ground will approximately be (starts at 2/3 of screen so it will be bottom 1/3) */
    private static final float GROUND_START_AT = 2f/3f;
    /** To avoid non const numbers */
    private static final float HALF = 0.5f;
    private static final float SUN_START_AT_HEIGHT = 60f;
    // for the transition
    private static final float INITIAL_VALUE = 0f;
    private static final float FINAL_VALUE = 360f;

    /**
     * Creates and return a game object representing a sun for our simulator. It moves in a circle around
     * groundHeightAtX0.
     * @param windowDimensions the size of the window
     * @param cycleLength the time the transition takes
     * @return the sun object we created
     */
    public static GameObject create (Vector2 windowDimensions, float cycleLength) {
        float groundHeightAtX0 = windowDimensions.y() * GROUND_START_AT;
        Vector2 initialCenter = new Vector2(windowDimensions.x()*HALF, SUN_START_AT_HEIGHT);
        // create sun object
        GameObject sun = new GameObject(initialCenter, new Vector2(SUN_SIZE,SUN_SIZE),
                new OvalRenderable(Color.YELLOW));
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);
        // values for transition
        Vector2 initialSunCenter = sun.getCenter();
        Vector2 cycleCenter = new Vector2(windowDimensions.x()*HALF, groundHeightAtX0);
        // add transition
        new Transition<Float>(
                sun,
                (Float angle)->sun.setCenter
                        (initialSunCenter.
                        subtract(cycleCenter).
                        rotated(angle).
                        add(cycleCenter)),
                INITIAL_VALUE,
                FINAL_VALUE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null);

        return sun;
    }
}

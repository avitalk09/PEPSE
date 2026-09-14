package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/** Class containing a single static function that creates a game object that mimics the transition from day
 *  to night and back in a cycle */
public class Night {

    /** Tag to set the night object with*/
    private static final String NIGHT_TAG = "night";
    /** Opacity for the darkest time f the cycle. */
    public static final Float MIDNIGHT_OPACITY = 0.5f;
    /** For the transition */
    private static final float INITIAL_VALUE = 0f;
    /** To avoid non const numbers */
    private static final float HALF = 0.5f;

    /**
     * Creates a game object that changes back and forth from invisible to dark (using MIDNIGHT_OPACITY as
     * the darkest it gets), it mimics the change from day to night in the simulator.
     * @param windowDimensions the size of the window
     * @param cycleLength the time the transition takes
     * @return the night game object we created
     */
    public static GameObject create (Vector2 windowDimensions, float cycleLength){
        // create the object as a big black
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions,
                new RectangleRenderable(Color.BLACK));

        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        // Add the transition effect
        new Transition<Float>(
                night,
                night.renderer()::setOpaqueness,
                INITIAL_VALUE,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength*HALF,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);

        return night;
    }
}

package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;


/** Class containing a single static function to create a game object representing the sky. */
public class Sky {

    /** Color used for the sky */
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");
    /** Tag to set the object with */
    private static final String SKY_TAG  = "sky";

    /**
     * Creates and returns a game object representing the sky.
     * @param windowDimensions the size of the screen
     * @return The sky game object we created
     */
    public static GameObject create (Vector2 windowDimensions){
        GameObject sky = new GameObject(Vector2.ZERO, windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag(SKY_TAG);
        return sky;
    }
}

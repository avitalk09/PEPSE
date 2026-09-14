package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/** Class containing a single static function to create an object that represent the halo of the sun for our
 *  simulator. It follows the movement of the sun and is a translucid yellow. */
public class SunHalo {

    /** Tag to set the halo with*/
    private static final String HALO_TAG = "halo";
    /** Color of the halo */
    private static final Color HALO_COLOR = new Color(255,255,0,20);
    /** Size of the halo */
    private static final float HALO_SIZE = 100;

    /**
     * Creates and returns a game object that mimics a halo of the sun, it follows it around.
     * @param sun the sun object we need to follow
     * @return the halo game object we created
     */
    public static GameObject create(GameObject sun) {

        GameObject halo = new GameObject(sun.getTopLeftCorner(), new Vector2(HALO_SIZE,HALO_SIZE),
                new OvalRenderable(HALO_COLOR));

        halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        halo.setTag(HALO_TAG);

        halo.addComponent(deltaTime -> halo.setCenter(sun.getCenter()));

        return halo;
    }
}

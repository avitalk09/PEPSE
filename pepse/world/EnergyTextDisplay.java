package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;


/** Graphic class, used to add a display of the energy the avatar from our world has left, gets updated as
 *  the simulation goes. */
public class EnergyTextDisplay {

    /** Size for the text */
    private static final Vector2 TEXT_SIZE = new Vector2(50, 50);
    /** Max energy, to start with. */
    private static final int MAX_ENERGY = 100;
    /** Energy sign (to avoid non const strings). */
    private static final String ENERGY_SIGN = "%";
    /** The game object used for displaying the percentage of energy*/
    private final GameObject energyDisplay;
    /** The text renderable used inside the gameObject, holds the percentage */
    private final TextRenderable textRenderable;


    /**
     * Creates a game object that is used to render the current energy of the avatar in our world at all time
     * @param topLeftCorner the position for the top left corner of the object
     */
    public EnergyTextDisplay(Vector2 topLeftCorner){
        this.textRenderable = new TextRenderable(MAX_ENERGY+ENERGY_SIGN);
        this.textRenderable.setColor(Color.BLACK);

        this.energyDisplay = new GameObject(topLeftCorner, TEXT_SIZE, this.textRenderable);
        this.energyDisplay.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
    }

    /**
     * Updates the text renderer of the energy.
     * @param energy the number to set the renderer to
     */
    public void updateEnergy(int energy){
        this.textRenderable.setString(energy+ ENERGY_SIGN);
    }

    /**
     * @return the game object from our field
     */
    public GameObject getEnergyDisplay(){
        return this.energyDisplay;
    }
}

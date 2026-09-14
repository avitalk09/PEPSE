package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/** A block object is a game object that cannot move and cannot be passed through*/
public class Block extends GameObject {

    /** Size of a single block on screen */
    public static final int SIZE = 30;

    /**
     * Creates a block object, which is a game object, the block is immovable and cannot be passed through
     * @param topLeftCorner the place to put the top left of the object
     * @param renderable the image to render the block as
     */
    public Block (Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }

}

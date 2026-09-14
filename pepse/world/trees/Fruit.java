package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.avatar.Avatar;

import java.awt.*;

/** Game object representing fruits the avatar can eat, they respawn one cycle after being eaten. */
public class Fruit extends GameObject {

    /** TAGS */
    public static final String FRUIT_TAG = "fruit";
    /** Tag for when a fruit is unavailable */
    public static final String UNAVAILABLE_FRUIT_TAG = "unavailableFruit";
    /** Size of a fruit (needs to be smaller than a block) */
    private static final Vector2 FRUIT_SIZE = new Vector2(Block.SIZE*(0.75f), Block.SIZE*(0.75f));
    /** Color of the fruit. */
    private static final Color FRUIT_COLOR = new Color(193, 28, 28);
    /** How long it takes before the fruit reappears. */
    private static final float RESPAWN_TIME = 30f;
    /** Show the state of the fruit (eaten in the last 30s or not)*/
    private boolean available;

    /**
     * Create a fruit object that is available for the avatar to eat
     * @param topLeftCorner Where to place the fruit
     */
    public Fruit(Vector2 topLeftCorner) {
        super(topLeftCorner, FRUIT_SIZE, new OvalRenderable(FRUIT_COLOR));
        this.setTag(FRUIT_TAG);
        this.available = true;
    }

    /**
     * @param other The GameObject with which a collision occurred.
     * @param collision Information regarding this collision.
     *                  A reasonable elastic behavior can be achieved with:
     *                  setVelocity(getVelocity().flipped(collision.getNormal()));
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if(!this.available) { return; }
        if(!other.getTag().equals(Avatar.AVATAR_TAG)) { return; }

        makeUnavailable();
    }

    /**
     * Makes a fruit unavailable for 30 seconds after being eaten, it makes it invisible and calls
     *  makeAvailable after a full cycle (set its tag to a different one to make a difference).
     */
    private void makeUnavailable() {
        this.available = false;
        renderer().setRenderable(null);
        setTag(UNAVAILABLE_FRUIT_TAG);
        // after RESPAWN_TIME it will run makeAvailable
        new ScheduledTask(this,
                RESPAWN_TIME,
                false,
                this::makeAvailable);
    }

    /**
     * Makes a fruit available after being eaten, reset its tag to the original one and add it's renderable
     *  back again.
     */
    private void makeAvailable() {
        this.available = true;
        renderer().setRenderable(new OvalRenderable(FRUIT_COLOR));
        setTag(FRUIT_TAG);
    }
}


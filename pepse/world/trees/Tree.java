package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.world.Block;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.util.Random;

/** Class used to create trees. contains one public method 'createTree' that returns a list of blocks
 *  representing the tree. */
public class Tree {

    /** TAGS */
    public static final String TRUNK_TAG = "trunk";
    /** tag for leaves. */
    public static final String LEAF_TAG = "leaf";

    /** Minimum and maximum number of blocks height a tree trunk can be. */
    private static final int MIN_TRUNK_HEIGHT = 3;
    private static final int MAX_TRUNK_HEIGHT = 6;

    /** Max number of rows and columns of leaves there can be on a tree (centered on the trunk).*/
    private static final int MAX_LEAVES_ROWS = 5;
    private static final int MAX_LEAVES_COLUMNS = 5;

    /** Probability to add or not a leaf/fruit*/
    private static final float LEAF_PROBABILITY = 0.6f;
    private static final float FRUIT_PROBABILITY = 0.2f;

    /** Base color for trunk and leaves (use ColorSupplier.approximateColor on them). */
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final Color LEAF_COLOR = new Color(50, 200, 30);

    /** Constant for the wind animation of leaves. */
    private static final float WIND_MIN_DELAY = 0.15f;
    private static final float WIND_MAX_DELAY = 0.8f;

    private static final float WIND_MIN_ANGLE = 6f;
    private static final float WIND_MAX_ANGLE = 14f;

    private static final float WIND_MIN_SCALE = 0.95f;
    private static final float WIND_MAX_SCALE = 1.08f;

    private static final float WIND_MIN_DURATION = 2;
    private static final float WIND_MAX_DURATION = 4f;

    /** To avoid non const numbers */
    private static final int ONE = 1;

    /**
     * Create a tree (blocks in form of a tree) at x, just on top of the ground
     * @param x The coordinate to place the tree at
     * @param groundHeightAtX The height of the ground at this x coordinate
     * @param rand A Random instance use for the randomization of tree height and number of leaves
     * @return A list of blocks representing the trunk and leaves of the tree we created
     */
    public static List<GameObject> createTree(int x, float groundHeightAtX, Random rand){
        List<GameObject> treeObjects = new ArrayList<>();
        // decide the height of the trunk
        int trunkHeight = rand.nextInt(MAX_TRUNK_HEIGHT-MIN_TRUNK_HEIGHT+ONE) + MIN_TRUNK_HEIGHT;
        // create the trunk and leaves and add them to the list
        treeObjects.addAll(createTrunk(x, groundHeightAtX, trunkHeight));
        treeObjects.addAll(createLeaves(x, groundHeightAtX,trunkHeight, rand));

        return treeObjects;
    }

    /**
     * Creates blocks for the trunk of the tree
     * @param x The coordinate to place the trunk at
     * @param groundHeightAtX The height of the ground at this x coordinate
     * @param trunkHeight The number of block the trunk should be
     * @return The blocks creating the trunk
     */
    private static List<Block> createTrunk(int x, float groundHeightAtX, int trunkHeight){
        // create empty list
        List<Block> blocks = new ArrayList<>();
        // approximate the color for the trunk
        Color trunkColor = ColorSupplier.approximateColor(TRUNK_COLOR);
        // create blocks for the tree starting at the ground
        for(int i=0; i< trunkHeight; i++){
            // height of next block
            float y = groundHeightAtX - Block.SIZE*(i+ONE);
            // create and add ti the list
            Block block = new Block(new Vector2(x,y), new RectangleRenderable(trunkColor));
            block.setTag(TRUNK_TAG);
            blocks.add(block);
        }
        // return the list representing the tree
        return blocks;
    }

    /**
     * Creates blocks representing the leaves of the tree at x
     * @param x The coordinate of the tree the leaves will sit on
     * @param trunkHeight The height of the trunk of the tree
     * @param rand Random instance use for randomization of the leaves
     * @return  The blocks creating the leaves
     */
    private static List<GameObject> createLeaves(int x, float groundHeightAtX, int trunkHeight, Random rand){
        List<GameObject> leavesAndFruits = new ArrayList<>();
        float topOfTrunk = groundHeightAtX - Block.SIZE*(trunkHeight);
        int colsOfLeavesPerSide = MAX_LEAVES_COLUMNS/2;

        for(int row=0; row<MAX_LEAVES_ROWS; row++){
            for(int col= -colsOfLeavesPerSide; col<=colsOfLeavesPerSide; col++){
                if(rand.nextFloat() < LEAF_PROBABILITY){
                    float leafX = x +col*Block.SIZE;
                    float leafY = topOfTrunk - row*Block.SIZE;
                    Block leaf = new Block(new Vector2(leafX, leafY),
                            new RectangleRenderable(ColorSupplier.approximateColor(LEAF_COLOR)));
                    leaf.setTag(LEAF_TAG);
                    addWindEffect(leaf, rand);
                    leavesAndFruits.add(leaf);
                    if(rand.nextFloat() < FRUIT_PROBABILITY){
                        leavesAndFruits.add(new Fruit(leaf.getTopLeftCorner()));
                    }
                }
            }
        }
        return leavesAndFruits;
    }

    /**
     * Add the wind effect to a leaf
     * @param leaf leaf to add to
     * @param rand Random instance to use
     */
    private static void addWindEffect(Block leaf, Random rand){
        float delay = randomRange(rand, WIND_MIN_DELAY, WIND_MAX_DELAY);
        float angle = randomRange(rand, WIND_MIN_ANGLE, WIND_MAX_ANGLE);
        float scale = randomRange(rand, WIND_MIN_SCALE, WIND_MAX_SCALE);
        float duration = randomRange(rand, WIND_MIN_DURATION, WIND_MAX_DURATION);
        Vector2 originalSize = leaf.getDimensions();

        new ScheduledTask(leaf,
                delay,
                false,
                ()->applyMotion(leaf, angle, scale, duration, originalSize));

    }

    /**
     * @param rand Instance of Random to use
     * @param min minimum number for the range
     * @param max maximum number for the range
     * @return Return a random float between min and max (including)
     */
    private static float randomRange(Random rand, float min, float max) {
        return min + rand.nextFloat() * (max - min);
    }

    /**
     * Transitions to insert in the ScheduledTask for the wind effect
     * @param leaf to add the transition to
     * @param angle of the sway
     * @param scale the change in the size of the leaf during the transition
     * @param duration of the transition
     * @param originalSize size of the leaf before any shrinkage
     */
    private static void applyMotion(Block leaf, float angle, float scale, float duration,
                                    Vector2 originalSize) {
        new Transition<>(
                leaf,
                leaf.renderer()::setRenderableAngle,
                -angle,
                angle,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                duration,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
        new Transition<>(
                leaf,
                leaf::setDimensions,
                originalSize,
                originalSize.mult(scale),
                (a, b, t) -> a.mult(ONE - t).add(b.mult(t)),
                duration,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }

}


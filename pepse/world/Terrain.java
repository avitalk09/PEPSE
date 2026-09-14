package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Class used to create the ground for the simulator */
public class Terrain {

    /** Tag to set the terrain blocks with*/
    public static final String GROUND_TAG = "ground";
    /** Color used for the ground */
    private static final Color BASE_GROUND_COLOR = new Color( 212,123, 74);
    /** Number of blocks per column */
    private static final int TERRAIN_DEPTH = 20;
    /** Height at which the ground will approximately be (starts at 2/3 of screen so it will be bottom 1/3) */
    private static final float GROUND_START_AT = 2f/3f;
    /** Used in noise generator to create smooth ground*/
    private static final float MULTIPLICATOR_FOR_NOISE = 7;
    /** The height of the ground at x=0 */
    private final float groundHeightAtX0;
    /** Used for creating random heights for the blocks */
    private final NoiseGenerator noiseGenerator;


    /**
     * Constructor for the terrain, initialize the seed and noise generator.
     * @param windowDimensions the size of the screen
     * @param seed used for making the random choices
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_START_AT;
        // TODO: check this line
        this.noiseGenerator = new NoiseGenerator(seed, (int) this.groundHeightAtX0);

    }

    /**
     * Chooses a random height for the ground at a given coordinate
     * @param x the coordinate we want to add ground at
     * @return Return the height of the ground wanted at the coordinate x.
     */
    public  float groundHeightAt(float x) {
        float noise = (float) noiseGenerator.noise(x, Block.SIZE * MULTIPLICATOR_FOR_NOISE);
        return groundHeightAtX0 + noise;
    }

    /**
     * Creates and returns a list of blocks representing the ground, covering the space from minX to maxX.
     * @param minX the first coordinate to cover
     * @param maxX th last coordinate to cover
     * @return list of blocks representing the ground
     */
    public List<Block> createInRange(int minX, int maxX){

        List<Block> blocks = new ArrayList<>();

        // calculate the coordinates to cover (to make the length a multiple of blocksize)
        int startX = (int) (Math.floor(minX / (double) Block.SIZE) * Block.SIZE);
        int endX = (int) (Math.ceil(maxX / (double) Block.SIZE) * Block.SIZE);

        for(int x=startX; x<=endX; x+=Block.SIZE){
            // get the height of ground wanted for this x, and the height where the first block will sit
            float groundHeight = groundHeightAt(x);
            int topHeight = (int)(Math.floor(groundHeight / (double) Block.SIZE) * Block.SIZE);
            // create 20 blocks
            for(int i=0; i<TERRAIN_DEPTH; i++){
                // calc the height of current block
                int y = topHeight + i*Block.SIZE;
                // create the block and add to out list
                Block block = new Block(new Vector2(x,y),
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
                block.setTag(GROUND_TAG);
                blocks.add(block);
            }
        }
        return blocks;
    }

}

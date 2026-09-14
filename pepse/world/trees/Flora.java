package pepse.world.trees;

import danogl.GameObject;
import pepse.world.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Flora class is a factory like class used for creating vegetation in our world */
public class Flora {

    /** Probability for each leaf to appear*/
    private static final float PROBABILITY_FOR_TREES = 0.1f;
    /** Random instance used for randomisation of trees, leaves and fruits */
    private final Random rand;
    /** Function to know the height of the ground at a coordinate */
    private final Function<Float, Float> groundHeightAt;

    /**
     * Constructor for the factory
     * @param groundHeightAt function from terrain
     * @param seed to use in the random
     */
    public Flora(Function<Float, Float> groundHeightAt, int seed) {
        rand = new Random(seed);
        this.groundHeightAt = groundHeightAt;
    }

    /**
     * Creates vegetation in the given range
     * @param minX minimum coordinate to cover
     * @param maxX maximum coordinate to cover
     * @return A list of game object representing the vegetation
     */
    public List<GameObject> createInRange(int minX, int maxX){
        List<GameObject> objects = new ArrayList<>();;
        int startAt = (int) (Math.floor(minX / (double) Block.SIZE) * Block.SIZE);

        for(int x=startAt; x<=maxX; x+=Block.SIZE){
            if(this.rand.nextFloat() < PROBABILITY_FOR_TREES){
                float groundHeightAtX = this.groundHeightAt.apply((float) x);
                float roundedHeight = (float)(Math.floor(groundHeightAtX / Block.SIZE) * Block.SIZE);
                List<GameObject> treeObjects = Tree.createTree(x,roundedHeight, this.rand);
                objects.addAll(treeObjects);
            }
        }
        return objects;
    }


}

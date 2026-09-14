package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.EnergyTextDisplay;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.trees.Fruit;
import pepse.world.trees.Tree;

import java.util.List;
/** Simulator manager, creates the world and manages it. */
public class PepseGameManager extends GameManager {

    /** Layers in which the sky and terrain will be placed. */
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int TERRAIN_LAYER = Layer.STATIC_OBJECTS;
    private static final int AVATAR_LAYER = Layer.DEFAULT;
    private static final int USER_LAYER = Layer.UI;
    /** Length of the cycle for day/night. */
    private static final int CYCLE_LENGTH = 30;
    /** Seed for the randomisation. */
    private static final int SEED = 8762;
    /** Placement for the displayer of the energy */
    private static final Vector2 ENERGY_PLACEMENT = new Vector2(10, 10);

    /** Numbers of screen (in length) to keep created ahead and behind the avatar */
    private static final int WORLD_SCREENS = 2;
    /** Coordinate to create the world between */
    private int loadMinx;
    private int loadMaxX;

    /** The avatar that runs in the simulator */
    private Avatar avatar;
    private Terrain terrain;
    private Flora flora;
    /** Dimensions of the screen*/
    private Vector2 dimensions;



    /** Initialize the simulator. Creates and adds all the items using other classes
     *
     * @param imageReader Contains a single method: readImage, which reads an image from disk.
     *                 See its documentation for help.
     * @param soundReader Contains a single method: readSound, which reads a wav file from
     *                    disk. See its documentation for help.
     * @param inputListener Contains a single method: isKeyPressed, which returns whether
     *                      a given key is currently pressed by the user or not. See its
     *                      documentation.
     * @param windowController Contains an array of helpful, self explanatory methods
     *                         concerning the window.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        // set parameters for the functions after
        this.dimensions = windowController.getWindowDimensions();
        int screenWidth =(int)  dimensions.x();
        this.loadMinx = -screenWidth*WORLD_SCREENS;
        this.loadMaxX = screenWidth*WORLD_SCREENS;

        // create terrain and flora objects
        this.terrain = new Terrain (dimensions, SEED);
        this.flora = new Flora(this.terrain::groundHeightAt, SEED);
        // call all creating functions
        addBackground(dimensions);
        addAvatar(dimensions, inputListener, imageReader);
        addEnergyDisplay();
        addGroundAndFlora(this.loadMinx, this.loadMaxX);

        Vector2 initialAvatarLocation = this.avatar.getTopLeftCorner();
        Vector2 dist =windowController.getWindowDimensions().mult(0.5f).subtract(initialAvatarLocation);
        setCamera(new Camera(this.avatar, dist, windowController.getWindowDimensions(),
                windowController.getWindowDimensions()));
    }

    /**
     * Adds the sky, sun and its halo and the night effect to the world.
     * @param dimensions the dimensions of the screen
     */
    private void addBackground(Vector2 dimensions){
        // Sky
        GameObject sky = Sky.create(dimensions);
        gameObjects().addGameObject(sky, SKY_LAYER);

        // Night
        GameObject night = Night.create(dimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(night, TERRAIN_LAYER);

        // Sun
        GameObject sun = Sun.create(dimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(sun, SKY_LAYER);

        // Sun Halo
        GameObject halo = SunHalo.create(sun);
        gameObjects().addGameObject(halo, SKY_LAYER);

    }

    /**
     * Adds the avatar to the world
     * @param dimensions the dimensions of the screen
     * @param inputListener object used to read from keyboard
     * @param imageReader object used to read images from paths
     */
    private void addAvatar(Vector2 dimensions, UserInputListener inputListener, ImageReader imageReader){
        float x = terrain.groundHeightAt(dimensions.x()*0.5f);
        float y = dimensions.y()*0.5f;
        Vector2 avatarLocation = new Vector2(x, y);
        this.avatar = new Avatar(avatarLocation, inputListener, imageReader);
        gameObjects().addGameObject(avatar, AVATAR_LAYER);
    }

    /**
     * Adds a text renderable to the screen that represent the energy of the avatar
     */
    private void addEnergyDisplay(){
        EnergyTextDisplay energyDisplay = new EnergyTextDisplay(ENERGY_PLACEMENT);
        gameObjects().addGameObject(energyDisplay.getEnergyDisplay(), USER_LAYER);
        this.avatar.setEnergyCallback(energyDisplay::updateEnergy);
    }

    /**
     * adds the ground and tree between given coordinates
     * @param minX start of range to cover
     * @param maxX end of range to cover
     */
    private void addGroundAndFlora(int minX, int maxX){
        List<Block> blocks = this.terrain.createInRange(minX, maxX);
        for(Block block : blocks){
            gameObjects().addGameObject(block, TERRAIN_LAYER);
        }
        List<GameObject> objects = this.flora.createInRange(minX,maxX);
        for(GameObject object : objects){
            switch (object.getTag()) {
                case Tree.TRUNK_TAG -> gameObjects().addGameObject(object, TERRAIN_LAYER);
                case Tree.LEAF_TAG -> gameObjects().addGameObject(object, SKY_LAYER);
                case Fruit.FRUIT_TAG -> gameObjects().addGameObject(object, AVATAR_LAYER);
            }
        }
    }

    /**
     * Extends the world if needed.
     * @param dimensions dimensions of the screen
     */
    private void updateWorld(Vector2 dimensions){
        int screenWidth =(int)  dimensions.x();

        int neededMinX = (int) this.avatar.getCenter().x() - screenWidth*WORLD_SCREENS;
        int neededMaxX = (int) this.avatar.getCenter().x() + screenWidth*WORLD_SCREENS;

        while(neededMaxX > this.loadMaxX + screenWidth){
            extendWorldRight(neededMinX, neededMaxX);
            this.loadMinx = neededMinX;
            this.loadMaxX = neededMaxX;

        }
        while(neededMinX < this.loadMinx-screenWidth){
            extendWorldLeft( neededMinX, neededMaxX);
            this.loadMinx = neededMinX;
            this.loadMaxX = neededMaxX;
        }
    }

    /**
     *  adds grounds and flora to the next needed coordinates on the right
     * @param neededMinX min coordinate to cover
     * @param neededMaxX max coordinate to cover
     */
    private void extendWorldRight(int neededMinX, int neededMaxX){
        addGroundAndFlora (this.loadMaxX, neededMaxX);
        removeFarObject(neededMinX, neededMaxX);
    }

    /**
     * adds grounds and flora to the next needed coordinates on the left
     * @param neededMinX min coordinate to cover
     * @param neededMaxX max coordinate to cover
     */
    private void extendWorldLeft(int neededMinX, int neededMaxX){
        addGroundAndFlora (neededMinX, this.loadMinx );
        removeFarObject(neededMinX,neededMaxX);

    }

    /**
     * Removes objects that are far from the avatar (outside the screens we keep)
     * @param neededMinX the min coordinate to keep object from
     * @param neededMaxX the max coordinate to keep object from
     */
    private void removeFarObject(int neededMinX, int neededMaxX){
        for(GameObject object : gameObjects()){
            float x = object.getTopLeftCorner().x();
            if(x < neededMinX || x > neededMaxX){
               switch (object.getTag()) {
                   case Tree.TRUNK_TAG -> gameObjects().removeGameObject(object, TERRAIN_LAYER);
                   case Tree.LEAF_TAG -> gameObjects().removeGameObject(object, SKY_LAYER);
                   case Fruit.FRUIT_TAG -> gameObjects().removeGameObject(object, AVATAR_LAYER);
                   case Fruit.UNAVAILABLE_FRUIT_TAG ->  gameObjects().removeGameObject(object, AVATAR_LAYER);
                   case Terrain.GROUND_TAG -> gameObjects().removeGameObject(object, TERRAIN_LAYER);
               }
            }
        }
    }

    /**
     *
     * @param deltaTime The time, in seconds, that passed since the last invocation
     *                  of this method (i.e., since the last frame). This is useful
     *                  for either accumulating the total time that passed since some
     *                  event, or for physics integration (i.e., multiply this by
     *                  the acceleration to get an estimate of the added velocity or
     *                  by the velocity to get an estimate of the difference in position).
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateWorld(this.dimensions);
    }

    /**
     *  Runs the simulator
     * @param args None needed for this program
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

}


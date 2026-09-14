package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Terrain;
import pepse.world.trees.Fruit;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/** Object class representing an avatar that can move around in our simulator. It can run, jump and eat. */
public class Avatar extends GameObject {

    /** Const for the size of the avatar. */
    private static final float AVATAR_SIZE = 40;

    /** Const fot the velocity and gravity of the avatar. */
    private static final float VELOCITY_X = 400;
    private static final float VELOCITY_Y = -500f;
    private static final float GRAVITY = 600;

    /** Limits for number of energy points. */
    private static final int MIN_ENERGY = 0;
    private static final int MAX_ENERGY = 100;

    /** Required/Gained energy for movements. */
    private static final int REST_REGAIN_ENERGY = 1;
    private static final int RUN_ENERGY_COST = 2;
    private static final int JUMP_ENERGY_COST = 20;
    private static final int DOUBLE_JUMP_ENERGY_COST = 50;
    private static final int FRUIT_REGAIN_ENERGY = 10;

    /** Constants to represent if the avatar wants to go right or left*/
    private static final int RIGHT_DIRECTION = 1;
    private static final int LEFT_DIRECTION = -1;

    /** List of images for each state animation */
    private static final String[]  idleAnimationFrames= new String[]{"assets/idle_0.png",
            "assets/idle_1.png", "assets/idle_2.png", "assets/idle_3.png"};
    private static final String[] runAnimationFrames= new String[]{"assets/run_0.png",
            "assets/run_1.png", "assets/run_2.png", "assets/run_3.png", "assets/run_4.png",
            "assets/run_5.png"};
    private static final String[] jumpAnimationFrames= new String[]{"assets/jump_0.png",
            "assets/jump_1.png", "assets/jump_2.png", "assets/jump_3.png"};

    /** Time between the change in animation frame for each state */
    private static final float timeBetweenAnimationClips = 0.15f;

    /** Tags */
    public static final String AVATAR_TAG = "avatar";

    // END OF PRIVATE STATIC FINAL

    /** Animations for the different states of the avatar. */
    private final AnimationRenderable idleAnimation;
    private final AnimationRenderable runAnimation;
    private final AnimationRenderable jumpAnimation;

    /** Used to read input from keyboard. */
    private final UserInputListener inputListener;

    /** Number representing the energy the avatar has at each point in time (always between 0 and 100). */
    private int energy;
    /** Mode in which the avatar is in each point in time. */
    private MovementState movementState;

    /** At each jump we can do only one double jump, this will tell us if we used it or not. */
    private boolean doubleJumpUsed = false;

    /** Callback used to update the renderable for energy percentage. */
    private Consumer<Integer> energyCallBack;


    /**
     * Constructor for the avatar, sets his image, gravity and energy.
     * @param topLeftCorner where the top left corner of the avatar will be placed
     * @param inputListener object that reads from the keyboard
     * @param imageReader object that can read images from paths
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener, ImageReader imageReader){
        super(topLeftCorner,Vector2.ONES.mult(AVATAR_SIZE), null);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);  // prevent from going in the ground
        Renderable avatarImage = imageReader.readImage("assets/idle_0.png", true);
        renderer().setRenderable(avatarImage);
        transform().setAccelerationY(GRAVITY);
        this.setTag(AVATAR_TAG);
        this.inputListener = inputListener;
        this.energy = MAX_ENERGY;

        this.idleAnimation = new AnimationRenderable( idleAnimationFrames, imageReader, true,
                timeBetweenAnimationClips);
        this.runAnimation = new AnimationRenderable( runAnimationFrames,
                imageReader, true, timeBetweenAnimationClips);
        this.jumpAnimation = new AnimationRenderable(jumpAnimationFrames, imageReader, true,
                timeBetweenAnimationClips);
    }

    /** Returns true if the avatar is currently falling (checking its y velocity).
     */
    private boolean isFalling(){
        return getVelocity().y() > 0;
    }

    /**
     * Makes sure the avatar won't fall through the ground
     * @param other The GameObject with which a collision occurred.
     * @param collision Information regarding this collision.
     *                  A reasonable elastic behavior can be achieved with:
     *                  setVelocity(getVelocity().flipped(collision.getNormal()));
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if(other.getTag().equals(Terrain.GROUND_TAG) && isFalling()) {
            this.transform().setVelocityY(0);
            this.doubleJumpUsed = false; // reset our ability to double jump after we landed
            this.movementState = MovementState.IDLE;
        }
        if(other.getTag().equals(Fruit.FRUIT_TAG)) {
            increaseEnergy(FRUIT_REGAIN_ENERGY);
        }
    }

    /**
     * Update the avatar according to input and state
     * @param deltaTime The time elapsed, in seconds, since the last frame. Can
     *                  be used to determine a new position/velocity by multiplying
     *                  this delta with the velocity/acceleration respectively
     *                  and adding to the position/velocity:
     *                  velocity += deltaTime*acceleration
     *                  pos += deltaTime*velocity
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float xVel = 0;

        if(inputListener.isKeyPressed(KeyEvent.VK_LEFT) && !inputListener.isKeyPressed(KeyEvent.VK_RIGHT))
            xVel += runIfPossible(LEFT_DIRECTION);
        if(inputListener.isKeyPressed(KeyEvent.VK_RIGHT) && !inputListener.isKeyPressed(KeyEvent.VK_LEFT))
            xVel += runIfPossible(RIGHT_DIRECTION);
        transform().setVelocityX(xVel);

        if(inputListener.isKeyPressed(KeyEvent.VK_SPACE))
            jumpIfPossible();

        if(xVel == 0 && isOnGround()){ // if it is zero, and we are on ground nothing happened
            this.movementState = MovementState.IDLE;
            renderer().setRenderable(this.idleAnimation);
            increaseEnergy(REST_REGAIN_ENERGY);
        }
    }

    /** Returns true if the avatar is on the ground.
      */
    private boolean isOnGround(){
        return getVelocity().y() == 0;
    }

    /**
     *  Reduce the energy by the given number, making sure not to go under the minimum.
     * @param amountToReduce number to reduce energy by
     */
    private void reduceEnergy(int amountToReduce){
        int oldEnergy = this.energy; // keep to see if changed
        this.energy = Math.max(MIN_ENERGY, this.energy - amountToReduce);

        if(energyCallBack != null && this.energy!=oldEnergy){
            energyCallBack.accept(this.energy);
        }
    }

    /**
     *  Increases the energy by the given number, making sure not to go over the maximum
     * @param amountToIncrease number to indreace energy by
     */
    private void increaseEnergy(int amountToIncrease){
        int oldEnergy = this.energy;
        this.energy = Math.min(MAX_ENERGY, this.energy + amountToIncrease);

        if(energyCallBack != null && this.energy!=oldEnergy){
            energyCallBack.accept(this.energy);
        }
    }

    /**
     * Checks if we can move to the wanted direction and return the new velocity the avatar should have.
     *  if we are allowed to run and are not in the air - make payment and change movement state
     * @param direction to move to (right/ left)
     * @return the new velocity the avatar should have
     */
    private float runIfPossible(int direction){
        float xVel = 0;
        // if we are not on the ground, move with no cost
        if(!isOnGround()){
            if(direction == RIGHT_DIRECTION){
                xVel += VELOCITY_X;
                renderer().setIsFlippedHorizontally(false);
            }
            if(direction == LEFT_DIRECTION){
                xVel -= VELOCITY_X;
                renderer().setIsFlippedHorizontally(true);
            }
            return xVel;
        }
        // if we don't have enough energy do nothing
        if(this.energy < RUN_ENERGY_COST)
            return xVel;

        if(direction == RIGHT_DIRECTION){
            xVel += VELOCITY_X;
            renderer().setIsFlippedHorizontally(false);
        }
        if(direction == LEFT_DIRECTION){
            xVel -= VELOCITY_X;
            renderer().setIsFlippedHorizontally(true);
        }
        reduceEnergy(RUN_ENERGY_COST);
        this.movementState = MovementState.RUN;
        renderer().setRenderable(this.runAnimation);
        return xVel;
   }

    /** Makes the avatar jump/double jump based on current conditions (if it's on the ground, already double
     *  jumped, has enough energy... */
   private void jumpIfPossible(){
      if(isOnGround()){
          if(this.energy >= JUMP_ENERGY_COST){
              transform().setVelocityY(VELOCITY_Y);
              reduceEnergy(JUMP_ENERGY_COST);
              this.movementState = MovementState.JUMP;
              renderer().setRenderable(this.jumpAnimation);
              return;
          }
          // here we are on ground but don't have enough energy to jump so we do nothing
          return;
      }
      // allow double jump if not used and we are in the air
      if((!this.doubleJumpUsed) && isFalling()){
          this.doubleJumpUsed = true;
          if(this.energy >= DOUBLE_JUMP_ENERGY_COST){
              transform().setVelocityY(VELOCITY_Y);
              reduceEnergy(DOUBLE_JUMP_ENERGY_COST);
              this.movementState = MovementState.JUMP;
              renderer().setRenderable(this.jumpAnimation);
          }
      }

   }

    /**
     * Used to set what callback function the updates of energy will use
     * @param callback the function to use
     */
   public void setEnergyCallback(Consumer<Integer> callback){
       this.energyCallBack = callback;
       callback.accept(this.energy);
   }

}

package pepse.world.avatar;

/** Enum for the state of movement the avatar can be in. */
public enum MovementState {
    /** When tha avatar is standing still on the ground*/
    IDLE,
    /** When the avatar is running (either left or righ) t*/
    RUN,
    /** When the avatar is in the air (either going up or down) */
    JUMP
}

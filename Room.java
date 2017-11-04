/**
 * Class Room - a room in an adventure game.
 *
 * This class is inspired by the "World of Zuul" application. 
 * "World of Zuul" is a very simple, text based adventure game.  
 *
 * A "Room" represents one location in the scenery of the game.  It is 
 * connected to other rooms via exits.  The exits are labelled north, 
 * east, south, west.  For each direction, the room stores a reference
 * to the neighboring room, or null if there is no exit in that direction.
 * Adapted from the work of Hani Safadi, Michael Kölling, and David J. Barnes.
 * 
 * @author https://github.com/f-z
 * @version 2016.11.16
 */
public class Room 
{
    private int x, y; // represent room location in the maze (row, col)
    private Room northExit;
    private Room southExit;
    private Room eastExit;
    private Room westExit;
    private boolean[] person; // players
    private boolean exitRoom; // is this room the final exit room?
    private boolean[] evilCircle = {false, false, false}; // red circles that represent the evil demons roaming the maze randomly
    private boolean[] killEvilCircle; // green triangle power-up that kills an evil circle
    private boolean[] becomeInvincible;// blue triangle power-up that makes the player undestructible for 1 minute
    private boolean[] doubleSpeed;
    
    /**
     * Create a room described "description". Initially, it has
     * no exits. "description" is something like "a kitchen" or
     * "an open court yard".
     * @param description The room's description.
     */
    public Room(int x, int y)
    {
        this.x = x;
        this.y = y;
        person = new boolean[2];
        doubleSpeed = new boolean[2];
        killEvilCircle = new boolean[2];
        becomeInvincible = new boolean[2];
        
        for (int i = 0; i < 2; i++) {
            person[i] = false;
            doubleSpeed[i] = false;
            killEvilCircle[i] = false;
            becomeInvincible[i] = false;
        }
        
        exitRoom= false;
    }

    /**
     * Define the exits of this room.  Every direction either leads
     * to another room or is null (no exit there).
     * @param north The north exit.
     * @param east The east east.
     * @param south The south exit.
     * @param west The west exit.
     */
    public void setExits(Room north, Room east, Room south, Room west) 
    {
        if(north != null)
            northExit = north;
        if(east != null)
            eastExit = east;
        if(south != null)
            southExit = south;
        if(west != null)
            westExit = west;
    }

    /**
     * @param player The number of the player (1 or 2).
     * @return True if the player is currently in this room.
     */
    public boolean isPersonInRoom(int player) {return person[player-1];}

    public boolean isEvilInRoom(int evil) {return evilCircle[evil-1];}

    /**
     * @param i The number of one of the power-ups that kills an evil circle (1 or 2).
     * @return True if the power-up that kills an evil circle is currently in this room.
     */
    public boolean isKillEvilCircleInRoom(int i) {return killEvilCircle[i - 1];}

    /**
     * @param i The number of one of the power-ups that makes the player invincible (1 or 2).
     * @return True if the invincibility power-up that makes the player indestructible is currently in this room.
     */
    public boolean isInvincibilityInRoom(int i) {return becomeInvincible[i - 1];}

   /**
     * @param i The number of one of the power-ups that doubles player speed (1 or 2).
     * @return True if the power-up is currently in this room.
     */
    public boolean isDoubleSpeedInRoom(int i) {return doubleSpeed[i - 1];}

    /**
     * Setter method for players. 
     * Assigns a value of true if the player is in the room or false otherwise.
     * @param i The number of the player (1 or 2).
     * @param isInRoom True if the player is in the current room.
     */
    public void setPersonInRoom(int i, boolean isInRoom) {person[i - 1] = isInRoom;}

    /**
     * Setter method for evil circles. 
     * Assigns a value of true if the evil is in the room or false otherwise.
     * @param i An int that represents if it is evil 1, 2, or 3.
     * @param isInRoom A boolean that is true if the evil is in the room.
     */
    public void setEvilInRoom(int i, boolean isInRoom) {
        evilCircle[i - 1] = isInRoom;
    }
 
    /**
     * Setter method for the power-up that kills evil circles.
     * Assigns a value of true if the power-up is in the room or false otherwise.
     * @param i An int that represents if it is power-up number 1 or 2.
     * @param isInRoom A boolean that is true if the power-up is in the room.
     */
    public void setKillEvilCircleInRoom(int i, boolean isInRoom) {
        killEvilCircle[i - 1] = isInRoom;
    }

    /**
     * Setter method for the power-up that makes the player invincible.
     * Assigns a value of true if the power-up is in the room or false otherwise.
     * @param i An int that represents if it is power-up number 1 or 2.
     * @param isInRoom A boolean that is true if the power-up is in the room.
     */
    public void setInvincibilityInRoom(int i, boolean isInRoom) {
        becomeInvincible[i - 1] = isInRoom;
    }

    /**
     * Setter method for Double Speed 1.
     * assigns a value of true if the Double Speed 1 Power-up is in the room or false otherwise
     */
    public void setDoubleSpeedInRoom(int i, boolean isInRoom) {
        doubleSpeed[i - 1] = isInRoom;
    }

    // Setter and getter methods
    public int getX() {return x;}

    public int getY() {return y;}

    /**
     * Get the room north of the current room.
     * @return Returns the room in the north, but null if there is a wall on the north side (if there is no exit that way).
     */
    public Room getNorthExit() {return northExit;} 

    /**
     * Get the room south of the current room.
     * @return Returns the room in the south, but null if there is a wall on the south side (if there is no exit that way).
     */
    public Room getSouthExit() {return southExit;}

     /**
     * Get the room east of the current room.
     * @return Returns the room in the east, but null if there is a wall on the east side (if there is no exit that way).
     */
    public Room getEastExit() {return eastExit;}

     /**
     * Get the room west of the current room.
     * @return Returns the room in the west, but null if there is a wall on the west side (if there is no exit that way).
     */
    public Room getWestExit() {return westExit;}

    /**
     * @return Returns true if the cuurent room is the exit room at the end of the game.
     */
    public boolean getExitRoom() {return exitRoom;}

    /**
     * Sets the exit room that ends the game if the player reaches it.
     * @param exit True if it is the exit room, false otherwise.
     */
    public void setExitRoom(boolean exit) {exitRoom = exit;}
}
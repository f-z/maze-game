import javax.swing.JOptionPane;
import javax.sound.sampled.AudioInputStream; // https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Clip.html, accessed on 08/31/2016.
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * A class implementing a random maze game (controller)
 * 
 * @author https://github.com/f-z
 * @version 2016.11.16
 */
public class Game
{

    private Canvas canvas;
    private int x, x2;
    private int y, y2;
    private int[] ex; // stores the locations of the evil circles
    private int[] ey;
    private int[] powerUpX; // stores the locations of the power-ups
    private int[] powerUpY;
    private Maze maze;
    private Renderer renderer;
    private boolean usedSpace, dots[][], allDotsConsumed;
    private int speed;
    String filename;
    MP3 mp3;
    private long activatedAt;
    private int players;
    private String godMode = "";
    int[][] mazeSolver;
    int speedSolver = 15;    // short delay between solver steps

    /**
     * Constructor for objects of class Game
     */
    public Game()
    {

        // initialise instance variables 
        canvas = Canvas.getCanvas();
        maze = new Maze(25);

        // start the game at 1, 1 (top left)
        x = 1;
        y = 1;
        x2 = 1;
        y2 = 1;

        maze.getRoom(x, y).setPersonInRoom(1, true);
        renderer = new Renderer(27); // maze size with +2 for borders
        renderer.renderMaze(maze);

        players = 0;

        usedSpace = false; // used the power to break the walls

        allDotsConsumed = false; // have all dots been consumed?
        speed = 1; // default speed setting
        filename = "./sounds/tetris.mp3"; // Tetris theme song from https://www.youtube.com/watch?v=E8FQBjVlERk, accessed on 11/04/2016
        mp3 = new MP3(filename);
        activatedAt = -60000;

        renderer.refresh();
    }

    /**
     * This method starts the main loop that controls interaction within the game.
     * Game loop:
     * 1- read player movement
     * 2- move the person
     * 3- redraw 
     * 4- wait for next move
     * 5- check for winning condition
     */
    public void interact()
    {
        mp3.play();

        if (players == 1){

            // create 100 random dots
            dots = new boolean[100][100];
            for (int k = 0; k < 100; k++) {
                int r = Maze.randomNum(24);
                int r2 = Maze.randomNum(24);
                // there is a dot in the room at row r, column r2
                dots[r+1][r2+1] = true;
                renderer.renderDots(maze.getRoom(r+1,r2+1));
            }

            createEvilCircles(3);

            createPowerUps();

            renderer.refresh();

            JOptionPane.showMessageDialog(null, "- Press SPACE\n   to break the walls around you!\n\n- You can only do it ONCE,\n- so use it wisely!", "TIP:", JOptionPane.PLAIN_MESSAGE);

            while (true) {
                if (mp3.hasEnded())
                    mp3.play();

                boolean[] directions = getKey();

                boolean exit = movePlayer(directions[0], directions[1], directions[2], directions[3]);

                if(exit) // if reached the exit room
                {
                    playSoundEffect("win");

                    JOptionPane.showMessageDialog(null, "Congratulations! You win!", "WIN!", JOptionPane.PLAIN_MESSAGE);

                    JOptionPane.showMessageDialog(null, "Now watch me try!", "AI Player", JOptionPane.PLAIN_MESSAGE);

                    initializeMazeSolver();
                    solveMaze(1,1);

                    System.exit(0);
                }
                else {dots[maze.getRoom(x, y).getX()][maze.getRoom(x, y).getY()] = false;
                    // get current room, get its location on the maze (x,y) and assign false to the
                    // respective Dots in that room (they have been consumed, since the player is here now)

                    detectLoss();

                    if (speed == 1)
                        moveEvils();
                    else if (speed == 2)
                    {
                        int random1 = Maze.randomNum(2);
                        if (random1 == 0) moveEvils();
                    }
                    else if (speed == 4)
                    {
                        int random1 = Maze.randomNum(4);
                        if (random1 == 0 || random1 == 1) moveEvils();
                    }
                    else moveEvils();

                    detectLoss();

                    if (maze.getRoom(x,y).isKillEvilCircleInRoom(1) && maze.getRoom(ex[0], ey[0]).isEvilInRoom(1)) // if the player reaches the green power-up and Evil 1 is still alive 
                    {
                        maze.getRoom(x,y).setKillEvilCircleInRoom(1, false);
                        maze.getRoom(ex[0],ey[0]).setEvilInRoom(1, false);
                        JOptionPane.showMessageDialog(null, "Evil 1 has been killed!\nGoodbye Evil 1...", "POWER-UP!", JOptionPane.PLAIN_MESSAGE);
                        renderer.renderRoom(maze.getRoom(ex[0],ey[0])); // redraw Evil 1's old room without it
                    }

                    if (maze.getRoom(x,y).isKillEvilCircleInRoom(2) && maze.getRoom(ex[1], ey[1]).isEvilInRoom(2))
                    {
                        maze.getRoom(x,y).setKillEvilCircleInRoom(2, false);
                        maze.getRoom(ex[1],ey[1]).setEvilInRoom(2, false);
                        JOptionPane.showMessageDialog(null, "Evil 2 has been killed!\nGoodbye Evil 2...", "POWER-UP!", JOptionPane.PLAIN_MESSAGE);
                        renderer.renderRoom(maze.getRoom(ex[1],ey[1]));
                    }

                    if (maze.getRoom(x,y).isInvincibilityInRoom(1) || maze.getRoom(x,y).isInvincibilityInRoom(2)) // if the player reaches one of the blue power-ups
                    {
                        invActivate();

                        if (maze.getRoom(x,y).isInvincibilityInRoom(1))                        
                            maze.getRoom(x,y).setInvincibilityInRoom(1, false);
                        else
                            maze.getRoom(x,y).setInvincibilityInRoom(2, false);

                        JOptionPane.showMessageDialog(null, "You are now invincible for 1 minute!", "POWER-UP!", JOptionPane.PLAIN_MESSAGE);
                    }

                    if (maze.getRoom(x,y).isDoubleSpeedInRoom(1) || maze.getRoom(x,y).isDoubleSpeedInRoom(2)) // if the player reaches one of the red power-ups
                    {
                        speed *= 2;

                        if (maze.getRoom(x,y).isDoubleSpeedInRoom(1))
                            maze.getRoom(x,y).setDoubleSpeedInRoom(1, false);
                        else 
                            maze.getRoom(x,y).setDoubleSpeedInRoom(2, false);

                        JOptionPane.showMessageDialog(null, "You are now twice as fast!", "POWER-UP!", JOptionPane.PLAIN_MESSAGE);
                    }                   
                }

                allDotsConsumed = true;

                // if there is even 1 dot that has not yet been consumed and is still there
                // then that means that not all dots have been consumed
                for (int i = 0; i < 100; i++)
                {
                    for (int j = 0; j < 100; j++)
                    {
                        if (dots[i][j] == true)
                        {
                            allDotsConsumed = false;
                        }
                    }
                }

                // if all 100 dots have been consumed, display a congratulatory message and exit the game
                if (allDotsConsumed == true)
                {
                    playSoundEffect("win");
                    JOptionPane.showMessageDialog(null, "Congratulations!\nYou got all the dots!\n\nYou win!", "WIN WITH DOTS", JOptionPane.PLAIN_MESSAGE);
                    System.exit(0);
                }

                if (godMode.toUpperCase().contains("FILIP"))
                    usedSpace = false;

                // determines the speed of the game
                try {
                    if(speed == 1)
                        Thread.sleep(16);
                    else if (speed == 2)
                        Thread.sleep(8);
                    else if (speed == 4)
                        Thread.sleep(4);
                }
                catch (InterruptedException e) { }
            }
        }
        else if (players == 2) {

            try {Thread.sleep(150);}
            catch (Exception e) {
                e.printStackTrace();
            }

            JOptionPane.showMessageDialog(null, "Race Mode!", "RACE", JOptionPane.PLAIN_MESSAGE);

            maze.getRoom(x2, y2).setPersonInRoom(2, true);
            renderer.refresh();     

            while (true) {

                if (mp3.hasEnded())
                    mp3.play();

                boolean[] directions = getKey();

                boolean exit = movePlayer(directions[0], directions[1], directions[2], directions[3]);
                boolean exit2 = movePlayer2(directions[4], directions[5], directions[6], directions[7]);

                if (exit) //exit room
                {
                    playSoundEffect("win");

                    JOptionPane.showMessageDialog(null, "Congratulations Player 1 (Blue)! You win!", "PlAYER 1 WIN", JOptionPane.PLAIN_MESSAGE);
                    JOptionPane.showMessageDialog(null, "Sorry Player 2 (Green)! Better luck next time...", "PlAYER 1 WIN", JOptionPane.PLAIN_MESSAGE);
                    System.exit(0);
                }
                else if (exit2) //exit room
                {
                    playSoundEffect("win");

                    JOptionPane.showMessageDialog(null, "Congratulations Player 2 (Green)! You win!", "PlAYER 2 WIN", JOptionPane.PLAIN_MESSAGE);
                    JOptionPane.showMessageDialog(null, "Sorry Player 1 (Blue)! Better luck next time...!", "PlAYER 2 WIN", JOptionPane.PLAIN_MESSAGE);
                    System.exit(0);
                }

                // determines the speed of the game
                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException e) { }
            }
        }        
    }

    /**
     * Moves the Player, if the game is in single-player mode.
     * Moves Player 1, if it is in 2-player mode.
     * @param north True if the direction pressed is north.
     * @param south True if the direction pressed is south.
     * @param east True if the direction pressed is east.
     * @param west True if the direction pressed is west.
     * @return True if Player 1 has moved to the exit room.
     */
    public boolean movePlayer(boolean north, boolean south, boolean east, boolean west)
    {   
        if (! (north || south || east || west))
            return false; // nothing happended save time
        // what is the current room?    
        Room r = maze.getRoom(x, y);
        boolean exit = false;
        // can the person go in the direction of the key press?
        if (north && r.getNorthExit() != null) {
            Room r2 = r.getNorthExit();
            r.setPersonInRoom(1, false);
            r2.setPersonInRoom(1, true);
            renderer.renderRoom(r);
            renderer.renderRoom(r2);
            x = r2.getX(); y = r2.getY();
            exit = r2.getExitRoom();
        }
        else if (south && r.getSouthExit() != null) {
            Room r2 = r.getSouthExit();
            r.setPersonInRoom(1, false);
            r2.setPersonInRoom(1, true);
            renderer.renderRoom(r);
            renderer.renderRoom(r2);
            x = r2.getX(); y = r2.getY();
            exit = r2.getExitRoom();
        }
        else if (east && r.getEastExit() != null) {
            Room r2 = r.getEastExit();
            r.setPersonInRoom(1, false);
            r2.setPersonInRoom(1, true);
            renderer.renderRoom(r);
            renderer.renderRoom(r2);
            x = r2.getX(); y = r2.getY();
            exit = r2.getExitRoom();
        }
        else if (west && r.getWestExit() != null) {
            Room r2 = r.getWestExit();
            r.setPersonInRoom(1, false);
            r2.setPersonInRoom(1, true);
            renderer.renderRoom(r);
            renderer.renderRoom(r2);
            x = r2.getX(); y = r2.getY();
            exit = r2.getExitRoom();
        }

        // refresh after moving the person
        renderer.refresh();       
        return exit;
    }

    /**
     * Moves Player 2, if the game is in 2-player mode
     * @param north2 True if the direction pressed is north.
     * @param south2 True if the direction pressed is south.
     * @param east2 True if the direction pressed is east.
     * @param west2 True if the direction pressed is west.
     * @return True if Player 2 has moved to the exit room.
     */
    public boolean movePlayer2(boolean north2, boolean south2, boolean east2, boolean west2)
    {   
        if (!(north2 || south2 || east2 || west2))
            return false;

        Room r3 = maze.getRoom(x2, y2);
        boolean exit2 = false;

        if (north2 && r3.getNorthExit() != null) {
            Room r4 = r3.getNorthExit();
            r3.setPersonInRoom(2, false);
            r4.setPersonInRoom(2, true);
            renderer.renderRoom(r3);
            renderer.renderRoom(r4);
            x2 = r4.getX(); y2 = r4.getY();
            exit2 = r4.getExitRoom();
        }
        else if (south2 && r3.getSouthExit() != null) {
            Room r4 = r3.getSouthExit();
            r3.setPersonInRoom(2, false);
            r4.setPersonInRoom(2, true);
            renderer.renderRoom(r3);
            renderer.renderRoom(r4);
            x2 = r4.getX(); y2 = r4.getY();
            exit2 = r4.getExitRoom();
        }
        else if (east2 && r3.getEastExit() != null) {
            Room r4 = r3.getEastExit();
            r3.setPersonInRoom(2, false);
            r4.setPersonInRoom(2, true);
            renderer.renderRoom(r3);
            renderer.renderRoom(r4);
            x2 = r4.getX(); y2 = r4.getY();
            exit2 = r4.getExitRoom();
        }
        else if (west2 && r3.getWestExit() != null) {
            Room r4 = r3.getWestExit();
            r3.setPersonInRoom(2, false);
            r4.setPersonInRoom(2, true);
            renderer.renderRoom(r3);
            renderer.renderRoom(r4);
            x2 = r4.getX(); y2 = r4.getY();
            exit2 = r4.getExitRoom();
        }

        renderer.refresh();       
        return exit2;
    }

    /**
     * Moves each one of the 3 evil circles randomly.
     */
    public void moveEvils()
    {   boolean[] evilsMoved = {false, false, false};

        while (!evilsMoved[0] && maze.getRoom(ex[0], ey[0]).isEvilInRoom(1))
        {   int randomNum = Maze.randomNum(4);
            Room e = maze.getRoom(ex[0], ey[0]);

            if (randomNum == 0) // move evil 1 north
            {
                if (e.getNorthExit() != null)
                {
                    Room ee = e.getNorthExit();
                    e.setEvilInRoom(1, false);
                    ee.setEvilInRoom(1, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[0] = ee.getX(); ey[0] = ee.getY();
                    evilsMoved[0] = true;
                }
            }
            else if (randomNum == 1)
            {
                if (e.getEastExit() != null && e.getEastExit().getExitRoom() == false)
                {
                    Room ee = e.getEastExit();
                    e.setEvilInRoom(1, false);
                    ee.setEvilInRoom(1, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[0] = ee.getX(); ey[0] = ee.getY();
                    evilsMoved[0] = true;
                }
            }
            else if (randomNum == 2)
            {
                if (e.getSouthExit() != null)
                {
                    Room ee = e.getSouthExit();
                    e.setEvilInRoom(1, false);
                    ee.setEvilInRoom(1, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[0] = ee.getX(); ey[0] = ee.getY();
                    evilsMoved[0] = true;
                }
            }
            else if (randomNum == 3)
            {
                if (e.getWestExit() != null)
                {
                    Room ee = e.getWestExit();
                    e.setEvilInRoom(1, false);
                    ee.setEvilInRoom(1, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[0] = ee.getX(); ey[0] = ee.getY();
                    evilsMoved[0] = true;
                }
            }
        }

        while (!evilsMoved[1] && maze.getRoom(ex[1], ey[1]).isEvilInRoom(2))
        {   int randomNum = Maze.randomNum(4);
            Room e = maze.getRoom(ex[1], ey[1]);

            if (randomNum == 0)
            {
                if (e.getNorthExit() != null)
                {
                    Room ee = e.getNorthExit();
                    e.setEvilInRoom(2, false);
                    ee.setEvilInRoom(2, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[1] = ee.getX(); ey[1] = ee.getY();
                    evilsMoved[1] = true;
                }
            }
            else if (randomNum == 1)
            {
                if (e.getEastExit() != null && e.getEastExit().getExitRoom() == false)
                {
                    Room ee = e.getEastExit();
                    e.setEvilInRoom(2, false);
                    ee.setEvilInRoom(2, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[1] = ee.getX(); ey[1] = ee.getY();
                    evilsMoved[1] = true;
                }
            }
            else if (randomNum == 2)
            {
                if (e.getSouthExit() != null)
                {
                    Room ee = e.getSouthExit();
                    e.setEvilInRoom(2, false);
                    ee.setEvilInRoom(2, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[1] = ee.getX(); ey[1] = ee.getY();
                    evilsMoved[1] = true;
                }
            }
            else if (randomNum == 3)
            {
                if (e.getWestExit() != null)
                {
                    Room ee = e.getWestExit();
                    e.setEvilInRoom(2, false);
                    ee.setEvilInRoom(2, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[1] = ee.getX(); ey[1] = ee.getY();
                    evilsMoved[1] = true;
                }
            }
        }

        while (!evilsMoved[2])
        {   int randomNum = Maze.randomNum(4);
            Room e = maze.getRoom(ex[2],ey[2]);

            if (randomNum == 0)
            {
                if (e.getNorthExit() != null)
                {
                    Room ee = e.getNorthExit();
                    e.setEvilInRoom(3, false);
                    ee.setEvilInRoom(3, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[2] = ee.getX(); ey[2] = ee.getY();
                    evilsMoved[2] = true;
                }
            }
            else if (randomNum == 1)
            {
                if (e.getEastExit() != null && e.getEastExit().getExitRoom() == false)
                {
                    Room ee = e.getEastExit();
                    e.setEvilInRoom(3, false);
                    ee.setEvilInRoom(3, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[2] = ee.getX(); ey[2] = ee.getY();
                    evilsMoved[2] = true;
                }
            }
            else if (randomNum == 2)
            {
                if (e.getSouthExit() != null)
                {
                    Room ee = e.getSouthExit();
                    e.setEvilInRoom(3, false);
                    ee.setEvilInRoom(3, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[2] = ee.getX(); ey[2] = ee.getY();
                    evilsMoved[2] = true;
                }
            }
            else if (randomNum == 3)
            {
                if (e.getWestExit() != null)
                {
                    Room ee = e.getWestExit();
                    e.setEvilInRoom(3, false);
                    ee.setEvilInRoom(3, true);
                    renderer.renderRoom(e);
                    renderer.renderRoom(ee);
                    ex[2] = ee.getX(); ey[2] = ee.getY();
                    evilsMoved[2] = true;
                }
            }
        }

        renderer.refresh();
    }

    /**
     * Activate the invincibility power-up and store the exact activation time.
     */
    public void invActivate() {
        activatedAt = System.currentTimeMillis();
    }

    /**
     * Check if the invincibility power is still active (if it is <1 minute from its last activation).
     */
    public boolean isInvActive() {
        return (System.currentTimeMillis() - activatedAt <= 60000);
    }

    /**
     * Set the number of players in the game.
     * @param players The number of players (1 or 2).
     */
    public void setPlayers(int players) {
        this.players = players;
    }

    /**
     * @return players Returns the number of players in the current game (1 or 2).
     */
    public int getPlayers() {
        return players;
    }

    /**
     * Preparing the maze solver and setting boundaries.
     */
    private void initializeMazeSolver()
    {
        mazeSolver = new int[maze.getSize()+2][maze.getSize()+2];
        for(int i = 0; i < maze.getSize(); i++) {
            for(int j = 0; j < maze.getSize(); j++)
                mazeSolver[i][j] = 0;    
        }

        for (int x = 0; x < maze.getSize()+2; x++) {
            mazeSolver[x][0] = -1; // -1 means visited
            mazeSolver[x][maze.getSize()+1] = -1;
        }
        for (int y = 0; y < maze.getSize()+2; y++) {
            mazeSolver[0][y] = -1;
            mazeSolver[maze.getSize()+1][y] = -1;
        }       
    }

    /**
     * My implementation was inspired by the maze created by Dr. David Eck, 
     * found at http://math.hws.edu/xJava/other/maze.html 
     * and accessed on 11/04/2016.
     * @param row The row of the current room to be solved.
     * @param col The column of the current room to be solved.
     * @return True if the solver has reached the end of the maze.
     */
    private boolean solveMaze(int row, int col) {

        if (mp3 == null || mp3.hasEnded())
        {
            filename = "./sounds/pacman.mp3"; // https://www.youtube.com/watch?v=BxYzjjs6d1s, accessed on 11/04/2016
            mp3 = new MP3(filename);
            mp3.play();
        }

        // Try to solve the maze by continuing current path from position (row,col).
        // Return true if a solution is found.
        // The maze is considered to be solved if the path reaches the lower right cell.
        if (mazeSolver[row][col] == 0) {
            mazeSolver[row][col] = 1;      //add this cell to the path
            renderer.renderSolution(maze.getRoom(row, col));
            renderer.refresh();
            if (row == maze.getSize() && col == maze.getSize()) {
                mp3.close();
                JOptionPane.showMessageDialog(null, "The End!!!", "BYE", JOptionPane.PLAIN_MESSAGE);
                return true; //path has reached goal
            }

            try { Thread.sleep(speedSolver); }
            catch (InterruptedException e) { }

            if (maze.getRoom(row, col).getEastExit() != null)
                if (solveMaze(row,col+1))
                    return true;

            if (maze.getRoom(row, col).getSouthExit() != null)
                if (solveMaze(row+1,col))
                    return true;

            if (maze.getRoom(row, col).getNorthExit() != null)
                if (solveMaze(row-1,col))
                    return true;

            if (maze.getRoom(row, col).getWestExit() != null)
                if (solveMaze(row,col-1))
                    return true;

            // maze can't be solved from this cell, so backtract out of the cell
            mazeSolver[row][col] = -1;   // mark cell as having been visited
            renderer.renderRoom(maze.getRoom(row, col));
            renderer.refresh();

            try { Thread.sleep(speedSolver); }
            catch (InterruptedException e) { }
        }
        return false;
    }

    /**
     * Reads the user input from the keyboard and returns the corresponding directions.
     * @return A boolean array containing true or false values that indicate the direction the user wants to go.
     */
    private boolean[] getKey()
    {
        boolean[] directions;

        if(getPlayers() == 1)
            directions = new boolean[4]; // north, south, east, west
        else
            directions = new boolean[8]; // north, south, east, west, north2, south2, east2, west2

        for (int i = 0; i < directions.length; i++)
            directions[i] = false;

        try {
            Key move = canvas.getLastKey();
            // moving the person

            if (move == Key.DOWN)
            {
                directions[1] = true;
                canvas.resetKey();
            }
            else if (move == Key.UP)
            {
                directions[0] = true;
                canvas.resetKey();
            }
            else if (move == Key.LEFT)
            {
                directions[3] = true;
                canvas.resetKey();
            }
            else if (move == Key.RIGHT)
            {
                directions[2] = true;
                canvas.resetKey();
            }
            else if (move == Key.SPACE && getPlayers() == 1)
            {
                if (!usedSpace)
                {
                    JOptionPane.showMessageDialog(null, "Kaboom!", "SUPERPOWER!", JOptionPane.PLAIN_MESSAGE);                    
                    usedSpace = true;
                    breakWalls();

                    renderer.refresh();
                }
                else {
                    JOptionPane.showMessageDialog(null, "You have already used your superpower...", "SORRY!", JOptionPane.PLAIN_MESSAGE);
                }

                canvas.resetKey();
            }
            else if (move == Key.S) {
                directions[5] = true;
                canvas.resetKey();
            }
            else if (move == Key.W) {
                directions[4] = true;
                canvas.resetKey();
            }
            else if (move == Key.A) {
                directions[7] = true;
                canvas.resetKey();
            }
            else if (move == Key.D) {
                directions[6] = true;
                canvas.resetKey();
            }
            else if (move == Key.F) {
                godMode += "F";
                canvas.resetKey();
            }
            else if (move == Key.I) {
                godMode += "I";
                canvas.resetKey();
            }
            else if (move == Key.L) {
                godMode += "L";
                canvas.resetKey();
            }
            else if (move == Key.P) {
                godMode += "P";
                canvas.resetKey();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return directions;
    }

    /**
     * Create evil demon circles randomly somewhere from 2,2 to 23,23 in the maze.
     * @param n The number of evil circles to be created.
     */
    private void createEvilCircles(int n) {
        ex = new int[n];
        ey = new int[n];

        for (int i = 0; i < n; i++)
        {
            ex[i] = Maze.randomNum(23) + 2;
            ey[i] = Maze.randomNum(23) + 2;
            maze.getRoom(ex[i], ey[i]).setEvilInRoom(i + 1, true);
            renderer.renderRoom(maze.getRoom(ex[i], ey[i]));      
        }
    }

    /**
     * Create the triangle power-ups randomly somewhere from 2,2 to 23,23 in the maze.
     */
    private void createPowerUps() {
        powerUpX = new int[6];
        powerUpY = new int[6];

        for (int i = 0; i < 6; i++)
        {
            powerUpX[i] = Maze.randomNum(23) + 2;
            powerUpY[i] = Maze.randomNum(23) + 2;
            if (i < 2)
                maze.getRoom(powerUpX[i], powerUpY[i]).setKillEvilCircleInRoom(i + 1, true);
            else if (i < 4)
                maze.getRoom(powerUpX[i], powerUpY[i]).setInvincibilityInRoom(i - 1, true);
            else if (i < 6)
                maze.getRoom(powerUpX[i], powerUpY[i]).setDoubleSpeedInRoom(i - 3, true);

            renderer.renderRoom(maze.getRoom(powerUpX[i], powerUpY[i]));    
        }
    }

    /**
     * When called, breaks the walls around the player's current room. 
     * Does not break the boundary walls on the edges of the maze though.
     */
    private void breakWalls() {
        Room r = maze.getRoom(x, y);
        Room rNorth = maze.getRoom(x-1, y);
        Room rEast = maze.getRoom(x, y+1);
        Room rSouth = maze.getRoom(x+1, y);
        Room rWest = maze.getRoom(x, y-1);

        // r.setExits(maze.getRoom(x-1, y), maze.getRoom(x, y+1), maze.getRoom(x+1, y), maze.getRoom(x, y-1));
        // new version does boundary checking before breaking the wall

        if (rNorth.getX() > 0) {
            r.setExits(maze.getRoom(x - 1, y), r.getEastExit(), r.getSouthExit(), r.getWestExit());
            rNorth.setExits(rNorth.getNorthExit(),rNorth.getEastExit(),maze.getRoom(x, y),rNorth.getWestExit());

        }

        if (rEast.getY() < maze.getSize() + 1) {
            r.setExits(r.getNorthExit(), maze.getRoom(x, y + 1), r.getSouthExit(), r.getWestExit());
            rEast.setExits(rEast.getNorthExit(),rEast.getEastExit(),rEast.getSouthExit(),maze.getRoom(x, y));
        }

        if (rSouth.getX() < maze.getSize() + 1) {
            r.setExits(r.getNorthExit(), r.getEastExit(), maze.getRoom(x + 1, y), r.getWestExit());
            rSouth.setExits(maze.getRoom(x, y),rSouth.getEastExit(),rSouth.getSouthExit(),rSouth.getWestExit());
        }

        if (rWest.getY() > 0) {
            r.setExits(r.getNorthExit(), r.getEastExit(), r.getSouthExit(), maze.getRoom(x, y - 1));
            rWest.setExits(rWest.getNorthExit(),maze.getRoom(x, y),rWest.getSouthExit(),rWest.getWestExit());
        }

        renderer.renderRoomAfterSpace(r);
    }

    /**
     * Terminates the game if the player runs into one of the evil circles and is not invincible at the moment of the encounter.
     */
    private void detectLoss() {
        if (!isInvActive()){
            if (maze.getRoom(x,y).isEvilInRoom(1) || maze.getRoom(x,y).isEvilInRoom(2) || maze.getRoom(x,y).isEvilInRoom(3)) {
                JOptionPane.showMessageDialog(null, "Oh no! The evil demon caught you!", "OH NO!", JOptionPane.PLAIN_MESSAGE);
                playSoundEffect("lose");
                System.exit(0);
            }
        }        
    }

    /**
     * Plays a sound effect. 
     * Adapted from: https://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html, accessed on 08/31/2016, 
     * and http://stackoverflow.com/questions/6045384/playing-mp3-and-wav-in-java, accessed on 08/31/2016.
     * @param event A String describing the event that happened, so that it will play the correct sound effect.
     */
    private void playSoundEffect(String event)
    {
        mp3.close();
        mp3 = null;

        if (event.equals("win"))
            filename = "./sounds/win.wav"; //File originally from: https://www.freesound.org/people/LittleRobotSoundFactory/sounds/270319/, accessed on 10/31/2016.
        else if (event.equals("lose"))
            filename = "./sounds/evilLaugh.wav"; //File originally from: http://soundbible.com/tags-horror.html, accessed on 10/06/2016.

        try{
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Main.class.getResource(filename));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            //clip.loop(2); //https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Clip.html, accessed on 08/31/2016.
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch(Exception e){  
        }
    }
}
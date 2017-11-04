/**
 * A renderer that draws items on the Canvas.
 * Adapted from the work of Hani Safadi, Michael Kšlling, and David J. Barnes.
 * 
 * @author https://github.com/f-z
 * @version 2016.11.16
 */
public class Renderer
{
    private Canvas canvas;
    private int canvasSize; // canvas size in pixles
    int roomSize; // size of a room on canvas
    private final int strokeSize = 3; // thickness of walls
    private boolean autoRefresh = false; // when true the room/canvas will autorefresh after any change
    // use true for testing as it slows down rendering.

    /**
     * Constructor for objects of class Renderer
     * @param mazeSize The size of the maze as an integer
     */
    public Renderer(int mazeSize)
    {
        // initialise instance variables
        canvas = Canvas.getCanvas();
        canvasSize = Canvas.getSize();
        roomSize = canvasSize / mazeSize;
    }

    /**
     * Renders one room on canvas
     * @param r Room to be rendered
     */
    public void renderRoom(Room r)
    {
        // maping room location to canvas coordinates
        int xLoc = roomSize/2 + r.getY() * roomSize;
        int yLoc = roomSize/2 + r.getX() * roomSize;

        // render north wall if exists
        if(r.getNorthExit() == null) {
            for(int i=0; i<roomSize; i++) {
                Square s = new Square(i + xLoc, yLoc, strokeSize);
                s.makeVisible();
            }
        }

        // render south wall if exists
        if(r.getSouthExit() == null) {
            for(int i=0; i<roomSize; i++) {
                Square s = new Square(i + xLoc, yLoc + roomSize, strokeSize);
                s.makeVisible();
            }
        }    

        // render east wall if exists
        if(r.getEastExit() == null) {
            for(int i=0; i<roomSize; i++) {
                Square s = new Square(xLoc+ roomSize, yLoc + i, strokeSize);
                s.makeVisible();
            }
        } 

        // render west wall if exists
        if(r.getWestExit() == null) {
            for(int i=0; i<roomSize; i++) {
                Square s = new Square(xLoc , yLoc + i, strokeSize);
                s.makeVisible();
            }
        }

        // render Player 1 or Player 2, if they are in the room
        if(r.isPersonInRoom(1)) 
        {
            Person p = new Person(xLoc + roomSize / 2, yLoc + roomSize / 2, roomSize - (strokeSize+7));//strokeSize+7 default setting
            p.changeColor("blue");
            p.makeVisible();
        }
        else if (r.isPersonInRoom(2))
        {
            Person p = new Person(xLoc + roomSize / 2, yLoc + roomSize / 2, roomSize - (strokeSize+7));
            p.changeColor("green");
            p.makeVisible();
        }
        else // draw a white square (empty room)
        {
            Square s = new Square(xLoc + (strokeSize+1), yLoc+ (strokeSize+1), roomSize-strokeSize*2);
            s.changeColor("white");
            s.makeVisible();
        }

        // render the evil circles
        if (r.isEvilInRoom(1) || r.isEvilInRoom(2) || r.isEvilInRoom(3))
        {
            Circle c = new Circle((int)(xLoc+0.22*roomSize),(int)(yLoc+0.22*roomSize), (int)(roomSize/1.5));
            c.makeVisible();
        }

        // render the triangles that kill evil circles
        if (r.isKillEvilCircleInRoom(1) || r.isKillEvilCircleInRoom(2))
        {
            Triangle t = new Triangle(xLoc + roomSize / 2, yLoc + roomSize/4, roomSize - (strokeSize+7), roomSize - (strokeSize+7));
            t.makeVisible();
        }

        // render the triangles that make the player invincible
        if (r.isInvincibilityInRoom(1) || r.isInvincibilityInRoom(2))
        {
            Triangle t = new Triangle(xLoc + roomSize / 2, yLoc + roomSize/4, roomSize - (strokeSize+7), roomSize - (strokeSize+7));
            t.changeColor("blue");
            t.makeVisible();
        }

        // render the triangles that double player speed
        if (r.isDoubleSpeedInRoom(1) || r.isDoubleSpeedInRoom(2))
        {
            Triangle t = new Triangle(xLoc + roomSize / 2, yLoc + roomSize/4, roomSize - (strokeSize+7), roomSize - (strokeSize+7));
            t.changeColor("red");
            t.makeVisible();
        }

        if(autoRefresh) refresh();
    }

    /**
     * Renders 100 dots at random locations on canvas.
     * @param r Room to be rendered.
     */
    public void renderDots(Room r)
    {
        // maping room location to canvas coordinates
        int xLoc = roomSize/2 + r.getY() * roomSize;
        int yLoc = roomSize/2 + r.getX() * roomSize;

        Square s = new Square(xLoc + roomSize / 2 + (4 - Maze.randomNum(10)), yLoc + roomSize / 2 + (4 - Maze.randomNum(10)), strokeSize);
        s.changeColor("magenta");
        s.makeVisible();

        if(autoRefresh) refresh();
    }

    /**
     * Renders the current room on canvas after space has been hit,
     * so after the walls surrounding the current room are destroyed.
     * This method is only called when the walls break to make the animation better for the rest of the game.
     * @param r Room to be rendered.
     */
    public void renderRoomAfterSpace(Room r)
    {
        // maping room location to canvas coordinates
        int xLoc = roomSize/2 + r.getY() * roomSize;
        int yLoc = roomSize/2 + r.getX() * roomSize;

        // render exit to the north
        for(int i=2; i<roomSize-2; i++) {
            Square s = new Square(i + xLoc, yLoc, strokeSize);
            s.changeColor("white");
            s.makeVisible();
        }

        // render exit to the south
        for(int i=2; i<roomSize-2; i++) {
            Square s = new Square(i + xLoc, yLoc + roomSize, strokeSize);
            s.changeColor("white");
            s.makeVisible();
        }

        // render exit to the east
        for(int i=2; i<roomSize-2; i++) {
            Square s = new Square(xLoc+ roomSize, yLoc + i, strokeSize);
            s.changeColor("white");
            s.makeVisible();
        }

        // render exit to the west
        for(int i=2; i<roomSize-2; i++) {
            Square s = new Square(xLoc, yLoc + i, strokeSize);
            s.changeColor("white");
            s.makeVisible();
        }

        if(autoRefresh) refresh();
    }

    /**
     * Renders the boundary rooms around the maze.
     * @param r The boundary room to be rendered.
     */
    public void renderBoundaryRoom(Room r)
    {
        // maping room location to canvas coordinates
        int xLoc = roomSize/2 + r.getY() * roomSize;
        int yLoc = roomSize/2 + r.getX() * roomSize;

        renderRoom(r);
        
        Square s = new Square(xLoc, yLoc, roomSize);
        s.makeVisible();

        if(autoRefresh) refresh();
    }

    /**
     * Renders the exit room at the end of the maze (bottom right).
     * @param m The maze for which the exit room will be rendered.
     */
    public void renderExitRoom(Maze m)
    {
        Room exitRoom = m.getRoom(m.getSize(),m.getSize()+1);

        int xLoc = roomSize/2 + exitRoom.getY() * roomSize;
        int yLoc = roomSize/2 + exitRoom.getX() * roomSize;

        Square s = new Square(xLoc-10, yLoc, roomSize);
        s.changeColor("white");
        Square s2 = new Square(xLoc+roomSize-strokeSize, yLoc, roomSize);
        s2.changeColor("white");
        Square s3 = new Square(xLoc, yLoc, roomSize);
        s3.changeColor("white");

        s.makeVisible();
        s2.makeVisible();
        s3.makeVisible();
    }

    /**
     * Renders the whole maze.
     * @param m The maze to be rendered.
     */
    public void renderMaze(Maze m)
    {
        int size = m.getSize();

        for(int i = 0; i < m.getSize()+2; i++)
            for(int j = 0; j<m.getSize()+2; j++)
                renderRoom(m.getRoom(i,j));

        // rendering boundary rooms as solid walls
        for (int x = 0; x < m.getSize()+2; x++) {
            renderBoundaryRoom(m.getRoom(x,0));
            renderBoundaryRoom(m.getRoom(x,size+1));
        }
        for (int y = 0; y < m.getSize()+2; y++) {
            renderBoundaryRoom(m.getRoom(0,y));
            renderBoundaryRoom(m.getRoom(size+1,y));
        }

        renderExitRoom(m);

        if(autoRefresh) refresh();
    }

    /**
     * Refreshes the canvas (all changes will be reflected).
     */
    public void refresh() {
        canvas.refresh();
    }

    // setter & getter methods
    /**
     * @return autoRefresh Returns true if autorefresh is on.
     */
    public boolean getAutoRefresh() {
        return autoRefresh;
    }

    /**
     * @param autoRefresh Sets the autorefresh on or off (true or false).
     */
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    /**
     * Draws a yellow circle indicating the path of the automatic solution of the maze.
     * @param r The room that will be included in the path.
     */
    public void renderSolution(Room r) {
        //maping room location to canvas coordinates
        int xLoc = roomSize/2 + r.getY() * roomSize;
        int yLoc = roomSize/2 + r.getX() * roomSize;

        Circle c = new Circle((int)(xLoc+0.22*roomSize),(int)(yLoc+0.22*roomSize), (int)(roomSize/1.5));
        c.changeColor("yellow");
        c.makeVisible();
    }
}
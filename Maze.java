import java.util.Random;

/**
 * A class that generates a random maze procedurally.
 * Algorithm inspired by http://algs4.cs.princeton.edu/41graph/Maze.java.html.
 * 
 * Model part of the Game Engine Architecture.
 * 
 * @author https://github.com/f-z
 * @version 2016.11.11
 */

public class Maze
{
    private Room[][] rooms; // 2-d array for rooms in the maze
    private int size; // size of the maze, the maze is always square so size is the number of rows & the number of cols
    private boolean[][] visited;
    @SuppressWarnings("unused")
    private boolean autoRefresh; // auto refresh canvas
    
    /**
     * Constructor for objects of class Maze
     * @param size number of rooms in each row & col
     */
    public Maze(int size)
    {
        // create the rooms in the maze
        this.size = size;
        rooms = new Room[size+2][size+2];
        visited = new boolean[size+2][size+2];

        for(int i = 0; i < size+2; i++)
            for(int j = 0; j < size+2; j++)
                rooms[i][j] = new Room(i, j);

        // making the boundaries of the maze out of reach for the procedural maze generation algorithm
        // to avoid IndexOutOfBounds exception
        for (int x = 0; x < size+2; x++) {
            visited[x][0] = true;
            visited[x][size+1] = true;
        }
        
        for (int y = 0; y < size+2; y++) {
            visited[0][y] = true;
            visited[size+1][y] = true;
        }

        // initializing all rooms with no exits
        try
        {
            for(int i = 0; i < size+2; i++)
                for(int j = 0; j < size+2; j++) {

                    Room r = rooms[i][j];

                    r.setExits(null, null, null, null);

                }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }

        autoRefresh = false;
        // generate the maze starting from upper left
        generateMaze(1, 1);
    }

    /**
     * Procedural maze generation algorithm.
     */
    public void generateMaze(int x, int y)
    {
        // setting the visited value to true for the current room
        visited[x][y] = true;

        // while there is an unvisited neighbor
        while (!visited[x][y+1] || !visited[x+1][y] || !visited[x][y-1] || !visited[x-1][y]) {
            while (true) {
                // choose a random neighbor by picking a random integer from 0 to 3
                int r = randomNum(4);
                if (r == 0 && !visited[x][y+1]) {
                    rooms[x][y].setExits(rooms[x][y].getNorthExit(), rooms[x][y+1], rooms[x][y].getSouthExit(), rooms[x][y].getEastExit());
                    rooms[x][y+1].setExits(rooms[x][y+1].getNorthExit(), rooms[x][y+1].getEastExit(), rooms[x][y+1].getSouthExit(), rooms[x][y]);
                    generateMaze(x, y +1);
                    break;
                }
                else if (r == 1 && !visited[x+1][y]) {
                    rooms[x][y].setExits(rooms[x][y].getNorthExit(), rooms[x][y].getEastExit(), rooms[x+1][y], rooms[x][y].getWestExit());
                    rooms[x+1][y].setExits(rooms[x][y], rooms[x+1][y].getEastExit(), rooms[x+1][y].getSouthExit(), rooms[x+1][y].getWestExit());
                    generateMaze(x+1, y);
                    break;
                }
                else if (r == 2 && !visited[x][y-1]) {
                    rooms[x][y].setExits(rooms[x][y].getNorthExit(), rooms[x][y].getEastExit(), rooms[x][y].getSouthExit(), rooms[x][y-1]);
                    rooms[x][y-1].setExits(rooms[x][y-1].getNorthExit(), rooms[x][y], rooms[x][y-1].getSouthExit(), rooms[x][y-1].getWestExit());
                    generateMaze(x, y-1);
                    break;
                }
                else if (r == 3 && !visited[x-1][y]) {
                    rooms[x][y].setExits(rooms[x-1][y], rooms[x][y].getEastExit(), rooms[x][y].getSouthExit(), rooms[x][y].getWestExit());
                    rooms[x-1][y].setExits(rooms[x-1][y].getNorthExit(), rooms[x-1][y].getEastExit(), rooms[x][y], rooms[x-1][y].getWestExit());
                    generateMaze(x-1, y);
                    break;
                }
            }
        }

        // the final room is the special exit room
        Room exit = new Room(size,size+1);
        rooms[size][size].setExits(rooms[size][size].getNorthExit(), exit, rooms[size][size].getSouthExit(), rooms[size][size].getWestExit());
        exit.setExitRoom(true);
    }

    /**
     * @param x An int representing the x position of the room.
     * @param y An int representing the y position of the room.
     * @return Room Returns a specific room from the random maze.
     */
    public Room getRoom(int x, int y)
    {
        if (x >= 0 && x < rooms.length && y >= 0 && y < rooms[x].length)
            return rooms[x][y];
        else
            return null;
    }

    /**
     * @return size The size of the maze.
     */
    public int getSize() {return size;}

    // random number generator
    private static Random rng = new Random();

    /**
     * @param n The upper bound of the random number range.
     * @return num A random number within [0, n).
     */
    public static int randomNum(int n)
    {
        int num = rng.nextInt(n);
        return num;
    }
}
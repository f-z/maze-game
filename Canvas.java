import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Canvas is a class to allow for simple graphical drawing on a canvas.
 * This is a modification of the general purpose Canvas, specially made for 
 * the BlueJ "shapes" example. 
 *
 * Adapted from the work of Hani Safadi, Bruce Quig, and Michael Kolling.
 * 
 * @author https://github.com/f-z
 * @version 2016.11.16
 */

// declaring arrow keys for Player 1 movement and WSAD for Player 2 movement
// declaring space key for "breaking walls" superpower
// declaring NONE key for invalid input (all other keys)
// declaring extra keys for god mode input
enum Key {LEFT, RIGHT, UP, DOWN, SPACE, NONE, S, W, A, D, F, I, L, P}

public class Canvas  
{
    // Note: The implementation of this class (specifically the handling of
    // shape identity and colors) is slightly more complex than necessary. This
    // is done on purpose to keep the interface and instance fields of the
    // shape objects in this project clean and simple for educational purposes.

    private static Canvas canvas;
    private final static int size = 750;

    /**
     * Factory method to get the canvas object.
     */
    public static Canvas getCanvas()
    {
        if (canvas == null) {
            canvas = new Canvas("Terry Maze Game", size+2, size+2, Color.white);
        }
        canvas.setVisible(true);
        return canvas;
    }

    public static int getSize()
    {
        return size; 
    }

    //  ----- instance part -----

    private JFrame frame;
    private CanvasPane canvasPane; //extension of a JPanel
    private Graphics2D graphics;
    private Color backgroundColor;
    private Image canvasImage;
    private List<Object> objects;
    private HashMap<Object, ShapeDescription> shapes;
    private Key lastKey = Key.NONE;

    /**
     * Create a Canvas.
     * @param title    title to appear in Canvas Frame
     * @param width    the desired width for the canvas
     * @param height   the desired height for the canvas
     * @param bgColor  the desired background color of the canvas
     */
    private Canvas(String title, int width, int height, Color bgColor)
    {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasPane = new CanvasPane();
        frame.setContentPane(canvasPane);
        frame.setTitle(title);
        frame.setLocation(30, 30);
        canvasPane.setPreferredSize(new Dimension(width, height));
        backgroundColor = bgColor;
        frame.pack();
        objects = new ArrayList<Object>();
        shapes = new HashMap<Object, ShapeDescription>();
    }

    /**
     * Set the canvas visibility and brings canvas to the front of screen
     * when made visible. This method can also be used to bring an already
     * visible canvas to the front of other windows.
     * @param visible  boolean value representing the desired visibility of
     * the canvas (true or false) 
     */
    public void setVisible(boolean visible)
    {
        if(graphics == null) {
            // first time: instantiate the offscreen image and fill it with
            // the background color
            Dimension size = canvasPane.getSize();
            canvasImage = canvasPane.createImage(size.width, size.height);
            graphics = (Graphics2D)canvasImage.getGraphics();

            // draw loading screen
            graphics.setColor(Color.black);
            graphics.fillRect(0, 0, size.width, size.height);
            graphics.setColor(backgroundColor);
            Font fnt0 = new Font("arial", Font.BOLD, 50);
            graphics.setFont(fnt0);
            graphics.drawString("MAZE GAME", size.width/2-160, 190);
            Font fnt1 = new Font("arial", Font.BOLD, 30);
            graphics.setFont(fnt1);

            // draw instructions
            if(Main.getPlayers() == 1)
            {               
                graphics.drawString("Get to the exit", size.width/2-110, 310);
                graphics.drawString("Or collect all the dots", size.width/2-160, 350);
                graphics.drawString("Move with arrows", size.width/2-135, 450);
                graphics.drawString("Grab the triangle power ups", size.width/2-200, 540);
                graphics.drawString("But avoid the circles!", size.width/2-160, 580);
            }
            else if(Main.getPlayers() == 2) {
                graphics.drawString("Control Player 1 (Blue) with arrows", size.width/2-260, 360);
                graphics.drawString("Control Player 2 (Green) with W,A,S,D", size.width/2-280, 500);
            }
        }
        frame.setVisible(visible);
    }

    /**
     * Draw a given shape onto the canvas.
     * @param  referenceObject  an object to define identity for this shape
     * @param  color            the color of the shape
     * @param  shape            the shape object to be drawn on the canvas
     */
    // Note: this is a slightly backwards way of maintaining the shape
    // objects. It is carefully designed to keep the visible shape interfaces
    // in this project clean and simple for educational purposes.
    public void draw(Object referenceObject, String color, Shape shape)
    {
        objects.remove(referenceObject);   // just in case it was already there
        objects.add(referenceObject);      // add at the end
        shapes.put(referenceObject, new ShapeDescription(shape, color));
    }

    /**
     * Erase a given shape's from the screen.
     * @param  referenceObject  the shape object to be erased 
     */
    public void erase(Object referenceObject)
    {
        objects.remove(referenceObject);   // just in case it was already there
        shapes.remove(referenceObject);
    }

    /**
     * Set the foreground color of the Canvas.
     * @param  colorString   the new color for the foreground of the Canvas 
     */
    public void setForegroundColor(String colorString)
    {
        if (colorString.equals("red")) {
            graphics.setColor(new Color(235, 25, 25));
        }
        else if (colorString.equals("black")) {
            graphics.setColor(Color.black);
        }
        else if (colorString.equals("blue")) {
            graphics.setColor(new Color(30, 75, 220));
        }
        else if (colorString.equals("yellow")) {
            graphics.setColor(new Color(255, 230, 0));
        }
        else if (colorString.equals("green")) {
            graphics.setColor(new Color(80, 160, 60));
        }
        else if (colorString.equals("magenta")) {
            graphics.setColor(Color.magenta);
        }
        else if (colorString.equals("white")) {
            graphics.setColor(Color.white);
        }
        else {
            graphics.setColor(Color.black);
        }
    }

    /**
     * Wait for a specified number of milliseconds before finishing.
     * This provides an easy way to specify a small delay which can be
     * used when producing animations.
     * @param  milliseconds The number of milliseconds.
     */
    public void wait(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        } 
        catch (Exception e)
        {
            // ignoring exception at the moment
        }
    }

    /**
     * Refreshes the image by redrawing everything.
     */
    public void refresh()
    {
        redraw();
    }

    /**
     * Redraws all shapes currently on the Canvas.
     */
    private void redraw()
    {
        erase();
        for (Object o : objects) {
            shapes.get(o).draw(graphics);
        }
        canvasPane.repaint();
    }

    /**
     * Erases the whole canvas (does not repaint).
     */
    private void erase()
    {
        Color original = graphics.getColor();
        graphics.setColor(backgroundColor);
        Dimension size = canvasPane.getSize();
        graphics.fill(new Rectangle(0, 0, size.width, size.height));
        graphics.setColor(original);
    }

    /**
     * @return Key Last key pressed.
     */
    public Key getLastKey()
    {
        return lastKey;
    }

    /**
     * Resets the key after each input.
     */
    public void resetKey()
    {
        lastKey = Key.NONE;
    }

    /************************************************************************
     * Inner class CanvasPane - the actual canvas component contained in the
     * Canvas frame. This is essentially a JPanel with added capability to
     * refresh the image drawn on it.
     */
    private class CanvasPane extends JPanel
    {

        private static final long serialVersionUID = 1L;

        public CanvasPane() {
            setFocusable(true);
            requestFocusInWindow();

            addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        int key = e.getKeyCode();      

                        if (key == KeyEvent.VK_KP_LEFT || key == KeyEvent.VK_LEFT)
                        {
                            Canvas.this.lastKey = Key.LEFT;
                        }
                        else if (key == KeyEvent.VK_KP_RIGHT || key == KeyEvent.VK_RIGHT)
                        {
                            Canvas.this.lastKey = Key.RIGHT;
                        } else if (key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_UP)
                        {
                            Canvas.this.lastKey = Key.UP;
                        } else if (key == KeyEvent.VK_KP_DOWN || key == KeyEvent.VK_DOWN)
                        {
                            Canvas.this.lastKey = Key.DOWN;
                        } else if (key == KeyEvent.VK_SPACE)
                        {
                            Canvas.this.lastKey = Key.SPACE;
                        } 
                        else if (key == KeyEvent.VK_A)
                        {
                            Canvas.this.lastKey = Key.A;
                        } 
                        else if (key == KeyEvent.VK_D)
                        {
                            Canvas.this.lastKey = Key.D;
                        }
                        else if (key == KeyEvent.VK_W)
                        {
                            Canvas.this.lastKey = Key.W;
                        } else if (key == KeyEvent.VK_S)
                        {
                            Canvas.this.lastKey = Key.S;
                        }
                        else if (key == KeyEvent.VK_F)
                        {
                            Canvas.this.lastKey = Key.F;
                        }
                        else if (key == KeyEvent.VK_I)
                        {
                            Canvas.this.lastKey = Key.I;
                        }
                        else if (key == KeyEvent.VK_L)
                        {
                            Canvas.this.lastKey = Key.L;
                        }
                        else if (key == KeyEvent.VK_P)
                        {
                            Canvas.this.lastKey = Key.P;
                        }
                        else {
                            Canvas.this.lastKey = Key.NONE;
                        }
                    }
                });
        }

        /**
         * Draws the image of the canvas.
         * @param g The graphics component used.
         */
        public void paint(Graphics g)
        {
            g.drawImage(canvasImage, 0, 0, null);
        }
    }

    /************************************************************************
     * Inner class ShapeDescription - the actual shape of the object being drawn, 
     * along with its color.
     */
    private class ShapeDescription
    {
        private Shape shape;
        private String colorString;

        /**
         * Creates a shape with a given color.
         * @shape The shape that will be created.
         * @color The color that the shape will have.
         */
        public ShapeDescription(Shape shape, String color)
        {
            this.shape = shape;
            colorString = color;
        }

        /**
         * Draws the shape using the foreground color.
         * @param graphics The graphics component to be used in drawing the shape.
         */
        public void draw(Graphics2D graphics)
        {
            setForegroundColor(colorString);
            graphics.fill(shape);
        }
    }
}
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;

/**
 * A class implementing an MP3 player in Java.
 * This is used as the basis for a soundtrack within the game.
 * 
 * Adapted from http://introcs.cs.princeton.edu/java/faq/mp3/MP3.java.html, 
 * accessed on 10/06/2016.
 *
 * @author https://github.com/f-z
 * @version 2016.11.11
 */
public class MP3 {
    private String filename;
    private Player player; 

    /**
     * Constructor that takes the name of an MP3 file.
     * @param filename The file name of the MP3 that we want to play.
     */
    public MP3(String filename) {
        this.filename = filename;
    }

    /**
     * Closing the player.
     */
    public void close() {
        if (player != null) player.close();
    }

    /**
     * Determine if the player has reached the end of the song.
     * @return True if the song has ended, false otherwise.
     */
    public boolean hasEnded() {
            if (player.isComplete())
            return true;
            return false;
        }

    /**
     * Loads the MP3 file and plays it.
     */
    public void play() {
        try {
            FileInputStream stream = new FileInputStream(filename);
            BufferedInputStream buffStream = new BufferedInputStream(stream);
            player = new Player(buffStream);
        }
        catch (Exception e) {
            System.out.println("Problem playing file " + filename);
            System.out.println(e);
        }

        // run in new thread to play in background
        new Thread() {
            public void run() {
                try { player.play(); }
                catch (Exception e) { System.out.println(e); }
            }
        }
        .start();
    }
}
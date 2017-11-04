import javax.swing.*;

/**
 * Creates a new maze game and starts gameplay.
 * 
 * @author https://github.com/f-z
 * @version 2016.11.16
 */
public class Main
{
    static int players = 0;

    public static void main(String[] args)
    {
        String mode = "";

        JFrame frame = new JFrame("Player Mode");

        Object[] possibilities = {"1 Player", "2 Players"};
        
        // reads user input about player mode (1 or 2)
        mode = (String)JOptionPane.showInputDialog(
            frame,
            "1 player or 2 players?",
            "Player Mode",
            JOptionPane.PLAIN_MESSAGE,
            null,
            possibilities,
            "1 Player");

        try {
            mode = mode.substring(0, 1);

            players = Integer.parseInt(mode);

            Game game = new Game();

            game.setPlayers(players);

            game.interact();
        }
        catch (NullPointerException ex){
            
        }
    }

    /**
     * @return The number of players in the game (1 or 2).
     */
    public static int getPlayers()
    {
        return players;
    }
}
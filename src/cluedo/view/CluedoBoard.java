package cluedo.view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * This class represents the cluedo board for the the game
 * @author Patrick
 *
 */
public class CluedoBoard {
	
	private static final String IMAGE_PATH = "images/";
	
	// The board is made up of a 2D array of Jbuttons
	private JButton[][] boardSquares = new JButton[22][22];
	// field to store the board panel
	private JPanel board;

	public CluedoBoard(String boardFile, CluedoFrame frame) {
		// initlizise the board squares from file
		initSquares(boardFile);
		// initlizise the board
		initBoard(frame);
	}
	
	/**
	 * Scans a text file and constucts a 2D array
	 * 	of board squares
	 * @param boardFile
	 */
	private void initSquares(String boardFile){
		Scanner scanner = null;
		try{
			// Creating the scanner on the board file
			scanner = new Scanner(new File(boardFile));
			for(int height=0; scanner.hasNextLine(); height++){
				// reading a line of text
				char[] line = scanner.nextLine().toCharArray();
				for(int width = 0; width < line.length; width++){
					// read the text character	
					char c = line[width];
					JButton b;
					// If character is a digit, then make tile a player starting location
					if(Character.isDigit(c)){
						// creating a character token in this tile
						b = initCharacterToken(c);
					} else {
						// get the tile represented by the character
						b = getTile(c);
					}
					// set the tile on the board
					boardSquares[width][height] = b;	
				}
			}
		} catch(IOException e){
			// board reading failed
			System.out.println("Board file reading fail: " + e.getMessage());
		}
	}
	
	/**
	 * This method creates the character token images and
	 *  sets the tool tip text.
	 * @param c
	 * @return board square jbutton
	 */
	private JButton initCharacterToken(char c) {
		JButton b = new HallwayTile('H');
		int playerID = Character.digit(c, 10);
		switch(playerID){
			case 1:
				b.setIcon(new ImageIcon(IMAGE_PATH + "scarlett.png"));
				b.setToolTipText("MISS SCARLETT");
				break;
			case 2:
				b.setIcon(new ImageIcon(IMAGE_PATH + "mustard.png"));
				b.setToolTipText("COLONEL MUSTARD");
				break;
			case 3:
				b.setIcon(new ImageIcon(IMAGE_PATH + "green.png"));
				b.setToolTipText("THE REVERENED GREEN");
				break;
			case 4:
				b.setIcon(new ImageIcon(IMAGE_PATH + "peacock.png"));
				b.setToolTipText("MRS PEACOCK");
				break;
			case 5:
				b.setIcon(new ImageIcon(IMAGE_PATH + "plum.png"));
				b.setToolTipText("PROFESSOR PLUM");
				break;
			case 6:
				b.setIcon(new ImageIcon(IMAGE_PATH + "white.png"));
				b.setToolTipText("MRS WHITE");
				break;
		}
		return b;
	}

	/**
	 * Initlises the cluedo board and adds it to
	 * 	the parnet board frame
	 * @param frame
	 */
	private void initBoard(CluedoFrame frame) {
		// Setting the parent frame border
		frame.getGui().setBorder(new EmptyBorder(6, 12, 6, 12));
		// Setting up the board
		board = new JPanel(new GridLayout(22, 22));
        board.setBorder(new LineBorder(Color.BLACK));
        // Adding the board to the frame
        frame.getGui().add(board);
        // Adding all the board squares to the board
        for (int ii = 0; ii < boardSquares.length; ii++) {
            for(int jj = 0; jj < boardSquares[ii].length; jj++) {
            	board.add(boardSquares[jj][ii]);
            }
        }
	}

	/**
	 * Gets a board tile from a character
	 * @param c
	 * @return board tile
	 */
	private JButton getTile(char c) {
		JButton b = new JButton();
		switch(c){
		case 'C':
		case 'I':
		case 'L':
		case 'S':
		case 'B':
		case 'A':
		case 'K':
		case 'D':
		case 'O':
			b = new RoomTile(c);
			break;
		case 'd':
			b = new DoorwayTile(c);
			break;
		case 'X':
			b = new WallTile(c);
			break;
		case 'H':
			b = new HallwayTile(c);
			break;
		}
		// tiles are 24x24 px in size 
		ImageIcon icon = new ImageIcon(
		                new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB));
		b.setIcon(icon);
		b.setPreferredSize(new Dimension(24, 24));
		return b;
	}

}

package cluedo.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cluedo.model.Card;
import cluedo.model.CharacterToken;
import cluedo.model.CluedoGame;
import cluedo.model.Position;
import cluedo.view.CluedoBoard;

/**
 * Interacts with the players and handles actions.
 * @author Patrick and Maria
 *
 */
@SuppressWarnings("serial")
public class CluedoFrame extends JFrame implements MouseListener, KeyListener{

	private static final String IMAGE_PATH = "images/";

	// Stores the panel which holds the game board gui
	private JPanel gui = new JPanel(new BorderLayout(3, 3));
	// Stores the game board
	private CluedoBoard board;
	// Stores the game
	private CluedoGame game;
	private Movement movement;
	
	// Player UI panel
	private JPanel playerControls;
	
	// Game information text area
	private JTextArea gameTextArea;
	
	// Stores the current dice roll
	private int firstDie;
	private int secondDie;

	// Storex the current player
	public CharacterToken player;
	private boolean newPlayer = false;

	public CluedoFrame(String boardFile){
		super("Cluedo");

		firstDie = 3;
		secondDie = 4;

		// setup menu
		initMenu();
		// setup game board
		initBoard(boardFile);
		// setup player UI
		player = null;
		newPlayer = false;
		initPlayerUI();

		// setting title
		setTitle("Cluedo Game");
		// set size
		setSize(700, 700);
		// set display location
		setLocationRelativeTo(null);
		// set close operation
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		// pack to minimum size
		pack();
		// enforce minimum size
		setMinimumSize(getSize());
		// handles the closing of the game
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt){
                confirmExit();
            }
        });
		// set move
		movement = new Movement(this, board);
	}

	/**
	 * Creates the menu bar for the game
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		ImageIcon iconExit = new ImageIcon(IMAGE_PATH + "exit.png");
		ImageIcon iconNew = new ImageIcon(IMAGE_PATH + "new.png");
		ImageIcon iconHelp = new ImageIcon(IMAGE_PATH + "help.png");
		// creating game and help menus
		JMenu menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M);
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);

		// creating the view help menu item
		JMenuItem hMenuItem = new JMenuItem("View Help", iconHelp);
		hMenuItem.setMnemonic(KeyEvent.VK_H);
		hMenuItem.setToolTipText("Click for Game Help");
		hMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// displays a help message to user
				displayHelp();
			}
		});
		// creating new game and exit menu items
		JMenuItem nMenuItem = new JMenuItem("New Game", iconNew);
		nMenuItem.setMnemonic(KeyEvent.VK_N);
		nMenuItem.setToolTipText("Click to start a new Game");
		nMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO close current game; start new game
			}
		});
		JMenuItem eMenuItem = new JMenuItem("Exit", iconExit);
		eMenuItem.setMnemonic(KeyEvent.VK_E);
		eMenuItem.setToolTipText("Exit App");
		eMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmExit();
			}
		});
		// adding menu and help menus
		menu.add(nMenuItem);
		menu.addSeparator();
		menu.add(eMenuItem);
		help.add(hMenuItem);
		// adding menus to menubar
		menuBar.add(menu);
		menuBar.add(help);
		// set the menu bar
		setJMenuBar(menuBar);
	}

	/**
	 * Initialises the game board
	 * @param boardFile
	 */
	private void initBoard(String boardFile) {
		board = new CluedoBoard(boardFile, this);
		add(gui);
	}

	/**
	 * Initialises the game's player user interface
	 */
	private void initPlayerUI(){
		// Creating a panel to store the UI
		playerControls = new JPanel();
		playerControls.setBorder(
		   BorderFactory.createCompoundBorder(
				      BorderFactory.createEmptyBorder(0,12,2,12),
				      BorderFactory.createLineBorder(Color.BLACK, 1)
				   )
				);

		// Creating roll panel
		JPanel rollPnl = initRollPnl();

		// Creating a panel to display game information
//		JPanel gameInfoPnl = new JPanel();
		JTabbedPane gameInfoPnl = new JTabbedPane(); // two panes - game info and cards
		gameInfoPnl.setPreferredSize(null);
		
		// Creating a text area to display game information to the user
		gameTextArea = initGameTextArea();
		setText("");

		// Adding the text area to the panel
//		gameInfoPnl.add(gameTextArea, BorderLayout.CENTER);
		gameInfoPnl.add("Game Info",gameTextArea);

		if(player!=null){
			// Adding hand to panel
			JScrollPane handPnl = initHandPnl(); //FIXME: cards too wide
			gameInfoPnl.addTab("Hand", handPnl);
		}


		// Creating a panel to display the current players options
		JPanel gameOptionsPnl = new JPanel(new GridLayout(0,1,5,5));
		gameOptionsPnl.setBorder(new EmptyBorder(0,2,0,4));

		// Creating buttons for the options
		JButton beginBtn = new JButton("Begin.");
		JButton suggestBtn = new JButton("Suggest / Accuse.");
		JButton endTurnBtn = new JButton("End Turn.");
		JButton quitBtn = new JButton("Quit.");

		/*
		 * Adds ActionListeners to buttons
		 */
		// Begins the game
		beginBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(game.getActivePlayers()==null)
					System.out.println("no players");
				else{
					suggestBtn.setEnabled(true);
					endTurnBtn.setEnabled(true);
					player = game.getActivePlayers().get(0);
					newPlayer = true;
					redrawPlayerControls();
				}
			}
		});
		// Opens suggest dialog box
		suggestBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// check if player can suggest
				suggest();
			}});

		// Starts next player's turn
		endTurnBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// get next player
				if(player.getUid()<game.getActivePlayers().size()){
					player = game.getActivePlayers().get(player.getUid()); // player's uid is 1-6
				}
				else{
					player = game.getActivePlayers().get(0);
				}
				// draw next player's UI
//				System.out.println("Current player's id: " + player.getUid());
//				System.out.println("Next player's id: " + nextPlayer.getUid());
				newPlayer = true;
				redrawPlayerControls();
			}
		});

		// Ends game
		quitBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmExit();
			}});


		// Adding buttons to panel and enabling the relevant ones
		if(player!=null){
			beginBtn.setEnabled(false);
		}
		else{
			gameOptionsPnl.add(beginBtn);
			suggestBtn.setEnabled(false);
			endTurnBtn.setEnabled(false);
		}
		gameOptionsPnl.add(suggestBtn);
		gameOptionsPnl.add(endTurnBtn);
		gameOptionsPnl.add(quitBtn);


		// adding the roll panel to the player controls panel
		playerControls.add(rollPnl, BorderLayout.WEST);
		// adding the game info panel to the player controls panel
		playerControls.add(gameInfoPnl, BorderLayout.CENTER);
		// adding the game options panel to the player controls UI
		playerControls.add(gameOptionsPnl, BorderLayout.EAST);
		// adding the player controls UI to the bottom of the window
		add(playerControls, BorderLayout.SOUTH); // adds playerUI to frame
//		repaint();
	}

	/*-------------------------------------
	 *   Suggestion/accusation methods
	 -------------------------------------*/
	/**
	 * Displays a suggestion dialogue.
	 */
	private void suggest(){
		//TODO: suggestion and accusation
		Suggestion suggestion = new Suggestion(this);
		setText(suggestion.getPlayerSuggestion() + "\n" + suggestion.getResult());
//		suggestion.dispose();
	}
	
	/**
	 * Gets character to move the suspect and weapon into the suspected crime scene.
	 * @param suspect
	 * @param weapon
	 * @param room
	 */
	public void moveSuggestionItems(String suspect, String weapon, String room){
		CharacterToken suspectToken = getSuspectToken(suspect);
	}

	/**
	 * Returns the character token associated with this character name.
	 * @param suspect
	 * @return
	 */
	private CharacterToken getSuspectToken(String suspect){
		for(CharacterToken t: getPlayers()){
			if(t.getCharacter().toString().equals(suspect))
				return t;
		}
		return null;
	}
	
	/**
	 * Creates the game text area to display messages to the player.
	 * @param msg
	 * @return
	 */
	private JTextArea initGameTextArea(){
		gameTextArea = new JTextArea(4, 28);
		gameTextArea.setEditable(false);
		gameTextArea.setBorder(
				   BorderFactory.createCompoundBorder(
						      BorderFactory.createTitledBorder(new LineBorder(Color.BLACK,1),
						    		  "<html><b><u>GAME INFO</u></b></html>"), // using html tags to underline text
						      BorderFactory.createEmptyBorder(4,4,2,2)
						   )
						);
		return gameTextArea;
	}

	/**
	 * Changes the message in the game text area.
	 * @param msg
	 */
	private void setText(String msg){
		if(msg.equals("")){
			if(player==null){
				msg = "Roll the dice then either use the arrow keys to move\nor click on the tile you want to move to.\n";
			}
			else if(newPlayer){
				msg = player.getName() + "\nRoll the dice to move or select another option.";
			}
			else{
				msg = player.getName() + ": " + player.getStepsRemaining() + " moves left.\n" +
						"Use arrow keys to move or click on the board.";
			}
		}
		gameTextArea.setText(msg);
	}
	
	private void appendText(String msg){
		gameTextArea.append("\n" + msg);
	}

	/**
	 * Creates the roll panel with the dice and the roll button.
	 * @param player
	 * @param newPlayer
	 * @return
	 */
	private JPanel initRollPnl(){
		// Creating a panel to store the dice images and roll button
		JPanel rollPnl = new JPanel(new GridLayout(0,1,2,2));
		rollPnl.setBorder(new EmptyBorder(0,4,0,2));
		JPanel dicePnl = new JPanel();
		// Creating the dice images
		JLabel dice1 = getDiceImage(firstDie);
		dice1.setBorder(new LineBorder(Color.BLACK));
		JLabel dice2 = getDiceImage(secondDie);
		dice2.setBorder(new LineBorder(Color.BLACK));
		dicePnl.add(dice1);
		dicePnl.add(dice2);

		// Adding dice and roll button to the roll panel
		JButton rollBtn = new JButton("Roll.");
		if(!newPlayer)
			rollBtn.setEnabled(false);
		else
			rollBtn.setEnabled(true);

		rollPnl.add(dicePnl);
		rollPnl.add(rollBtn);

		rollBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				rollDice(); // set player's steps and dice pictures
				newPlayer = false;
				redrawPlayerControls();
			}
		});

		return rollPnl;
	}

	/**
	 * Creates panel for all the player's cards
	 */
	private JScrollPane initHandPnl(){
		JScrollPane hand = new JScrollPane();
		JPanel handPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hand.setAutoscrolls(true);
		hand.setViewportView(handPnl);
		hand.setPreferredSize(new Dimension(300, 100));
//		List<BufferedImage> cards = new ArrayList<BufferedImage>();
//		try {
//			cards.add(ImageIO.read(new File(IMAGE_PATH + "scarlett-card.png")));
//			cards.add(ImageIO.read(new File(IMAGE_PATH + "scarlett-card.png")));
//			cards.add(ImageIO.read(new File(IMAGE_PATH + "scarlett-card.png")));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		for(BufferedImage img: cards){
//			JLabel picLabel = new JLabel(new ImageIcon(img));
//			handPnl.add(picLabel);
//		}
		for(Card c: player.getHand()){
			JLabel picLabel = new JLabel(new ImageIcon(c.getImage()));
			handPnl.add(picLabel);
		}
		return hand;
	}

	/**
	 * Rolls the dice and sets the player's initial amount of steps.
	 * @param player
	 */
	private void rollDice(){
		firstDie = (int)(Math.random() * 6) + 1;
		secondDie = (int)(Math.random() * 6) + 1;
		if(player!=null)
			player.setStepsRemaining(firstDie + secondDie);
	}

	/**
	 * Returns a dice image to match the roll amount.
	 * @param roll
	 * @return
	 */
	private JLabel getDiceImage(int roll){
		try{
			switch(roll){
				case 1:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice1.png"))));
				case 2:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice2.png"))));
				case 3:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice3.png"))));
				case 4:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice4.png"))));
				case 5:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice5.png"))));
				case 6:
					return new JLabel(new ImageIcon(ImageIO.read(new File(IMAGE_PATH + "dice6.png"))));
			}
		} catch (IOException e1) { e1.printStackTrace(); }
		return null;
	}
	
	/**
	 * Displays dialog asking if user wants to exit the game
	 */
	private void confirmExit() {
		String msg = "Are You Sure You Want to Exit the Game?" ;
		int result = JOptionPane.showConfirmDialog(this, msg,
		        "Alert", JOptionPane.OK_CANCEL_OPTION);
		if(result==0){
			System.exit(0);
			dispose();
		}
	}

	/**
	 * Displays a help dialog message to the player
	 */
	protected void displayHelp() {
		String msg = "-- Cluedo Game Help -- \n" + "Select New Game to restart the game." ;
		JFrame helpPanel = new JFrame();
		JPanel pnl = (JPanel) helpPanel.getContentPane();
		JOptionPane.showMessageDialog(pnl, msg,
                "Cluedo Game Guide", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Redraws the player controls panel.
	 * @param player
	 * @param newPlayer
	 */
	private void redrawPlayerControls(){
		remove(playerControls); // remove the old panel
		initPlayerUI(); // reset the player UI
		revalidate(); // draw
	}

	/*
	 * Move methods
	 */

	@Override
	public void keyPressed(KeyEvent e) {
		if(player.getStepsRemaining()>0){
			if(e.getKeyCode() == KeyEvent.VK_UP)
				movement.moveNorth(player);
			else if(e.getKeyCode() == KeyEvent.VK_DOWN)
				movement.moveSouth(player);
			else if(e.getKeyCode() == KeyEvent.VK_LEFT)
				movement.moveWest(player);
			else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
				movement.moveEast(player);
		}
		else{
			setText("Please roll the dice.");
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(!movement.move(player, new Position(e.getX()/24, e.getY()/24)))
			invalidMoveDialog();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		/*
		boolean moved = movement.move(player, new Position(e.getX(), e.getY()));
		if(!moved){
			invalidMoveDialog();
		}
		*/
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	@Override
	public void keyReleased(KeyEvent e) {}

	public void invalidMoveDialog(){
		String msg = "Cannot move to that tile." ;
		int result = JOptionPane.showConfirmDialog(this, msg,
		        "Alert", JOptionPane.OK_CANCEL_OPTION);
	}
	/*--------------------------
	 *  Getter/setter methods
	 -------------------------*/

	/**
	 * Sets the reference to the CluedoGame.
	 * 	FIXME: is this used? Yep in main class
	 * @param game
	 */
	public void setGame(CluedoGame game){
		this.game = game;
	}

	/**
	 * Returns the main panel.
	 * @return
	 */
	public JPanel getGui() {
		return gui;
	}

	/**
	 * Returns the board associated with this frame.
	 * @return
	 */
	public CluedoBoard getBoard(){
		return board;
	}

	/**
	 * Returns the list of players.
	 * @return
	 */
	public List<CharacterToken> getPlayers() {
		return game.getActivePlayers();
	}
}

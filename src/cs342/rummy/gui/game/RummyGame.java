package cs342.rummy.gui.game;

import processing.core.*;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

// +-----------------+
// | RUMMYGAME CLASS |
// +-----------------+

public class RummyGame extends PApplet {
	
	private static final long serialVersionUID = 7575294182736689370L;
	
	// +-----------+
	// | CONSTANTS |
	// +-----------+
	
	public static final float
	H = (float) 1.0,
	W = (float) 1.6,
	WIDTH_OVER_HEIGHT_RATIO = W / H;
	
	private static final String
	CARD_IMAGE_PATH = "img/cards/",
	BUTTON_IMAGE_PATH = "img/buttons/";
	
	private static final int
	RUN_MELD = 1,
	SET_MELD = 0,
	NOT_MELD = -1;
	
	private static final int
	DRAW_PHASE = 1,
	PLAY_PHASE = 2;
	
	private static final int[]
	CARDS_PER_PLAYER = new int[] { -1, -1, 10, 7, 7, 6, 6 };
	
	private static final float[]
	PLAYER_X_POS = new float[] {
		(float) (0.1 * W),
		(float) (0.3 * W),
		(float) (0.5 * W),
		(float) (0.7 * W),
		(float) (0.9 * W),
		(float) (0.5 * W)
	},
	PLAYER_Y_POS = new float[] {
		(float) (0.12 * H),
		(float) (0.12 * H),
		(float) (0.12 * H),
		(float) (0.12 * H),
		(float) (0.12 * H),
		(float) (0.75 * H)
	};
	
	private static final float
	USER_X_POS = (float) (0.5 * W),
	USER_Y_POS = (float) (0.9 * H),
	STOCK_PILE_X_POS = (float) (0.04 * W),
	STOCK_PILE_Y_POS = (float) (0.5 * H),
	STOCK_PILE_SCALE = (float) 0.15,
	DISCARD_PILE_X_POS = (float) (0.96 * W),
	DISCARD_PILE_Y_POS = (float) (0.5 * H),
	DISCARD_PILE_SCALE = (float) 0.15,
	MELD_AREA_X_POS = (float) (0.12 * W),
	MELD_AREA_Y_POS = (float) (0.35 * H),
	MELD_AREA_WIDTH = (float) (0.74 * W),
	MELD_AREA_HEIGHT = (float) (0.30 * H),
	MELD_AREA_CARD_SCALE = (float) (0.09 * H);
	
	private static final int
	SORT_SUIT_BUTTON_INDEX = 0,
	SORT_RANK_BUTTON_INDEX = 1,
	MELD_BUTTON_INDEX = 2,
	START_BUTTON_INDEX = 3,
	DEALER_BUTTON_INDEX = 4,
	LEAVE_BUTTON_INDEX = 5,
	DISCARD_BUTTON_INDEX = 6,
	JOIN_BUTTON_INDEX = 7;
	
	// +--------------+
	// | STATIC SETUP |
	// +--------------+
	
	private static boolean cardImagesLoaded = false;
	
	private static synchronized void loadImages(RummyGame game) {
		if (cardImagesLoaded)
			return;
		cardImagesLoaded = true;
		int i = 0;
		for (Cards.Suit suit : Cards.Suit.values())
			for (Cards.Rank rank : Cards.Rank.values())
				Cards.setCardImage(game.loadImage(CARD_IMAGE_PATH
						+ rank.SYMBOL + suit.SYMBOL + ".png"), i++);
		for (i = 0; i < 7; i++)
			Cards.setCoverImage(game.loadImage(CARD_IMAGE_PATH + "BACK-"
					+ i + ".png"), i);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "SORT_SUIT" + ".png"), SORT_SUIT_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "SORT_RANK" + ".png"), SORT_RANK_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "MELD" + ".png"), MELD_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "START" + ".png"), START_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "JOIN" + ".png"), LEAVE_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "DEALER" + ".png"), DEALER_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "DISCARD" + ".png"), DISCARD_BUTTON_INDEX);
		Button.setImage(game.loadImage(BUTTON_IMAGE_PATH + "JOIN" + ".png"), JOIN_BUTTON_INDEX);
	}
	
	// +------------------------+
	// | TESTING STATIC SECTION |
	// +------------------------+
	
	private static RummyGame tempGame;
	private static volatile boolean newEvent;
	private static volatile Event rumEv;
	
	public static void main(String[] args) {
		JFrame window = new JFrame("TEST");
		tempGame = new RummyGame("DEMO", new Listener() {
			@Override
			public void rummyEventOccured(Object event) {
				rumEv = (Event)event;
				newEvent = true;
				System.out.println("Event: " + rumEv.TYPE);
			}
		});
		
		tempGame.setPreferredSize(new Dimension(800, 500));
		tempGame.resize(new Dimension(800, 500));
		tempGame.init();
		window.setContentPane(tempGame);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		
		while (true) {
			if (newEvent) {
				newEvent = false;
				long time = System.currentTimeMillis() + 50;
				while (time > System.currentTimeMillis());
				tempGame.handleRummyEvent(rumEv);
			}
		}
	}
	
	
	
	// +--------------------+
	// | LISTENER INTERFACE |
	// +--------------------+
	
	public interface Listener {
		public void rummyEventOccured(Object event);
	}
	
	
	
	// +--------+
	// | EVENTS |
	// +--------+
	
	private static class Event implements Serializable {
		
		private static final long serialVersionUID = 1891846418602200671L;
		private final EventType TYPE;
		private Event(EventType type) {
			TYPE = type;
		}
	}
	
	private enum EventType {
		PLAYER_JOIN, MOUSE_CLICKED, GAME_START, CARD_DRAW, CARD_DISCARD, MELD, APPEND
	}
	
	private static class JoinEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = 2663677004975207720L;
		private final String PLAYER_NAME;
		private JoinEvent(EventType type, String playerName) {
			super(type);
			PLAYER_NAME = playerName;
		}
	}
	
	private static class StartEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = 5269906958899614412L;
		private final int DEALER_INDEX;
		private final List<Cards.Rank> START_RANKS;
		private final List<Cards.Suit> START_SUITS; 
		private StartEvent(EventType type, int dealerIndex, List<Cards.Rank> startRanks, List<Cards.Suit> startSuits) {
			super(type);
			DEALER_INDEX = dealerIndex;
			START_RANKS = startRanks;
			START_SUITS = startSuits;
		}
	}
	
	private static class ClickEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = -6248588640955445630L;
		private final float X, Y;
		private ClickEvent(EventType type, float x, float y) {
			super(type);
			X = x;
			Y = y;
		}
	}
	
	private static class DrawEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = 8517870409711306471L;
		private final boolean STOCK_PILE_DRAW;
		private DrawEvent(EventType type, boolean stockPileDraw) {
			super(type);
			STOCK_PILE_DRAW = stockPileDraw;
		}
	}
	
	private static class DiscardEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = 7332473902772629464L;
		private final Cards.Rank RANK;
		private final Cards.Suit SUIT;
		private DiscardEvent(EventType type, Cards.Rank rank, Cards.Suit suit) {
			super(type);
			RANK = rank;
			SUIT = suit;
		}
	}
	
	private static class MeldEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = 3682921760441228113L;
		private final List<Cards.Rank> RANKS;
		private final List<Cards.Suit> SUITS;
		private MeldEvent(EventType type, List<Cards.Rank> meldRanks, List<Cards.Suit> meldSuits) {
			super(type);
			RANKS = meldRanks;
			SUITS = meldSuits;
		}
	}
	
	private static class AppendEvent extends Event implements Serializable {
		
		private static final long serialVersionUID = -7856099598013129504L;
		private final List<Cards.Rank> RANKS;
		private final List<Cards.Suit> SUITS;
		private AppendEvent(EventType type, List<Cards.Rank> meldRanks, List<Cards.Suit> meldSuits) {
			super(type);
			RANKS = meldRanks;
			SUITS = meldSuits;
		}
	}
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	private final String USERNAME;
	private final Listener LISTENER;
	private final Motion MOTION = new Motion();
	
	private final Cards.Deck DECK;
	private final Cards.Stack DECK_STACK = new Cards.Stack();
	private final Cards.Stack STOCK_PILE = new Cards.Stack();
	private final Cards.Stack DISCARD_PILE = new Cards.Stack();
	private final List<Cards.Stack>	RUN_MELDS = new ArrayList<Cards.Stack>();
	private final List<Cards.Stack> SET_MELDS = new ArrayList<Cards.Stack>();
	private final List<Cards.Card> SELECTED_CARDS = new ArrayList<Cards.Card>();
	
	private final List<Player> PLAYERS = new ArrayList<Player>(6);
	private String joinRequestName = null;
	private int userPlayerIndex = -1;
	private int currentPlayerIndex = -1;
	private int currentDealerIndex = -1;
	private int currentTurnPhase = 0;
			
	float WIDTH, HEIGHT, SCALE;
	private final Button SORT_RANK_BUTTON;
	private final Button SORT_SUIT_BUTTON;
	private final Button MELD_BUTTON;
	private final Button START_BUTTON;
	private final Button DEALER_BUTTON;
	private final Button LEAVE_BUTTON;
	private final Button DISCARD_BUTTON;
	private final Button JOIN_BUTTON;
		
	private String statusText;
	private boolean gameInProgress = false;
	
	// +-------------+
	// | CONSTRUCTOR |
	// +-------------+
	
	public RummyGame(String username, Listener listener) {
		USERNAME = username;
		LISTENER = listener;
		
		loadImages(this);
		DECK = new Cards.Deck(MOTION);
		
		SORT_SUIT_BUTTON = new Button(SORT_SUIT_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.07, (float) 0.89, (float) 0.05, 0));
		SORT_RANK_BUTTON = new Button(SORT_RANK_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.07, (float) 0.95, (float) 0.05, 0));
		MELD_BUTTON = new Button(MELD_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.5 * W, (float) 0.5 * H, (float) 0.25, 0));
		START_BUTTON = new Button(START_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.5 * W, (float) 0.5 * H, (float) 0.25, 0));
		DEALER_BUTTON = new Button(DEALER_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.5 * W, (float) 0.6 * H, (float) 0.1, 0));
		LEAVE_BUTTON = new Button(LEAVE_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.5 * W, (float) 0.5 * H, (float) 0.1, 0));
		DISCARD_BUTTON = new Button(DISCARD_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.9 * W, (float) 0.7 * H, (float) 0.1, 0));
		JOIN_BUTTON = new Button(JOIN_BUTTON_INDEX,
				MOTION.new MotionObject((float) 0.5 * W, (float) 0.5 * H, (float) 0.25, 0));
		JOIN_BUTTON.visible = true;
	}
	
	// +------------------+
	// | INTERNAL METHODS |
	// +------------------+
	
	public void setup() {
		size(400, 300);
		//smooth();
		float xpos = 0;
		float dxpos = W / 52;
		for (int i = 0; i < DECK.CARDS.length; i++) {
			DECK.CARDS[i].moveTo(DECK_STACK);
			DECK.CARDS[i].MOTION_OBJECT.set(xpos, (float) -0.25, (float) 0.5, (float) (Math.random() * 2 * Math.PI));
			xpos += dxpos;
		}
		resetCards();		
		statusText("Network Rummy ::: designed by Tom, Andy, and Titus");
	}
	
	public void draw() {
		background(0);
		
		if (height * W/H > width) {
			WIDTH = width;
			HEIGHT = (int) (width * H/W);
		}
		else {
			WIDTH = (int) (height * W/H);
			HEIGHT = height;
		}
		SCALE = HEIGHT;
		scale(SCALE);
		
		MOTION.step();
				
		for (int p = 0; p < PLAYERS.size(); p++) {
			Player player = PLAYERS.get(p);
			if (player instanceof UserPlayer == false) {
				player.draw(this);
				for (int i = 0; i < player.HAND.size(); i++)
					player.HAND.get(i).draw(this);
			}
		}
		
		for (int i = 0; i < DECK_STACK.size(); i++)
			DECK_STACK.get(i).draw(this);
		for (int i = 0; i < STOCK_PILE.size(); i++)
			STOCK_PILE.get(i).draw(this);
		for (int i = 0; i < DISCARD_PILE.size(); i++)
			DISCARD_PILE.get(i).draw(this);
		for (int m = 0; m < RUN_MELDS.size(); m++)
			for (int i = 0; i < RUN_MELDS.get(m).size(); i++)
				RUN_MELDS.get(m).get(i).draw(this);
		for (int m = 0; m < SET_MELDS.size(); m++)
			for (int i = 0; i < SET_MELDS.get(m).size(); i++)
				SET_MELDS.get(m).get(i).draw(this);
		
		if (userPlayerIndex >= 0 ) {
			UserPlayer userPlayer = (UserPlayer) PLAYERS.get(userPlayerIndex);
			for (int i = 0; i < userPlayer.HAND.size(); i++)
				userPlayer.HAND.get(i).draw(this);
			userPlayer.draw(this);
		}
		
		if (SORT_SUIT_BUTTON.visible)	SORT_SUIT_BUTTON.draw(this);
		if (SORT_RANK_BUTTON.visible)	SORT_RANK_BUTTON.draw(this);
		if (MELD_BUTTON.visible)		MELD_BUTTON.draw(this);
		if (START_BUTTON.visible)		START_BUTTON.draw(this);
		if (DEALER_BUTTON.visible)		DEALER_BUTTON.draw(this);
		if (LEAVE_BUTTON.visible)		LEAVE_BUTTON.draw(this);
		if (DISCARD_BUTTON.visible)		DISCARD_BUTTON.draw(this);
		if (JOIN_BUTTON.visible)		JOIN_BUTTON.draw(this);
		
		drawStatusBar();
		
		fill(255);
		noStroke();
		resetMatrix();
		if (HEIGHT < height)
			rect(0, HEIGHT, width, height - HEIGHT);
		if (WIDTH < width)
			rect(WIDTH, 0, width - WIDTH, height);
	}
	
	
	void drawStatusBar() {
		pushMatrix();
		
		fill(128,64,32);
		noStroke();
		rect(0, 0, W, (float) 0.08);
		stroke(255,128,64);
		strokeWeight((float) 0.005);
		line(0, (float) 0.08, W, (float) 0.08);
		
		translate((float) (0.01 * W), (float) (0.04 * H));
		textAlign(RummyGame.LEFT);
		fill(255,128,64);
		
		drawText(statusText, W, (float) (0.08 * H));
		
		popMatrix();
	}
	
	
	void drawText(String text, float maxWidth, float maxHeight) {
		pushMatrix();
		
		float textSize = 16;
		textSize(textSize);
		scale(Math.min(maxWidth / (textWidth(text) + 10), maxHeight / (textAscent() + textDescent())));
		text(text, 0, 1 + textDescent());
		
		popMatrix();
	}
	
	private void statusText(String newStatusText) {
		statusText = newStatusText;
	}
	
	public int isValidMeld() {
		if(SELECTED_CARDS.size() <= 2)
			return NOT_MELD;
		
		Collections.sort(SELECTED_CARDS, Cards.SUIT_FIRST_COMPARATOR);
		Cards.Card card1 = SELECTED_CARDS.get(0);
		Cards.Card card2 = SELECTED_CARDS.get(1);
		
		if (card1.RANK == card2.RANK) {
			for (int i = 2; i < SELECTED_CARDS.size(); i++) {
				card1 = card2;
				card2 = SELECTED_CARDS.get(i);
				if (card1.RANK != card2.RANK)
					return NOT_MELD;
			}
			return SET_MELD;
		}
		if (card1.RANK.compareTo(card2.RANK) == 1 && card1.SUIT.compareTo(card2.SUIT) == 0) {
			for (int i = 2; i < SELECTED_CARDS.size(); i++) {
				card1 = card2;
				card2 = SELECTED_CARDS.get(i);
				if (card1.RANK.compareTo(card2.RANK) != 1 || card1.SUIT.compareTo(card2.SUIT) != 0)
					return NOT_MELD;
			}
			return RUN_MELD;
		}
		return NOT_MELD;
	}
	
	public void dealPlayers(int cardsPerPlayer) {
		int delay = 0;
		Cards.Card card;
		for (int i = 0; i < cardsPerPlayer; i++) {
			for (Player player : PLAYERS) {
				card = STOCK_PILE.CARDVIEW.get(STOCK_PILE.size() - 1);
				card.MOTION_OBJECT.setMotionDelay(delay);
				delay += 7;
				card.moveTo(player.HAND);
				if (player instanceof UserPlayer)
					card.setFaceUp();
				else
					card.setFaceDown();
			}
		}
		for (Player player : PLAYERS) {
			player.spreadHand();
			for (Cards.Card c : player.HAND.CARDVIEW) {
				System.out.print(" " + c);
			}
			System.out.println(" :)");
		}
		
		card = STOCK_PILE.CARDVIEW.get(STOCK_PILE.size() - 1);
		moveToDiscardPile(card, delay + 20, 30);
	}
	
	private void moveToDiscardPile(Cards.Card card, int delay, int steps) {
		card.MOTION_OBJECT.setMotionDelay(delay);
		card.MOTION_OBJECT.setMotionSteps(steps);
		card.MOTION_OBJECT.set((float) (DISCARD_PILE_X_POS + DISCARD_PILE.size() * 0.00025),
							   (float) (DISCARD_PILE_Y_POS - DISCARD_PILE.size() * 0.00015), DISCARD_PILE_SCALE, 0);
		card.moveTo(DISCARD_PILE);
		card.setFaceUp();
	}
	
	private void moveToMeld(Cards.Card card, Cards.Stack meld, boolean isSetMeld) {
		card.moveTo(meld);
		card.setFaceUp();
		card.MOTION_OBJECT.setMotionSteps(30);
		float x = (float) (MELD_AREA_X_POS + MELD_AREA_WIDTH * card.RANK.ordinal() / 12.0);
		float y = (float) (MELD_AREA_Y_POS + MELD_AREA_HEIGHT * card.SUIT.ordinal() / 3.0);
		card.MOTION_OBJECT.set(x, y, MELD_AREA_CARD_SCALE, (float) (isSetMeld ? 0 : Math.PI / 2));
	}
	
	private void selectUserCard(Cards.Card card) {
		SELECTED_CARDS.add(card);		
		card.highlight(true);

	}
	
	private void deselectUserCard(Cards.Card card) {
		SELECTED_CARDS.remove(card);
		card.highlight(false);
	}
	
	private Cards.Card findCardInStack(Cards.Rank rank, Cards.Suit suit, Cards.Stack stack) {
		for (int i = 0; i < stack.size(); i++) {
			Cards.Card card = stack.get(i);
			if (card.RANK == rank && card.SUIT == suit)
				return card;
		}
		return null;
	}
	
	private void resetCards() {
		DECK.reset();
		int delay = 0;
		float xpos = 0;
		float dxpos = W / 52;
		Cards.Card card = null;
		while ((card = DECK.pop()) != null) {
			card.moveTo(DECK_STACK);
			card.MOTION_OBJECT.set(xpos, (float) -0.25, (float) 0.5, (float) (Math.random() * 2 * Math.PI));
			card.MOTION_OBJECT.setMotionDelay(delay++);
			card.MOTION_OBJECT.setMotionSteps(30);
			card.setFaceUp();
			float x = (float) (MELD_AREA_X_POS + MELD_AREA_WIDTH * card.RANK.ordinal() / 12.0);
			float y = (float) (MELD_AREA_Y_POS + MELD_AREA_HEIGHT * card.SUIT.ordinal() / 3.0);
			card.MOTION_OBJECT.set(x, y, MELD_AREA_CARD_SCALE, (float) (Math.random() < 0.5 ? 0 : Math.PI / 2));
			xpos += dxpos;
		}
	}
	
	private void resetStockPile() {
		DECK_STACK.shuffle();
		int i = 0;
		while (DECK_STACK.size() > 0) {
			Cards.Card card = DECK_STACK.get(DECK_STACK.size() - 1);
			card.moveTo(STOCK_PILE);
			card.setFaceDown();
			card.MOTION_OBJECT.setMotionDelay(i);
			card.MOTION_OBJECT.setMotionSteps(30);
			card.MOTION_OBJECT.set((float) (STOCK_PILE_X_POS + i * 0.00025),
								   (float) (STOCK_PILE_Y_POS - i * 0.00015), STOCK_PILE_SCALE, 0);
			i++;
		}
	}
	
	// +------------------+
	// | EXTERNAL METHODS |
	// +------------------+
	
	public boolean requestToJoin(String requestedName) {
		if (gameInProgress) {
			statusText("You can't join when a game is in progress");
			return false;
		}
		if (joinRequestName != null)  {
			statusText("Still waiting for the first request confirmation...");
			return false;
		}
		joinRequestName = null;
		requestedName = requestedName.trim();
		if (userPlayerIndex >= 0) {
			if (requestedName.equals(PLAYERS.get(userPlayerIndex).NAME))
				statusText("You're already in the game, silly");
			else
				statusText("You can't join again with a different name");
			return false;
		}
		if (PLAYERS.size() >= 6) {
			statusText("Sorry, " + requestedName + ", but this game is full :(");
			return false;
		}
		joinRequestName = requestedName;
		statusText("Join request accepted; waiting for confirmation...");
		LISTENER.rummyEventOccured(new JoinEvent(EventType.PLAYER_JOIN, requestedName));
		return true;
	}
	
	
	
	public void mousePressed() {
		float x = mouseX / (float) SCALE;
		float y = mouseY / (float) SCALE;
		
		LISTENER.rummyEventOccured(new ClickEvent(EventType.MOUSE_CLICKED, x, y));
		
		if (JOIN_BUTTON.visible && JOIN_BUTTON.isButtonArea(x, y)) {
			boolean joinRequestResult = requestToJoin(USERNAME);
			if (joinRequestResult == true)
				JOIN_BUTTON.visible = false;
			return;
		}
		if (LEAVE_BUTTON.visible && LEAVE_BUTTON.isButtonArea(x, y)) {
			handlePlayerLeaving(userPlayerIndex);
			return;
		}
		if (!gameInProgress) {
			if (START_BUTTON.visible && START_BUTTON.isButtonArea(x, y)) {
				List<Cards.Rank> startRanks = new ArrayList<Cards.Rank>(STOCK_PILE.size());
				List<Cards.Suit> startSuits = new ArrayList<Cards.Suit>(STOCK_PILE.size());
				for (Cards.Card card : STOCK_PILE.CARDVIEW) {
					startRanks.add(card.RANK);
					startSuits.add(card.SUIT);
				}
				LISTENER.rummyEventOccured(new StartEvent(EventType.GAME_START,
						(int)(Math.random() * PLAYERS.size()), startRanks, startSuits));
			}
			return;
		}
		if (SORT_RANK_BUTTON.visible && SORT_RANK_BUTTON.isButtonArea(x, y)) {
			PLAYERS.get(userPlayerIndex).HAND.sortByRankFirst();
			PLAYERS.get(userPlayerIndex).spreadHand();
			return;
		}
		if (SORT_SUIT_BUTTON.visible && SORT_SUIT_BUTTON.isButtonArea(x, y)) {
			PLAYERS.get(userPlayerIndex).HAND.sortBySuitFirst();
			PLAYERS.get(userPlayerIndex).spreadHand();
			return;
		}
		if (DISCARD_BUTTON.visible && DISCARD_BUTTON.isButtonArea(x, y)) {
			Cards.Card card = SELECTED_CARDS.get(0);
			LISTENER.rummyEventOccured(new DiscardEvent(EventType.CARD_DISCARD, card.RANK, card.SUIT));
			return;
		}
		if (MELD_BUTTON.visible && MELD_BUTTON.isButtonArea(x, y)) {
			if (currentPlayerIndex == userPlayerIndex) {
				List<Cards.Rank> meldRanks = new ArrayList<Cards.Rank>();
				List<Cards.Suit> meldSuits = new ArrayList<Cards.Suit>();
				for (int i = 0; i < SELECTED_CARDS.size(); i++) {
					meldRanks.add(SELECTED_CARDS.get(i).RANK);
					meldSuits.add(SELECTED_CARDS.get(i).SUIT);
				}
				LISTENER.rummyEventOccured(new MeldEvent(EventType.MELD, meldRanks, meldSuits));
			}
			else
				statusText("BUG");
			return;
		}
		
		for (Cards.Card card : PLAYERS.get(userPlayerIndex).HAND.CARDVIEW) {
			if (card.isCardArea(x, y)) {
				if (card.isHighlighted())
					deselectUserCard(card);
				else
					selectUserCard(card);
				if (currentTurnPhase == PLAY_PHASE) {
					MELD_BUTTON.visible = (isValidMeld() != NOT_MELD);
					DISCARD_BUTTON.visible = SELECTED_CARDS.size() == 1;
				}
				return;
			}
		}
		if (currentPlayerIndex != userPlayerIndex)
			return;
		if (currentTurnPhase == DRAW_PHASE) {
			boolean stockPileDraw = true;
			Cards.Card card = STOCK_PILE.get(STOCK_PILE.size() - 1);
			if (card.isCardArea(x, y) == false) {
				stockPileDraw = false;
				card = DISCARD_PILE.get(DISCARD_PILE.size() - 1);
				if (card.isCardArea(x, y) == false)
					return;
			}
			LISTENER.rummyEventOccured(new DrawEvent(EventType.CARD_DRAW, stockPileDraw));
			return;
		}
		if (currentTurnPhase == PLAY_PHASE) {
			if (SELECTED_CARDS.isEmpty())
				return;
			for (int m = 0; m < RUN_MELDS.size(); m++) {
				Cards.Stack meld = RUN_MELDS.get(m);
				for (int i = 0; i < meld.size(); i++) {
					Cards.Card card = meld.get(i);
					if (card.isCardArea(x, y)) {
						SELECTED_CARDS.addAll(meld.CARDVIEW);
						boolean validAppend = isValidMeld() == RUN_MELD;
						SELECTED_CARDS.removeAll(meld.CARDVIEW);
						if (validAppend) {
							List<Cards.Rank> appendRanks = new ArrayList<Cards.Rank>();
							List<Cards.Suit> appendSuits = new ArrayList<Cards.Suit>();
							for (int a = 0; a < SELECTED_CARDS.size(); a++) {
								appendRanks.add(SELECTED_CARDS.get(a).RANK);
								appendSuits.add(SELECTED_CARDS.get(a).SUIT);
							}
							appendRanks.add(meld.get(0).RANK);
							appendSuits.add(meld.get(0).SUIT);
							LISTENER.rummyEventOccured(new AppendEvent(EventType.APPEND, appendRanks, appendSuits));
						}
					}
				}
			}
			for (int m = 0; m < SET_MELDS.size(); m++) {
				Cards.Stack meld = SET_MELDS.get(m);
				for (int i = 0; i < meld.size(); i++) {
					Cards.Card card = meld.get(i);
					if (card.isCardArea(x, y)) {
						SELECTED_CARDS.addAll(meld.CARDVIEW);
						boolean validAppend = isValidMeld() == SET_MELD;
						SELECTED_CARDS.removeAll(meld.CARDVIEW);
						if (validAppend) {
							List<Cards.Rank> appendRanks = new ArrayList<Cards.Rank>();
							List<Cards.Suit> appendSuits = new ArrayList<Cards.Suit>();
							for (int a = 0; a < SELECTED_CARDS.size(); a++) {
								appendRanks.add(SELECTED_CARDS.get(a).RANK);
								appendSuits.add(SELECTED_CARDS.get(a).SUIT);
							}
							appendRanks.add(meld.get(0).RANK);
							appendSuits.add(meld.get(0).SUIT);
							LISTENER.rummyEventOccured(new AppendEvent(EventType.APPEND, appendRanks, appendSuits));
						}						
					}
				}
			}
			return;
		}
	}
	
	
	
	public void keyPressed() {
		if (key == 's') {
			PLAYERS.get(userPlayerIndex).HAND.sortBySuitFirst();
			PLAYERS.get(userPlayerIndex).spreadHand();
		}
		else if (key == 'r') {
			PLAYERS.get(userPlayerIndex).HAND.sortByRankFirst();
			PLAYERS.get(userPlayerIndex).spreadHand();
		}
		else if (key == 'u') requestToJoin("MeMeMe");
		else if (key == 'i') requestToJoin("MeAgain");
		else if (key == 'q') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Queefus"));
		else if (key == 'a') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Asshole"));
		else if (key == 'z') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Zebra"));
		else if (key == 'x') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Xenu"));
		else if (key == 'c') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Cunt"));
		else if (key == 'v') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Vagina"));
		else if (key == 'b') handleRummyEvent(new JoinEvent(EventType.PLAYER_JOIN, "Bieber"));
		else if (key == 'p') resetCards();
		else if (key == 'n') {
			PLAYERS.get(currentPlayerIndex).highlighted = false;
			currentPlayerIndex = userPlayerIndex;
			PLAYERS.get(currentPlayerIndex).highlighted = true;
		}
	}
	
	
	
	public void handleRummyEvent(Object eventObject) {
		Event event = (Event)eventObject;
		System.out.println("RECEIVED RUMMY EVENT: " + event.TYPE);
		
		if (event.TYPE == EventType.PLAYER_JOIN) {
			JoinEvent je = (JoinEvent) event;
			if (gameInProgress) {
				statusText("Much Late! Join request denied for: " + je.PLAYER_NAME);
				return;
			}
			for (Player player : PLAYERS)
				if (je.PLAYER_NAME.equals(player.NAME)) {
					statusText("Join request denied for: " + je.PLAYER_NAME);
					return;
				}
			if (je.PLAYER_NAME.equals(joinRequestName)) {
				statusText("You have now joined the game");
				Player userPlayer = new UserPlayer(joinRequestName, USER_X_POS, USER_Y_POS);
				userPlayerIndex = PLAYERS.size();
				joinRequestName = null;
				PLAYERS.add(userPlayer);
				if (PLAYERS.size() == 1)
					resetStockPile();
			}
			else if (PLAYERS.size() >= 6) {
				statusText("Much full! Join request denied for: " + je.PLAYER_NAME);
				return;
			}
			else {
				statusText(je.PLAYER_NAME + " has joined");
				int p = PLAYERS.size();
				if (userPlayerIndex >= 0)
					p--;
				PLAYERS.add(new Player(je.PLAYER_NAME, PLAYER_X_POS[p], PLAYER_Y_POS[p]));
				if (PLAYERS.size() == 1)
					resetStockPile();
				if (PLAYERS.size() >= 2 && userPlayerIndex == 0)
					START_BUTTON.visible = true;
				if (PLAYERS.size() == 6)
					joinRequestName = null;
			}
		}
		else if (event.TYPE == EventType.GAME_START) {
			StartEvent se = (StartEvent) event;
			handleStartGame(se.DEALER_INDEX, se.START_RANKS, se.START_SUITS);
		}
		else if (event.TYPE == EventType.CARD_DRAW) {
			DrawEvent de = (DrawEvent) event;
			handleCardDraw(de.STOCK_PILE_DRAW);
		}
		else if (event.TYPE == EventType.CARD_DISCARD) {
			DiscardEvent de = (DiscardEvent) event;
			handleDiscard(de.RANK, de.SUIT);
		}
		else if (event.TYPE == EventType.MELD) {
			MeldEvent me = (MeldEvent) event;
			handleMeld(me.RANKS, me.SUITS);
		}
		else if (event.TYPE == EventType.APPEND) {
			AppendEvent ae = (AppendEvent) event;
			handleAppend(ae.RANKS, ae.SUITS);
		}
		else if (event.TYPE == EventType.MOUSE_CLICKED) {
			ClickEvent ce = (ClickEvent) event;
			System.out.println("Received click: " + ce.X + " , " + ce.Y);
		}
	}
	
	private void handleDiscard(Cards.Rank rank, Cards.Suit suit) {
		Player player = PLAYERS.get(currentPlayerIndex);
		Cards.Card card = findCardInStack(rank, suit, player.HAND);
		if (card == null)
			return;
		moveToDiscardPile(card, 0, 30);
		player.spreadHand();
		if (currentPlayerIndex == userPlayerIndex) {
			deselectUserCard(card);
			MELD_BUTTON.visible = false;
			DISCARD_BUTTON.visible = false;
		}
		player.highlighted = false;
		currentPlayerIndex = (currentPlayerIndex + 1) % PLAYERS.size();
		PLAYERS.get(currentPlayerIndex).highlighted = true;
		currentTurnPhase = DRAW_PHASE;
	}
	
	private void handlePlayerLeaving(int playerIndex) {
		Player player = PLAYERS.get(playerIndex);
		statusText("TODO: " + player.NAME + " left... but not really...");
	}
	
	private void handleStartGame(int dealerIndex, List<Cards.Rank> startRanks, List<Cards.Suit>startSuits) {
		if (gameInProgress)
			return;
		gameInProgress = true;
		START_BUTTON.visible = false;
		SORT_RANK_BUTTON.visible = true;
		SORT_SUIT_BUTTON.visible = true;
		currentDealerIndex = dealerIndex;
		currentPlayerIndex = (currentDealerIndex + 1) % PLAYERS.size();
		PLAYERS.get(currentPlayerIndex).highlighted = true;
		
		int i = 0;
		for (Cards.Card card : STOCK_PILE.CARDVIEW) {
			Cards.Rank rank = startRanks.get(i);
			Cards.Suit suit = startSuits.get(i);
			card.RANK = rank;
			card.SUIT = suit;
			card.IMAGE_INDEX = Cards.getImageIndex(rank, suit);
			i++;
		}
		
		dealPlayers(CARDS_PER_PLAYER[PLAYERS.size()]);
		currentTurnPhase = DRAW_PHASE;
	}
	
	private void handleCardDraw(boolean stockPileDraw) {
		if (currentTurnPhase != DRAW_PHASE)
			return;
		currentTurnPhase = PLAY_PHASE;
		Player player = PLAYERS.get(currentPlayerIndex);
		Cards.Card card = null;
		if (stockPileDraw)
			card = STOCK_PILE.get(STOCK_PILE.size() - 1);
		else
			card = DISCARD_PILE.get(DISCARD_PILE.size() - 1);
		card.moveTo(player.HAND);
		if (player instanceof UserPlayer)
			card.setFaceUp();
		else
			card.setFaceDown();
		player.spreadHand();
	}
	
	private void handleMeld(List<Cards.Rank> meldRanks, List<Cards.Suit> meldSuits) {
		List<Cards.Card> meldCards = new ArrayList<Cards.Card>();
		Player player = PLAYERS.get(currentPlayerIndex);
		for (int i = 0; i < meldRanks.size(); i++) {
			Cards.Card card = findCardInStack(meldRanks.get(i), meldSuits.get(i), player.HAND);
			if (card != null)
				meldCards.add(card);
		}
		if (meldCards.size() < 3)
			return;
		Cards.Stack newMeld = new Cards.Stack();
		boolean isSetMeld = meldRanks.get(0) == meldRanks.get(1);
		if (isSetMeld)
			SET_MELDS.add(newMeld);
		else
			RUN_MELDS.add(newMeld);
		for (int i = 0; i < meldCards.size(); i++) {
			Cards.Card card = meldCards.get(i);
			moveToMeld(card, newMeld, isSetMeld);
			MELD_BUTTON.visible = false;
			player.spreadHand();
		}
		if (currentPlayerIndex == userPlayerIndex)
			for (int i = SELECTED_CARDS.size(); i --> 0;)
				deselectUserCard(SELECTED_CARDS.get(i));
	}
	
	private void handleAppend(List<Cards.Rank> meldRanks, List<Cards.Suit> meldSuits) {
		Cards.Rank searchRank = meldRanks.get(meldRanks.size() - 1);
		Cards.Suit searchSuit = meldSuits.get(meldSuits.size() - 1);
		Cards.Stack theMeld = null;
		boolean setMeld = false;
		for (int m = 0; m < SET_MELDS.size(); m++) {
			Cards.Stack meld = SET_MELDS.get(m);
			Cards.Card card = findCardInStack(searchRank, searchSuit, meld);
			if (card != null) {
				theMeld = meld;
				setMeld = true;
				break;
			}
		}
		if (theMeld == null) {
			for (int m = 0; m < RUN_MELDS.size(); m++) {
				Cards.Stack meld = RUN_MELDS.get(m);
				Cards.Card card = findCardInStack(searchRank, searchSuit, meld);
				if (card != null) {
					theMeld = meld;
					setMeld = false;
					break;
				}
			}
		}
		if (theMeld != null) {
			for (int a = 0; a < meldRanks.size() - 1; a++) {
				Cards.Card appendix = findCardInStack(meldRanks.get(a), meldSuits.get(a), PLAYERS.get(currentPlayerIndex).HAND);
				moveToMeld(appendix, theMeld, setMeld);
			}
			if (currentPlayerIndex == userPlayerIndex)
				for (int i = SELECTED_CARDS.size(); i --> 0;)
					deselectUserCard(SELECTED_CARDS.get(i));
		}
	}
}

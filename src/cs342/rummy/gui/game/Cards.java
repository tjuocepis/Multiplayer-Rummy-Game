package cs342.rummy.gui.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.*;

public class Cards implements Serializable {
	
	private static final long serialVersionUID = 4367613140954156584L;
	
	
	
	private static final List<PImage> CARD_IMAGES = new ArrayList<PImage>(52);
	private static final List<PImage> COVER_IMAGES = new ArrayList<PImage>(7);
	
	public static void setCardImage(PImage image, int cardIndex) {
		while (cardIndex >= CARD_IMAGES.size())
			CARD_IMAGES.add(null);
		CARD_IMAGES.set(cardIndex, image);
	}
	
	public static void setCoverImage(PImage image, int cardIndex) {
		while (cardIndex >= COVER_IMAGES.size())
			COVER_IMAGES.add(null);
		COVER_IMAGES.set(cardIndex, image);
	}
	
	public static int getImageIndex(Rank rank, Suit suit) {
		return suit.ordinal() * 13 + rank.ordinal();
	}
	
	
	
	
	
	public enum Suit {
		SPADES('S'), HEARTS('H'), DIAMONDS('D'), CLUBS('C');
		public final char SYMBOL;
		private Suit(char s) {
			SYMBOL = s;
		}
	}
	
	public enum Rank {
		KING('K'), QUEEN('Q'), JACK('J'), TEN('T'),
		NINE('9'), EIGHT('8'), SEVEN('7'), SIX('6'), FIVE('5'),
		FOUR('4'), THREE('3'), TWO('2'), ACE('A');
		public final char SYMBOL;
		private Rank(char s) {
			SYMBOL = s;
		}
	}
	
	
	
	
	
	static final Comparator<Card> RANK_FIRST_COMPARATOR = new Comparator<Card>() {
		@Override
		public int compare(Card cardB, Card cardA) {
			int result = cardA.RANK.compareTo(cardB.RANK);
			if (result == 0)
				return cardA.SUIT.compareTo(cardB.SUIT);
			return result;
		}
	};
	
	static final Comparator<Card> SUIT_FIRST_COMPARATOR = new Comparator<Card>() {
		@Override
		public int compare(Card cardB, Card cardA) {
			int result = cardA.SUIT.compareTo(cardB.SUIT);
			if (result == 0)
				return cardA.RANK.compareTo(cardB.RANK);
			return result;
		}
	};
	
	
	
	
	
	public static class Deck {
		
		final Card[] CARDS = new Card[52];
		private byte topCard;
		
		public Deck(Motion motion) {
			int coverIndex = (int) (Math.random() * 7);
			for (Suit suit : Suit.values())
				for (Rank rank : Rank.values())
					CARDS[topCard++] = new Card(rank, suit,
							getImageIndex(rank, suit), coverIndex,
							motion.new MotionObject(0, 0, (float) 0.1, 0));
		}
		
		public void shuffle() {
			Collections.shuffle(Arrays.asList(CARDS));
			topCard = 52;
		}
		
		public void reset() {
			topCard = 52;
		}
		
		public Card pop() {
			return topCard > 0 ? CARDS[--topCard] : null;
		}
	}
	
	
	
	
	
	public static class Stack implements Serializable {
		
		private static final long serialVersionUID = -7827964726479944353L;
		public final List<Card> CARDVIEW;
		private final List<Card> CARDS;
		
		public Stack() {
			CARDS = new ArrayList<Card>();
			CARDVIEW = Collections.unmodifiableList(CARDS);
		}
		
		public boolean isEmpty() {
			return CARDS.isEmpty();
		}
		
		public int size() {
			return CARDS.size();
		}
		
		public Card get(int index) {
			if (index >= 0 && index < CARDS.size())
				return CARDS.get(index);
			else
				return null;
		}
		
		public void sortByRankFirst() {
			Collections.sort(CARDS, RANK_FIRST_COMPARATOR);
		}
		
		public void sortBySuitFirst() {
			Collections.sort(CARDS, SUIT_FIRST_COMPARATOR);
		}
		
		public void shuffle() {
			Collections.shuffle(CARDS);
		}
	}
	
	
	
	
	
	public static class Card implements Comparable<Card>, Serializable {
		
		private static final long serialVersionUID = 4156033596469413332L;
		Rank RANK;
		Suit SUIT;
		int IMAGE_INDEX;
		private final int COVER_INDEX;
		private final int IMAGE_WIDTH;
		private final int IMAGE_HEIGHT;
		final Motion.MotionObject MOTION_OBJECT;
		private Stack stack;
		private int highlightColor = 0xff4080ff;
		private boolean highlighted = false;
		private boolean faceDown = true;
		
		private Card(Rank r, Suit s, int imageIndex, int coverIndex,
											Motion.MotionObject motionObject) {
			RANK = r;
			SUIT = s;
			IMAGE_INDEX = imageIndex;
			COVER_INDEX = coverIndex;
			IMAGE_WIDTH = CARD_IMAGES.get(IMAGE_INDEX).width;
			IMAGE_HEIGHT = CARD_IMAGES.get(IMAGE_INDEX).height;
			MOTION_OBJECT = motionObject;
		}
		
		public void moveTo(Stack newStack) {
			if (stack != null)
				stack.CARDS.remove(this);
			newStack.CARDS.add(this);
			stack = newStack;
		}
		
		public void setHighlightColor(int color) {
			highlightColor = color;
		}
		
		public void highlight(boolean setting) {
			highlighted = setting;
		}
		
		public boolean isHighlighted() {
			return highlighted;
		}
		
		public void setFaceUp() {
			faceDown = false;
		}
		
		public void setFaceDown() {
			faceDown = true;
		}
		
		public boolean faceDown() {
			return faceDown;
		}
		
		@Override
		public int compareTo(Card other) {
			return RANK.compareTo(other.RANK);
		}
		
		@Override
		public String toString() {
			return "" + RANK.SYMBOL + SUIT.SYMBOL;
		}
		
		
		public void draw(RummyGame game) {
			game.imageMode(RummyGame.CENTER);
			game.pushMatrix();
			game.translate(RummyGame.H * MOTION_OBJECT.x, RummyGame.H * MOTION_OBJECT.y);
			game.scale(MOTION_OBJECT.scale * RummyGame.H / IMAGE_HEIGHT);
			game.rotate(MOTION_OBJECT.angle);
			if (highlighted)
				game.tint(highlightColor);
			else
				game.noTint();
			game.image(faceDown ? COVER_IMAGES.get(COVER_INDEX) : CARD_IMAGES.get(IMAGE_INDEX), 0, 0);
			game.noTint();
			game.popMatrix();
		}
		
		public boolean isCardArea(float x, float y) {
			if (Math.abs(y - MOTION_OBJECT.y) <= MOTION_OBJECT.scale/2) {
				float ratio = (float) IMAGE_WIDTH / IMAGE_HEIGHT;
				float width = (float) MOTION_OBJECT.scale * ratio;
				if (Math.abs(x - MOTION_OBJECT.x) <= width/2)
					return true;
			}
			return false;
		}
	}
} 
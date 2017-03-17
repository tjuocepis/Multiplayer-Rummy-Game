package cs342.rummy.gui.game;

class Player {
	
	static final float
	MAX_NAME_WIDTH = (float) (RummyGame.W * 0.175),
	MAX_NAME_HEIGHT = (float) (RummyGame.H * 0.05);
	
	final String NAME;
	final Cards.Stack HAND;
	float x, y;
	boolean highlighted;
	
	
	Player(String name, float x, float y) {
		NAME = name;
		HAND = new Cards.Stack();
		this.x = x;
		this.y = y;
	}
	
	void spreadHand() {
		float radius = (float) (0.75 * MAX_NAME_WIDTH);
		float centerX = x;
		float centerY = (float) (y + (MAX_NAME_HEIGHT - MAX_NAME_WIDTH) / 2);
		float angleStep = (float) (-0.5 * Math.PI / (HAND.size() + 1));
		float angle = (float) (0.75 * Math.PI + angleStep);
		
		for (int i = 0; i < HAND.size(); i++) {
			HAND.get(i).MOTION_OBJECT.setMotionSteps(30);
			HAND.get(i).MOTION_OBJECT.set((float) (centerX + radius * Math.cos(angle)),
								  (float) (centerY + radius * Math.sin(angle)),
								  (float) (0.05 + 0.05 / (HAND.size() + 1)),
								  (float) (angle + 0.5 * Math.PI));
			angle += angleStep;
		}
	}
	
	void draw(RummyGame game) {
		game.pushMatrix();
		
		game.translate(x, y);
		
		if (highlighted)
			game.fill(96,192,255);
		else
			game.fill(32,64,128);
		game.stroke(64,128,255);
		game.strokeWeight(MAX_NAME_HEIGHT / 20);
		game.rect((float) (-MAX_NAME_WIDTH / 2), (float) (-MAX_NAME_HEIGHT / 2), MAX_NAME_WIDTH, MAX_NAME_HEIGHT);
		
		game.fill(255);
		game.textAlign(RummyGame.CENTER);
		game.drawText(NAME, MAX_NAME_WIDTH, MAX_NAME_HEIGHT);
		
		game.popMatrix();
	}
}

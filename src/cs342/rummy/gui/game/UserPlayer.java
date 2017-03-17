package cs342.rummy.gui.game;

public class UserPlayer extends Player {
	
	static final float
	CARD_AREA_WIDTH = (float) (RummyGame.W * 1.0),
	CARD_AREA_HEIGHT = (float) (RummyGame.H * 0.2);
	
	UserPlayer(String name, float x, float y) {
		super(name, x, y);
		
	}
	
	@Override
	void spreadHand() {
		float radius = (float) (5 * CARD_AREA_WIDTH);
		float centerX = x;
		float centerY = (float) (y + radius);
		float arcRange = (float) Math.asin((CARD_AREA_WIDTH * 1.6) / radius);
		float angleStep = (float) (arcRange / (HAND.size() + 7));
		float angle = (float) (-(0.5 * Math.PI + arcRange / 2) + 4 * angleStep);
		
		for (int i = 0; i < HAND.size(); i++) {
			HAND.get(i).MOTION_OBJECT.setMotionSteps(30);
			HAND.get(i).MOTION_OBJECT.set((float) (centerX + radius * Math.cos(angle)),
								  (float) (centerY + radius * Math.sin(angle)),
								  (float) (0.2),
								  (float) (angle + 0.5 * Math.PI));
			angle += angleStep;
		}
	}
	
	@Override
	void draw(RummyGame game) {
		game.pushMatrix();
		
		game.translate(x, (float) (y + CARD_AREA_HEIGHT / 2));
		if (highlighted)
			game.fill(64,255,128,160);
		else
			game.fill(128,64,32,160);
		game.stroke(255,255,255,160);
		game.strokeWeight(MAX_NAME_HEIGHT / 20);
		game.ellipse(0, 0, RummyGame.W, MAX_NAME_HEIGHT * 2);
		
		game.translate(0, (float) (-MAX_NAME_HEIGHT / 2));
		game.fill(255);
		game.textAlign(RummyGame.CENTER);
		game.drawText(NAME, MAX_NAME_WIDTH, MAX_NAME_HEIGHT);
		
		game.popMatrix();
	}
}

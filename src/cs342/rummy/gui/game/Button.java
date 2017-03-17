package cs342.rummy.gui.game;

import java.util.ArrayList;
import java.util.List;

import processing.core.PImage;

public class Button {
	
	
	
	private static final List<PImage> BUTTON_IMAGES = new ArrayList<PImage>();
	
	public static void setImage(PImage image, int buttonIndex) {
		while (buttonIndex >= BUTTON_IMAGES.size())
			BUTTON_IMAGES.add(null);
		BUTTON_IMAGES.set(buttonIndex, image);
	}
	
	
	
	
	
	private final int IMAGE_INDEX;
	private final int IMAGE_WIDTH;
	private final int IMAGE_HEIGHT;
	final Motion.MotionObject MOTION_OBJECT;
	boolean visible;
	
	Button(int imageIndex, Motion.MotionObject motionObject) {
		IMAGE_INDEX = imageIndex;
		IMAGE_WIDTH = BUTTON_IMAGES.get(IMAGE_INDEX).width;
		IMAGE_HEIGHT = BUTTON_IMAGES.get(IMAGE_INDEX).height;
		MOTION_OBJECT = motionObject;
		visible = false;
	}
	
	public void draw(RummyGame game) {
		game.pushMatrix();
		game.imageMode(RummyGame.CENTER);
		game.translate(RummyGame.H * MOTION_OBJECT.x, RummyGame.H * MOTION_OBJECT.y);
		game.scale(MOTION_OBJECT.scale * RummyGame.H / IMAGE_HEIGHT);
		game.image(BUTTON_IMAGES.get(IMAGE_INDEX), 0, 0);
		game.popMatrix();
	}
	
	public boolean isButtonArea(float x, float y) {
		if (Math.abs(y - MOTION_OBJECT.y) <= MOTION_OBJECT.scale/2) {
			float ratio = (float) IMAGE_WIDTH / IMAGE_HEIGHT;
			float width = (float) MOTION_OBJECT.scale * ratio;
			if (Math.abs(x - MOTION_OBJECT.x) <= width/2)
				return true;
		}
		return false;
	}
}

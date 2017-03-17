package cs342.rummy.gui.game;

import java.util.ArrayList;
import java.util.List;

public class Motion {
	
	
	
	
	
	private final List<MotionObject> MOTION_OBJECTS;  
	
	public Motion() {
		MOTION_OBJECTS = new ArrayList<MotionObject>();
	}
	
	public void step() {
		for (MotionObject thing : MOTION_OBJECTS)
			thing.step();
	}
	
	
	
	
	
	public class MotionObject {
		
		float x, y, scale, angle;
		private float startX, startY, startScale, startAngle;
		private float endX, endY, endScale, endAngle;
		private int totalSteps, currentStep, delaySteps;
		private float stepSpeed;
		
		public MotionObject(float x, float y, float scale, float angle) {
			MOTION_OBJECTS.add(this);
			set(x, y, scale, angle);
		}
		
		public void setX(float x)			{set(x, endY, endScale, endAngle);}
		public void setY(float y) 			{set(endX, y, endScale, endAngle);}		
		public void setScale(float scale)	{set(endX, endY, scale, endAngle);}
		public void setAngle(float angle)	{set(endX, endY, endScale, angle);}
		public void setXY(float x, float y)	{set(x, y, endScale, endAngle   );}
		
		public void set(float x, float y, float scale, float angle) {
			if (totalSteps == 0) {
				startX = endX = this.x = x;
				startY = endY = this.y = y;
				startScale = endScale = this.scale = scale;
				startAngle = endAngle = this.angle = angle;
				currentStep = 0;
				stepSpeed = 0;
				delaySteps = 0;
			}
			else {
				startX = this.x;
				startY = this.y;
				startScale = this.scale;
				startAngle = this.angle;
				endX = x;
				endY = y;
				endScale = scale;
				endAngle = angle;
				if (totalSteps < 0) {
					int stepsX = (int) Math.ceil(Math.abs(startX - endX) / stepSpeed);
					int stepsY = (int) Math.ceil(Math.abs(startY - endY) / stepSpeed);
					int stepsScale = (int) Math.ceil(Math.abs(startScale - endScale) / stepSpeed);
					int stepsAngle = (int) Math.ceil(Math.abs(startAngle - endAngle) / stepSpeed);
					double distance = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
					int stepsXY = (int) Math.ceil(distance / stepSpeed);
					totalSteps = Math.max(stepsXY, Math.max(stepsAngle, Math.max(stepsScale, Math.max(stepsX, stepsY))));
					stepSpeed = 0;
					delaySteps = 0;
				}
			}
		}
		
		public void setMotionSteps(int steps) {
			if (steps > 0) {
				delaySteps -= currentStep;
				if (delaySteps < 0)
					delaySteps = 0;
				currentStep = 0;
				totalSteps = delaySteps + steps;
			}
			else {
				totalSteps = 0;
				set(endX, endY, endScale, endAngle);
			}
		}
		
		public void setMotionSpeed(float speed) {
			if (speed > 0) {
				stepSpeed = speed;
				totalSteps = -1;
				currentStep = 0;
			}
			else {
				totalSteps = 0;
				set(endX, endY, endScale, endAngle);
			}
		}
		
		public void setMotionDelay(int steps) {
			delaySteps = steps;
		}
		
		private void step() {
			if (totalSteps == 0)
				return;
			currentStep++;
			if (currentStep == totalSteps) {
				totalSteps = 0;
				currentStep = 0;
				delaySteps = 0;
				set(endX, endY, endScale, endAngle % (float) (Math.PI * 2));
				return;
			}
			float progress = 0;
			if (currentStep > delaySteps)
				progress = (currentStep - delaySteps) / (float) (totalSteps - delaySteps);
			x = startX + progress * (endX - startX);
			y = startY + progress * (endY - startY);
			scale = startScale + progress * (endScale - startScale);
			angle = startAngle + progress * (endAngle - startAngle);
		}
	}
}

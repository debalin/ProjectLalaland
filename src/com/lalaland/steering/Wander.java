package com.lalaland.steering;

import com.lalaland.engine.Engine;
import com.lalaland.object.Kinematic;

import processing.core.PApplet;
import processing.core.PVector;

public class Wander {

	private static Kinematic target;
	private static float time;
	
	static {
		time = -2001;
		target = new Kinematic();
	}
	
	public static SteeringOutput getPositionMatchingSteering(Kinematic character, float maxLinearAcc, float maxAngularAcc, float timeToTarget, float ros) {
		float mainTime = Engine.getTime();
		SteeringOutput steering;
		
		if((mainTime-time) > Math.random()*2000) {
			time += 2000;
			target.position.x = (float) (Math.random() * Engine.getResolution().x);
			target.position.y = (float) (Math.random() * Engine.getResolution().y);
		}
		
		steering = Face.getSteering(character, target, timeToTarget, ros);
		steering.linear = asVector(character.orientation);
		steering.linear.setMag(maxLinearAcc);
		if(steering.angular > maxAngularAcc)
			steering.angular = maxAngularAcc;

		return steering;
	}
	
//	public SteeringOutput getOrientationMatchingSteering() {
//		
//	}

	private static PVector asVector(float orientation) {
		PVector v = new PVector();
		v.y = PApplet.sin(orientation);
		v.x = PApplet.cos(orientation);
		return v;
	}
	
	private static float randomBinomial() {
		return (float) (Math.random() - Math.random());
	}
}

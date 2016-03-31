package com.lalaland.steering;

import com.lalaland.object.Kinematic;

import processing.core.PConstants;

public class Align {
	
	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float timeToTarget) {
		SteeringOutput steering = new SteeringOutput();
		
		steering.angular = target.orientation - character.orientation;
		steering.angular = mapToRange(steering.angular) / timeToTarget;
		
		return steering;
	}
	
	private static float mapToRange(float rotation) {
		rotation = rotation % (2*PConstants.PI);
		if(Math.abs(rotation) <= PConstants.PI)
			return rotation;
		if(rotation > PConstants.PI)
			return rotation - PConstants.PI*2;
		return rotation + PConstants.PI*2;
	}
}

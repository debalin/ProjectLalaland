package com.lalaland.steering;

import com.lalaland.object.Kinematic;

import processing.core.PVector;

public class Seek {
	
	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float max_acceleration, float ros) {
		SteeringOutput steering = new SteeringOutput();
		steering.linear = PVector.sub(target.position, character.position);
		
		if(steering.linear.mag() <= ros)
			return new SteeringOutput();
		
		if(steering.linear.mag() > max_acceleration)
			steering.linear.setMag(max_acceleration);
		
		return steering;
	}
}

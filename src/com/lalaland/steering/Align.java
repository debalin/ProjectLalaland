package com.lalaland.steering;

import com.lalaland.object.GameObject;
import com.lalaland.object.Kinematic;

public class Align {
	
	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float timeToTarget) {
		SteeringOutput steering = new SteeringOutput();
		
		steering.angular = target.orientation - character.orientation;
		steering.angular = GameObject.mapToRange(steering.angular) / timeToTarget;
		
		return steering;
	}

}

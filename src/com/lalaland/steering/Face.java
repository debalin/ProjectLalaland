package com.lalaland.steering;

import com.lalaland.object.Kinematic;

import processing.core.PVector;

public class Face extends Align {

	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float timeToTarget, float ros) {
		PVector direction = PVector.sub(target.position, character.position);
		if (direction.mag() < ros)
			return new SteeringOutput();
		target.orientation = direction.heading();
		return Align.getSteering(character, target, timeToTarget);
	}
}

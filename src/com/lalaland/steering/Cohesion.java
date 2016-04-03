package com.lalaland.steering;

import java.util.ArrayList;

import com.lalaland.object.Kinematic;

import processing.core.PVector;

public class Cohesion {

	public static SteeringOutput getSteering(Kinematic character, ArrayList<Kinematic> targets, float maxAcceleration) {
		SteeringOutput steering = new SteeringOutput();
		PVector v = new PVector();
		int neighbourCount = 0;
		for(int i=0; i<targets.size(); i++) {
			Kinematic target = targets.get(i);
			if(character == target)
				continue;
			v.add(target.position);
			neighbourCount++;
		}
		if(neighbourCount == 0)
			return steering;
		v.div(neighbourCount);
		v.sub(character.position);
		v.normalize();
		v.mult(maxAcceleration);
		steering.linear = v;
		return steering;
	}
}

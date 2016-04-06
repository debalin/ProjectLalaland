package com.lalaland.steering;

import java.util.List;
import com.lalaland.object.Kinematic;
import processing.core.PVector;

public class Separation {

	public static SteeringOutput getSteering(Kinematic character, List<Kinematic> targets, float maxAcceleration, float threshold) {
		SteeringOutput steering = new SteeringOutput();
		PVector v = new PVector();
		int neighbourCount = 0;
		for(Kinematic target : targets) {
			if(character == target || target.velocity.mag() == 0)
				continue;
			PVector direction = PVector.sub(target.position, character.position);
			if(direction.mag() < threshold) {
				v.add(direction);
				neighbourCount++;
			}
		}
		if(neighbourCount == 0)
			return steering;
		v.div(neighbourCount);
		v.normalize();
		v.mult(-1);
		v.mult(maxAcceleration);
		steering.linear = v;
		return steering;
	}

}

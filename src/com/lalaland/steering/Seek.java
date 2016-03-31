package com.lalaland.steering;

import com.lalaland.object.Kinematic;
import processing.core.PVector;

public class Seek {
	
	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float maxAcceleration, float ros) {
		SteeringOutput steering = new SteeringOutput();
		steering.linear = PVector.sub(target.position, character.position);
		
		if (steering.linear.mag() <= ros)
			return new SteeringOutput();

		steering.linear.setMag(maxAcceleration);
		
		return steering;
	}

	public static KinematicOutput getKinematic(Kinematic character, Kinematic target, float maxVelocity) {
		KinematicOutput kinematic = new KinematicOutput();
		kinematic.velocity = PVector.sub(target.position, character.position);

		if(kinematic.velocity.mag() > maxVelocity)
			kinematic.velocity.setMag(maxVelocity);

		return kinematic;
	}

}

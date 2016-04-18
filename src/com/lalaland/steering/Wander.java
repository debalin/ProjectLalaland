package com.lalaland.steering;

import com.lalaland.engine.Engine;
import com.lalaland.environment.Environment;
import com.lalaland.object.Kinematic;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Wander {

	private static Kinematic target;
	private static float time;

	private int randomiserCounter;
	private int randomiserLimit;
	private float targetOrientation;
	
	static {
		time = -2001;
		target = new Kinematic();
	}

	public Wander(int randomiserLimit) {
		randomiserCounter = 0;
		this.randomiserLimit = randomiserLimit;
		targetOrientation = 0f;
	}
	
	public static SteeringOutput getPositionMatchingSteering(Kinematic character, float maxLinearAcc, float maxAngularAcc, float timeToTarget, float ros) {
		float mainTime = Engine.getTime();
		SteeringOutput steering;
		
		if((mainTime - time) > Math.random() * 2000) {
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
	
	public KinematicOutput getOrientationMatchingSteering(Kinematic character, Environment environment, PApplet parent, int BORDER_PADDING, float MAX_VELOCITY) {
		KinematicOutput kinematicOutput = new KinematicOutput();
		randomiserCounter++;
		if(randomiserCounter == randomiserLimit){
			targetOrientation = randomBinomial() * PConstants.PI + character.orientation;
			randomiserCounter = 0;
		}

		boolean onObstacle = ObstacleSteering.checkForObstacleAvoidance(character, environment);
		if(onObstacle) {
			targetOrientation = ObstacleSteering.avoidObstacleOnWander(character, parent, environment);
		}
		else if(BoundarySteering.checkForBoundaryAvoidance(character, parent, BORDER_PADDING)){
			targetOrientation = BoundarySteering.avoidBoundaryOnWander(character, parent, BORDER_PADDING);
		}
		kinematicOutput.rotation = rotateShapeDirection(character, targetOrientation);
		kinematicOutput.velocity = calculateVelocityPerOrientation(character, MAX_VELOCITY);
		return kinematicOutput;
	}

	protected float rotateShapeDirection(Kinematic character, float angle) {
		angle = (angle - character.orientation) / 30;
		return angle;
	}

	protected PVector calculateVelocityPerOrientation(Kinematic character, float MAX_VELOCITY) {
		PVector velocity = PVector.fromAngle(character.orientation);
		velocity.setMag(MAX_VELOCITY);
		return velocity;
	}

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

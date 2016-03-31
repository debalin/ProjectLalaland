package com.lalaland.object;

import processing.core.PVector;

public class Kinematic {
	public PVector position, velocity;
	public float orientation, rotation;
	
	public Kinematic() {
		position = new PVector();
		velocity = new PVector();
		orientation = 0f; 
		rotation = 0f;
	}
	
	public Kinematic(Kinematic kinematic) {
		position = new PVector(kinematic.position.x, kinematic.position.y);
		velocity = new PVector(kinematic.velocity.x, kinematic.velocity.y);
		orientation = kinematic.orientation; 
		rotation = kinematic.rotation;
	}
	
	public Kinematic(PVector position, PVector velocity, float orientation, float rotation) {
		this.position = position;
		this.velocity = velocity;
		this.orientation = orientation;
		this.rotation = rotation;
	}
}

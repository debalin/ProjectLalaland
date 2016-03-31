package com.lalaland.steering;

import processing.core.PVector;

public class KinematicOutput {

	public PVector velocity;
	public float rotation;

	public KinematicOutput() {
		velocity = new PVector();
		rotation = 0f;
	}

}

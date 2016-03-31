package com.lalaland.steering;

import processing.core.PVector;

public class SteeringOutput {
	public PVector linear;
	public float angular;
	
	public SteeringOutput() {
		linear = new PVector();
		angular = 0f;
	}
}

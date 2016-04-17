package com.lalaland.steering;

import com.lalaland.environment.Environment;
import com.lalaland.object.Kinematic;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.Random;

public class ObstacleSteering {

  public static boolean checkForObstacleAvoidance(Kinematic character, Environment environment){
    PVector future_ray1 = PVector.add(character.position, PVector.mult(character.velocity, 15f));
    PVector future_ray2 = PVector.add(character.position, PVector.mult(character.velocity, 30f));
    PVector future_ray3 = PVector.add(character.position, PVector.mult(character.velocity, 3.5f));
    PVector future_ray4 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(character.velocity.mag() * 10f));
    PVector future_ray5 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(character.velocity.mag() * 10f));
    return (
      environment.onObstacle(future_ray1) ||
      environment.onObstacle(future_ray2) ||
      environment.onObstacle(future_ray3) ||
      environment.onObstacle(future_ray4) ||
      environment.onObstacle(future_ray5)
    );
  }

  public static float avoidObstacleOnWander(Kinematic character, PApplet parent, Environment environment) {
    float orient;
    Random random = new Random();
    float targetOrientation;
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = parent.radians(orient) + character.orientation;
    } while (checkForObstacleAvoidance(new Kinematic(character.position, PVector.fromAngle(targetOrientation).setMag(20), 0, 0), environment));
    return targetOrientation;
  }

}

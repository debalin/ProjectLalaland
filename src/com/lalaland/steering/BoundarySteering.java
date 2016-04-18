package com.lalaland.steering;

import com.lalaland.object.Kinematic;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Random;

public class BoundarySteering {

  public static boolean checkForBoundaryAvoidance(Kinematic character, PApplet parent, int BORDER_PADDING){
    PVector future_ray =  PVector.add(character.position, character.velocity);
    return (
      future_ray.x <= BORDER_PADDING ||
      future_ray.x >= parent.width - BORDER_PADDING ||
      future_ray.y <= BORDER_PADDING ||
      future_ray.y >= parent.height - BORDER_PADDING
    );
  }

  public static float avoidBoundaryOnWander(Kinematic character, PApplet parent, int BORDER_PADDING){
    float orient, targetOrientation;
    Random random = new Random();
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = parent.radians(orient) +  character.orientation;
    } while(checkForBoundaryAvoidance(new Kinematic(character.position, PVector.fromAngle(targetOrientation).setMag(100), 0, 0), parent, BORDER_PADDING));
    return targetOrientation;
  }

}

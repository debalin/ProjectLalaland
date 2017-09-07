# Game AI Project
### Our rendition of Crimsonland for CSC 584 - Building Game AI
#### North Carolina State University


### Authors
+ Debalin Das
+ Kunal Kapoor
+ Utkarsh Verma

### Resources
+ [Demo Video](https://youtu.be/PioWmf5c-ow)
+ [Presentation Slides](https://drive.google.com/open?id=0B13zgKKtKt2EZzh0MEcyTWNJQXM)
+ [Paper Submission](https://drive.google.com/open?id=0B13zgKKtKt2ESFNDaFdjVmlTa28)

### Description
Crimsonland is a 2D (top-down view) survival game where the player is trying to stay alive while various enemies converge on him from all sides of the map. The player is placed in the middle of the map at the beginning of the game and the enemies enter from around the boundary of the map. The player has to shoot the enemies to kill them and stop them from reaching him. Killing enemies also earns the player points, and the objective is to try and maximize the points earned until death.

Our objective is to create a game like Crimsonland where we want to try out, invent and implement novel enemy behaviors to make the survival game interesting. The variety of enemies will range from simple wanderers without a goal in mind to beings with swarm intelligence that try to use their numbers to corner and kill the player. Additionally, we plan to add a respawnable helper bot for the player, which will be used for assistance in dropped item collection. The goal of the project is to create various interesting types of enemy bots demonstrating different AI techniques to defeat the player, and balance the equation by creating a helper bot to assist the player.


## Tasks and Techniques
This is a survival game with waves of enemies converging on the player, trying to kill him. This gives us a platform to create many different enemy bots demonstrating a combination of AI techniques. Along with that, our assistant bot will showcase its own independent path-finding techniques to ferry the ammo drops back to the player. Apart from these, we will briefly touch upon the map generation and the player himself which will be controlled by the user.
    
###Map Generation
For simplicity, our map contains a constant terrain and obstacles can be generated randomly or from a fixed script. The AI bots in the game as well as the player need to avoid these obstacles while moving around the map. These obstacles have been put to some interesting use. An enemy AI will no doubt be fired upon constantly by the player and more often than not, the enemy will die before reaching the player. But we have built a certain enemy type in such a way, that when they have very low health, they will run for cover behind obstacles to avoid incoming fire. This has added a different kind of realism to the game.  

The player will spawn at the center of the map. The enemies will spawn both outside and inside the visible area of the map and will come towards the player. 

###Player
The player is represented as a distinct shape/sprite on the map, and is under direct control of the person playing the game. The player can move around the map in 8 directions - UP, DOWN, LEFT, RIGHT, UP-LEFT, UP-RIGHT, DOWN-LEFT and DOWN-RIGHT (using the W, S, A, D keys on a keyboard). The player will be responsible for moving smartly across the terrain, avoiding obstacles and using them as cover when necessary. The player has to avoid coming in contact with the enemy bots, which can lead to health damage. The fire direction is always towards the current position of mouse pointer.

###Enemy Bots
There will be several kinds of enemy bots in the game, which will differ in appearance, and behaviour. However, most enemy bots have one common goal, which is to kill the player by coming in contact with him. By providing a variation of enemies, we plan to test them together in a single session of the game as well as individually. Furthermore, we will test different methods of implementing the same enemy behavior and see which looks best. We will detail this in the later part of this document, when we talk about Implementation Details and Evaluation Methods. The enemy types are listed below:

####1. Soldier
These are the most commonly spawned enemy units. They directly seek the player, and try to kill him by contact. They move at a constant speed throughout the map. They are unaware of other enemy bots, and thus each such bot moves independently. These bots have normal life reduction rate (LRR). They are randomly spawned in any corner of the map. While moving, it tries to avoid any obstacles that might come in its way. 
        
The more interesting behaviour of Soldier bot happens when it starts losing health on taking damage from player's gun. When the health goes lower than a certain threshold, it will try and save itself by finding an appropriate wall/obstacle and take cover behind that. Once it has stopped behind that wall, it will start regaining health. After it has reached a certain threshold in the course of regaining health, it will start seeking to attack the player again. While recuperating, if the player comes to that cover point and shoots at it again, it will then find a "different" obstacle than the one it was hiding at right now and take cover behind that. It will continue doing this until it has regained its health till the higher threshold and then it will start seeking the player just like before. 

####2. Grunt
The Grunt aimlessly wanders around the map, serving as another bot for the player to avoid and kill. Grunts always move at a constant speed, and spawn less frequently than Soldiers. Grunt also has a lower LRR, so they are medium-level in difficulty to kill. The idea behind this bot is that in a game with all "intelligent" AI bots, we need a few that are essentially simple and, to an extent, random in order to provide a better gameplay experience. They are like a moving obstacle which the player should avoid coming in contact with. Grunt also steers clear of walls and obstacles in an elegant manner and avoids wandering too close to the walls. 
        
An interesting alternative behavior of the Grunt which we test is a slightly 'smarter' version of Grunt. In this mode, the Grunt keeps track of where the player is on the map, and finds an 'average' position of the player - essentially the location on map around which player has been spending a lot of time. Grunt tries to wander slowly towards this direction, while still not seeking the player directly, akin to 'artificial stupidity'. This leads to an interesting behavior wherein the Grunt moves towards the area in map where player has been staying for a long time, while still wandering randomly. Our hypothesis is that this will lead to Grunt being a larger menace to the player, and there will be larger probability of it coming in contact with the player. This gives the player incentive to kill Grunts as soon as they spawn on the map before it changes to this 'smart' behaviour.

####3. Hermit
These bots are move powerful than the soldier bots, as they can move at a high speed and even have lower LRR, making them harder to kill. Hermits do not actively seek the player but they wander around the map in pseudo-random fashion. They can only see as far as a certain radius allows, and are harmless when the player is not within this visible range. However, when the player comes within this range, these bots get immediately alerted. Upon activation they enlarge and turn yellow - we call it 'rage' mode, and then they quickly steer towards the player at a high speed. If the player moves out of the view radius during the pursuit, they return back to their normal appearance and wander behavior. 
        
While testing the game during development, we observed that it's quite easy to kill Hermits when it is in rage-mode and seeking the player at a high speed. This is contrary to the behavior we actually wanted to achieve. The reason for this is that since the bot is directly moving towards the player, the player can simply keep running backwards, while continuously shooting at the incoming bot. The bot takes in damage from all the bullets and dies rather quickly. To make this more challenging for the player, we proposed a different movement pattern for Hermit bots. When seeking the player in rage-mode, they move in a zig-zag pattern towards the player. This makes it more difficult to shoot at, making the game more fun and challenging. Another proposal is that the bot will spiral down towards the player, moving in a circular motion and slowly converging on it. This might prove even more challenging to kill, as we plan to find out in our evaluation stage.

####4. Flocker
These are (collectively) intelligent enemy bots which spawn and move around in groups. Each such group has a leader, where all other members follow the group leader. The leader alone can smell the player's position and moves towards him, while the group members keep following the leader. The leader has a pre-defined field of vision which is blocked by obstacles, and once the player is visible, it alerts the group followers, who then abandon their current flock formation to attack the player. If the player moves out of the vision of the leader, the followers reform their flock around the leader and continue to follow him as before. If the leader dies before its flock, then the followers abandon each other and focus on killing the player.

####5. Martyr
Martyrs are bots that follow their leader with the sole purpose of keeping the leader alive. They try their best to sacrifice themselves in order to keep the leader out of harms way. The interesting part about Martyrs are that they always travel in strategic formations. The leader of the martyr group is always at the center of the formation, surrounding which there are the individual martyrs. The leader pursues the player and the martyrs take their position around the leader. The formation always starts with the leader in the center and 8 martyrs forming a box around the leader. 
        
There is a reason why we have chosen to call them martyrs though. This is because as the leader seeks the player, the front row of martyrs will get shot and probably get killed as a result. Whenever this happens, a martyr from the back row of the formation will come forward and take its position, thus dynamically changing the formation. Another way that this has been implemented is to dynamically change the complete formation type to adjust the loss in martyrs. These two cases will actually show different behaviors and we have planned to evaluate and see which one fares better in protecting the leader in the middle. 

####6. Blender
These bots start off with very low HP when they spawn, but they have an interesting feature. Every blender will look at a certain radius around itself and tries to spot another blender within that area. If it finds any, then both (or more) of these blenders will come together and unite to form a bigger blender having more HP and speed. This blender will be more difficult to kill than the smaller blenders. So its imperative for the player to kill any blender whenever they spawn, so that they aren't given much time to coalesce and form larger and stronger blenders. This might give an interesting choice to the player to think and decide which enemy to kill first; allowing a blender to roam around for long may be more dangerous to the player than anything else. Furthermore, the goal of a blender could be two-fold. Knowing that to kill the player the blender must be stronger, it could have a dynamic goal matching behavior to initially find other blenders and morph into a stronger being before going after the player.

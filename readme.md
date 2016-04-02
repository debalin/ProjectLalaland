# Game AI Project
### Our rendition of Crimsonland for CSC 584 - Building Game AI
#### North Carolina State University


####Authors
+	Debalin Das
+ Kunal Kapoor
+ Utkarsh Verma


Crimsonland is a 2D (top-down view) survival game where the player is trying to stay alive while various enemies converge on him from all sides of the map. The player is placed in the middle of the map at the beginning of the game and the enemies enter from around the boundary of the map. The player has to shoot the enemies to kill them and stop them from reaching him. Killing enemies also earns the player points, and the objective is to try and maximize the points earned until death.

Our objective is to create a game like Crimsonland where we want to try out, invent and implement novel enemy behaviors to make the survival game interesting. The variety of enemies will range from simple wanderers without a goal in mind to beings with swarm intelligence that try to use their numbers to corner and kill the player. Additionally, we plan to add a respawnable helper bot for the player, which will be used for assistance in dropped item collection. The goal of the project is to create various interesting types of enemy bots demonstrating different AI techniques to defeat the player, and balance the equation by creating a helper bot to assist the player.


## Tasks and Techniques
This is a survival game with waves of enemies converging on the player, trying to kill him. This gives us a platform to create many different enemy bots demonstrating a combination of AI techniques. Along with that, our assistant bot will showcase its own independent path-finding techniques to ferry the ammo drops back to the player. Apart from these, we will briefly touch upon the map generation and the player himself which will be controlled by the user.
    
###Map Generation
For simplicity, our map will only contain a constant terrain and randomly generated patches of obstacles. The AI bots in the game as well as the player needs to avoid the obstacles while moving around the map. These obstacles can be put to some interesting use. An enemy AI will no doubt be fired upon constantly by the player and more often than not, the enemy will die before reaching the player. But we can program the bots in such a way, that when they have very low health, they will run for cover behind obstacles to avoid incoming fire. This will add a different kind of realism to the game. We plan to touch on some of these subtle things when we implement the game. 
The player will spawn at the center of the map and the assistant will spawn near him. The enemies will spawn outside the visible area of the map and will come towards the player. In the process, they will become visible. 

###Player
The player is represented as a distinct shape/sprite on the map, and is under direct control of the person playing the game. The player can move around the map in all directions. Player will be responsible for moving strategically across the terrain, avoiding obstacles and using them as cover when necessary. The player has to avoid coming in contact with the enemy bots, which can lead to death. The player will also be able to shoot at enemies while moving, and will receive help with collecting ammunition from the assistant bot.

###Enemy Bots
There will be several kinds of enemy bots in the game, which will differ in appearance, and behaviour. However, most enemy bots have one common goal, which is to kill the player by coming in contact with him (there will be a few simpler bots who just turn up during gameplay to wander the game environment and eventually dying). By providing a variation of enemies, we plan to test them together in a single session of the game as well as individually (we will talk about this in more detail later). We are listing a few of the many different enemy types that we have thought of below.


####1. Soldier
These are the basic and most common enemy units, which follow a simple rule. They directly seek the player, and try to kill him by contact. They move at a constant speed throughout the map, and are simple in their AI complexity. They are unaware of other enemy bots, and thus each such bot moves independently. These bots have normal hit-points (HP). After a soldier is generated outside the visible area of the map, it starts doing a position matching seek with the player. In the process, it tries to avoid any obstacles that might come in its way. We could figure out a pre-defined path avoiding obstacles for all of these bots, but we plan to keep the path-finding technique only for the assistant bot. In that way, we can reflect and analyze the differences between the two. So, instead of using path-finding, we will make a soldier start moving in the direction of the player; if it notices an obstacle within a certain radius of its location, it will avoid it and then move forward. It does not have any knowledge of other obstacles lying on its path unless it encounters one.

####2. Grunt
The grunt would aimlessly wander the map searching for a cause unknown to mankind just so that it could serve as another obstacle for the player to murder. The idea behind this is that in a game with all "intelligent" AI bots, we need a few that are essentially simple and, to an extent, random in order to provide a better gameplay experience. 


####3. Hermit
These bots are move powerful than the soldier bots, as they can move much faster and have more HP, making them harder to kill. These bots do not actively seek the player but they wander around the map in pseudo-random fashion. They can only see as far as a certain radius allows, and are harmless when the player is not within their visible range. However, when the player comes within this range, these bots get immediately alerted. Upon activation, they immediately steer towards the player and move towards him for a kill. This will be implemented using a seek-steering behaviour, wherein the bot accelerates to max speed while moving towards the player. Otherwise, it will display a orientation based wandering motion.  

####4. Flocker
These are (collectively) intelligent enemy bots which spawn and move around in groups. Each such group has a leader, where all other members follow the group leader. The leader alone seeks out the player and moves towards him, while the group members keep following the leader. The leader has a pre-defined alert range, and once the player is within that range, it alerts the group followers, who then spread out and try to surround the player independently. Moreover, we plan on playing with the flocking pattern (i.e. the structure formed by the followers around the leader) and see which one looks the most interesting.

####5. Martyr
Martyrs are small weak bots that follow their leader with the sole purpose of keeping the leader alive. They try their best to sacrifice themselves in order to keep the leader out of harms way. Once the leader is dead, however, these numerous creatures go berserk and chase after the player extremely fast for revenge. This is an interesting aspect in game enemy design which actually gives a human touch to the game. The player, for example, may understand this behavior and decide to shoot all the young-lings before targeting the leader.

####6. Blender
These bots start off with very low HP when they spawn, but they have an interesting feature. Every blender will look at a certain radius around itself and tries to spot another blender within that area. If it finds any, then both (or more) of these blenders will come together and unite to form a bigger blender having more HP and speed. This blender will be more difficult to kill than the smaller blenders. So its imperative for the player to kill any blender whenever they spawn, so that they aren't given much time to coalesce and form larger and stronger blenders. This might give an interesting choice to the player to think and decide which enemy to kill first; allowing a blender to roam around for long may be more dangerous to the player than anything else. Furthermore, the goal of a blender could be two-fold. Knowing that to kill the player the blender must be stronger, it could have a dynamic goal matching behavior to initially find other blenders and morph into a stronger being before going after the player.

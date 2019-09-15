# will-bot

A bot that plays a text-only version of Fire Emblem (also works with friends). 

## Commands

w!register <username> [<class>] - Registers a user.  
w!classes - Shows a list of classes.  
w!select <user> - Auto-fills <user>.  
w!stats <user> - Displays stats.  
w!reroll <user> - Rerolls stats.  
w!weaponranks <user> - Displays weapon ranks.  
w!users - Displays a list of users.  
w!weapons - Shows a list of weapons.  
w!equip <user> <weapon> - Equips a weapon.  
w!unequip <user> - Unequips a weapon.  
w!skills - Shows a list of skills.  
w!assign <user> <skill> - Assigns a skill.  
w!remove <user> - Removes a skill.  
w!attackhelp - Shows help regarding attacks.  
w!taghelp - Shows help regarding tags.  
w!roll [<dice>] - Rolls dice.  
w!rollhelp - Shows help regarding dice rolling.  
w!ping - Pong!  

## Changelog

v1.7: The Class Update
- **Classes** have been added.
    - Classes can be chosen during registration by typing **w!register <username> <class>**.
    - Use **w!classes** for a list of currently implemented classes.
    - Classes affect **base stats**, **growths**, and **abilities to use weapons**.
    - Stratum enemies will now also be categorized into classes.
    - However, as a result, **all character data has been cleared**. Sorry.
- The **Anima weapon triangle** has been added to combat.
- Maps are now **15x15**, although larger maps are possible and will be randomly generated.
- Hit chances no longer stack each time an attack is calculated.
- An opponent with Fortune equipped no longer sets their own crit chance to 0.
- Tags with no {args} fields no longer break when given extra arguments.

v1.6: The Tag Update
- **Tags** have been added.
    - Tags can be used to incur a specific reaction from the bot.
    - Use **w!taghelp** for help on using tags.
    - **{args}**, **{args;n}** and **{user}** fields have been added.
- **Adept** no longer activates on itself.
- Fixed some naming issues in the combat log.

v1.5: The Map Update
- **Maps** have been added to boss battles.
    - During a boss battle, a 2-D map will display showing positions of allies and enemies.
    - Use **w!move <user> <location>** to move characters on the map. Locations are typed as x,y.
    - Enemies must be in range before you can attack them. The same goes for enemies.
    - All units have **2 Mov** and cannot move through walls.
    - Use **w!endturn <user>** to prematurely end a turn. Attacking or healing will automatically end the turn.
- The attack command has been reduced to **w!attack**. Physical/magic is now based on your weapon.
- The **Physic** staff has been added.

v1.4: The Boss Update
  - **Boss battles** have been added.
    - Use **w!boss <bossname> <players...>** to join a boss battle.
    - **Cyrus** [Lv. 15] is the only battle for now, but more will be added soon.
    - Use **w!bosses** to see a list of bosses, and **w!rooms** to see a list of rooms.
    - Winning a boss battle will grant special weapons.
  - Only stratum enemies will now take an automatic turn after an attack.
  - Skills **Gamble**, **Luna** and **Sol** have been added.
  
v1.3: The Combat Update
  - An **enemy turn** has been added.
    - The enemy will **always retaliate** if neither units are dead after initial combat.
  - The combat system has been reworked (no visible effects).
    - Everything should be a lot smoother going forward.
  - Stratum enemies are now **limited to lv 1000**.
    - Generating higher-level enemies could hang the bot.
- You are now **forced to re-calculate** if either your character or the enemy changes between calculation and conformation.
  - Player comparisons now work properly.
  - Stats for stratum enemies now display correctly.
  - Healing now correctly awards weapon XP.
  - Characters are now able to attack without confirming their previous action.
  - Skills that are permanently on no longer randomly display activation in combat.
  
v1.2: The Weapon Ranks Update
  - Many more weapons (**Bronze**, **Steel**, **Brave** variants) have been added.
    - The shop has been updated to contain more information.
  - **Weapon ranks** have been added. Weapons now require a certain rank to wield.
    - Use **w!weaponranks** to show your weapon ranks.
  - **Default character selection** has been added.
    - Use **w!select <user>** to auto-fill all future <user> tags.
  - The **Blossom** and **Discipline** skills have been added.
  
v1.1: The Hit Chance Update
  - **Hit Rates** have been added.
    - Have fun missing 99% hits and getting critted with a 1% chance.
  - Many skills have been added.\n"
    - These include **Aptitude**, **Fortune**, **Paragon** and **Pavise**
  - **Tomes** (**Fire**, **Wind**, **Thunder**) have been added.
  - **Training stratums** have been introuced.
    - Use these by replacing the enemy's name in an attack with **\"stratum+<level>\"**.
  - The heal for infinite HP bug has been quashed.

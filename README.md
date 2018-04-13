# will-bot

Changelog:

v1.14: The Boss Update
  - **Boss battles** have been added.
    - Use **w!boss <bossname> <players...>** to join a boss battle.
    - **Cyrus** [Lv. 15] is the only battle for now, but more will be added soon.
    - Use **w!bosses** to see a list of bosses, and **w!rooms** to see a list of rooms.
    - Winning a boss battle will grant special weapons.
  - Only stratum enemies will now take an automatic turn after an attack.
  - Skills **Gamble**, **Luna** and **Sol** have been added.
  
v1.13: The Combat Update
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
  
v1.12: The Weapon Ranks Update
  - Many more weapons (**Bronze**, **Steel**, **Brave** variants) have been added.
    - The shop has been updated to contain more information.
  - **Weapon ranks** have been added. Weapons now require a certain rank to wield.
    - Use **w!weaponranks** to show your weapon ranks.
  - **Default character selection** has been added.
    - Use **w!select <user>** to auto-fill all future <user> tags.
  - The **Blossom** and **Discipline** skills have been added.
  
v1.11: The Hit Chance Update
  - **Hit Rates** have been added.
    - Have fun missing 99% hits and getting critted with a 1% chance.
  - Many skills have been added.\n"
    - These include **Aptitude**, **Fortune**, **Paragon** and **Pavise**
  - **Tomes** (**Fire**, **Wind**, **Thunder**) have been added.
  - **Training stratums** have been introuced.
    - Use these by replacing the enemy's name in an attack with **\"stratum+<level>\"**.
  - The heal for infinite HP bug has been quashed.

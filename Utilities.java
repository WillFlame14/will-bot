package bot.willbot;

import java.util.*;

public class Utilities{
    
    public static String bold(String s) {
        return "**" + s + "**";
    }
    
    public static String highlight (String s) {
        return "***" + s + "***";
    }
    
    public static void checkPlayer(String name, Long id)throws ValidationException {
        if (!Bot.playermap.containsKey(name)) {       //user is not in database
            throw new ValidationException("You did not specify a valid user.");
        }
        if (!Bot.idmap.get(name).equals(id)) {
            throw new ValidationException("You do not have authority over this character.");
        }
    }
    
    public static Player generateStratumOpponent(int level) {
        if(level > 1000) {
            level = 1000;
        }
        PClass pclass = PClass.roll();
        Player opponent = new Player(pclass.name(), new Stats(pclass), new Growths(pclass), Weapon.NA, new WeaponRanks(), Skill.NA, pclass, -1);
        long identifier = (int)(Math.random() * 10000) + 1;
        opponent.username += "" + identifier;      //add identifier
        opponent.authorid *= identifier;
        Weapon[] weapons = Weapon.values();
        ArrayList<Weapon> weaponPool = new ArrayList<>();
        ArrayList<Skill> skillPool = new ArrayList<>();
        level += (int)(Math.random() * 5) - 2;
        for(int i = 0; i < level; i++) {
            opponent.levelup();
        }
        opponent.stats.chp = opponent.stats.thp;        //fix HP values from leveling up
        Player.clearLevelUp();
        for(int i = 0; i < weapons.length; i++) {
            if(opponent.pclass.usableWeapons.contains(weapons[i].weaponType)) {
                if((level / 10) >= weapons[i].rank && (int)(level / 10) - weapons[i].rank < 3) {  //needs to be at level, but not 3+ lv
                    weaponPool.add(weapons[i]);
                }
            }
        }
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        if(level > 10) {
            skillPool.add(Skill.Crit15);
        }
        if(level > 20) {
            skillPool.add(Skill.NA);
            skillPool.add(Skill.Vantage);
        }
        opponent.stats.lvl = level;
        opponent.weapon = weaponPool.get((int)(Math.random() * weaponPool.size()));
        opponent.skill = skillPool.get((int)(Math.random() * skillPool.size()));
        Bot.playermap.put(opponent.username, opponent);
        Bot.idmap.put(opponent.username, identifier);
        return opponent;
    }
    
    public static int xpGained(Player winner, Player loser) {
        int[] values = {10, 60, 120, 160, 200, 200, 200, 200, 260, 320, 380, 440, 500, 560, 600};
        
        if(5 - winner.stats.lvl + loser.stats.lvl < 0) {
            return 10;
        }
        if(5 - winner.stats.lvl + loser.stats.lvl > 14) {
            return 600;
        }
        return values[5 - winner.stats.lvl + loser.stats.lvl];
    }
    
    public static long getXpLevelUp(int level) {
        int[] values = {100, 110, 121, 133, 146, 161, 177, 194, 214, 235, 259, 285, 313, 345, 379, 417, 459, 505,
            555, 611, 672, 740, 814, 895, 984, 1083, 1191, 1310, 1442, 1586, 1744, 1919, 2111, 2322, 2554, 2810, 3091, 3400, 3740};
        if(level > 40) {
            return Long.MAX_VALUE;
        }
        return values[level - 1];
    }
}

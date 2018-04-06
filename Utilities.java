package bot.willbot;

import java.util.*;

public class Utilities{
    static ArrayList<Player> generics = new ArrayList<>();
    
    public static void init() {
                                                    //chp, thp, str, mag, spd, def, res, skl, lck, lvl
        generics.add(new Player("Fighter", new Stats(18, 18, 11, 5, 5, 6, 3, 4, 6, 1, 0), new Growths(), Weapon.NA, Skill.NA, -1));
        for(Player p: generics) {
            p.growths.hp = 85;
        }
    }
    
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
        Player opponent = generics.get((int)(Math.random() * generics.size()));
        ArrayList<Weapon> weaponPool = new ArrayList<>();
        ArrayList<Skill> skillPool = new ArrayList<>();
        level += (int)(Math.random() * 5) - 2;
        for(int i = 1; i < level; i++) {
            opponent.levelup();
        }
        weaponPool.add(Weapon.Fist);
        weaponPool.add(Weapon.IronSword);
        weaponPool.add(Weapon.IronAxe);
        weaponPool.add(Weapon.IronLance);
        weaponPool.add(Weapon.Heal);
        weaponPool.add(Weapon.Fire);
        weaponPool.add(Weapon.Wind);
        weaponPool.add(Weapon.Thunder);
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        skillPool.add(Skill.NA);
        if(level > 10) {
            weaponPool.add(Weapon.SteelSword);
            weaponPool.add(Weapon.SteelAxe);
            weaponPool.add(Weapon.SteelLance);
            weaponPool.add(Weapon.Elfire);
            weaponPool.add(Weapon.Elwind);
            weaponPool.add(Weapon.Elthunder);
            weaponPool.add(Weapon.Mend);
            skillPool.add(Skill.Crit15);
        }
        if(level > 20) {
            weaponPool.remove(Weapon.IronSword);
            weaponPool.remove(Weapon.IronAxe);
            weaponPool.remove(Weapon.IronLance);
            weaponPool.remove(Weapon.Fire);
            weaponPool.remove(Weapon.Wind);
            weaponPool.remove(Weapon.Thunder);
            weaponPool.remove(Weapon.Heal);
            skillPool.add(Skill.NA);
            skillPool.add(Skill.Vantage);
        }
        opponent.weapon = weaponPool.get((int)(Math.random() * weaponPool.size()));
        opponent.skill = skillPool.get((int)(Math.random() * skillPool.size()));
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

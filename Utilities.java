package bot.willbot;

import java.util.*;

public class Utilities{
    static ArrayList<Player> generics = new ArrayList<>();
    
    public static void init() {
                                                    //chp, thp, str, mag, spd, def, res, skl, lck, lvl
        generics.add(new Player("SwordFighter", new Stats(18, 18, 11, 5, 5, 6, 3, 4, 6, 1, 0), new Growths(), Weapon.IronSword, Skill.NA, -1));
        generics.add(new Player("AxeFighter", new Stats(18, 18, 11, 5, 5, 6, 3, 5, 5, 1, 0), new Growths(), Weapon.IronAxe, Skill.NA, -1));
        generics.add(new Player("LanceFighter", new Stats(18, 18, 11, 5, 5, 6, 3, 6, 4, 1, 0), new Growths(), Weapon.IronLance, Skill.NA, -1));
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
        for(int i = 1; i < level; i++) {
            opponent.levelup();
        }
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
    
    public static int getXpLevelUp(int level) {
        int[] values = {100, 110, 121, 133, 146, 161, 177, 194, 214, 235, 259, 285, 313, 345, 379, 417, 459, 505,
            555, 611, 672, 740, 814, 895, 984, 1083, 1191, 1310, 1442, 1586, 1744, 1919, 2111, 2322, 2554, 2810, 3091, 3400, 3740};
        return values[level - 1];
    }
}

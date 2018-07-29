package bot.willbot;

import java.util.ArrayList;
import java.util.Arrays;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Stats implements Category{
    int lvl, xp, chp, thp, str, mag, spd, def, res, skl, lck;
    
    public Stats(int[] stats) {     //stratum enemies, bosses
        chp = stats[0];
        thp = stats[1];
        str = stats[2];
        mag = stats[3];
        spd = stats[4];
        def = stats[5];
        res = stats[6];
        skl = stats[7];
        lck = stats[8];
        lvl = stats[9];
        xp = stats[10];
    }
    
    public Stats(PClass pclass) {  //player character with class selected
        int[] stats = assignVariation(Arrays.copyOf(pclass.stats, pclass.stats.length));
        chp = stats[0];
        thp = stats[0];
        str = stats[1];
        mag = stats[2];
        spd = stats[3];
        def = stats[4];
        res = stats[5];
        skl = stats[6];
        lck = stats[7];
        lvl = 1;
        xp = 0;
    }
    
    public Stats() {    //player character with no class selected
        str = (int)(Math.random() * 4) + 3;
        mag = (int)(Math.random() * 6);
        spd = (int)(Math.random() * 8) + 2;
        def = (int)(Math.random() * 5) + 1;
        res = (int)(Math.random() * 7);
        skl = (int)(Math.random() * 5) + 1;
        lck = (int)(Math.random() * 7);
        lvl = 1;
        xp = 0;
        thp = 45 + (int)(Math.random() * 4) - str - mag - spd - def - res - skl - lck;
        chp = thp;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!stats") || action.equals("w!reroll") || action.equals("w!weaponranks");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(action.equals("w!stats")) {        //w!growths <user>
            if (!args.isEmpty()) {
                if (!Bot.playermap.containsKey(args.get(0))) {
                    c.sendMessage("You did not specify a valid user.").queue();
                    return;
                }
                c.sendMessage(Bot.playermap.get(args.get(0)).showStats().build()).queue();
            } else {
                if(Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                    c.sendMessage(Bot.defaultPlayer.get(event.getAuthor().getIdLong()).showStats().build()).queue();
                }
                else {
                    throw new ValidationException("You did not specify a user. The correct usage is `w!stats <user>`.");
                }
            }         
        }
        else if(action.equals("w!reroll")) {      //w!reroll <user>
            if (!args.isEmpty()) {
                Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
                Player p = Bot.playermap.get(args.get(0));
                p.reroll();       
                c.sendMessage("Your stats have been rerolled!").queue();
                c.sendMessage(p.showStats().build()).queue();
                Bot.update();       //update config.txt
            } else {
                if(Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                    Player p = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                    p.reroll();
                    c.sendMessage("Your stats have been rerolled!").queue();
                    c.sendMessage(p.showStats().build()).queue();
                    Bot.update();
                }
                else {
                    throw new ValidationException("You did not specify a user. The correct usage is `w!reroll <user>`.");
                }
            }       
        }
        else if(action.equals("w!weaponranks")) {       //w!weaponranks <user>
            if (!args.isEmpty()) {
                if (!Bot.playermap.containsKey(args.get(0))) {
                    c.sendMessage("You did not specify a valid user.").queue();
                    return;
                }
                c.sendMessage(Bot.playermap.get(args.get(0)).showRanks().build()).queue();
            } 
            else if(Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                c.sendMessage(Bot.defaultPlayer.get(event.getAuthor().getIdLong()).showRanks().build()).queue();
            }
            else {
                throw new ValidationException("You did not specify a user. The correct usage is `w!weaponranks <user>`.");
            }   
        }
    }
    
    private static int[] assignVariation(int[] stats) {
        for(int i = 0; i < 4; i++) {
            int rng = (int)(Math.random() * 8), value = 1;
            if(i%2 != 0) {  //bane
                value = -1;
            }
            if(i >= 2) {    //double
                value *= 2;
            }
            stats[rng] += value;
        }
        for(int i = 0; i < stats.length; i++) {       //boundary checks
            if(stats[i] < 0) {
                stats[i] = 0;
            }
        }
        return stats;
    }
    
    public int[] toArray() {
        int[] stats = new int[11];
        stats[0] = chp;
        stats[1] = thp;
        stats[2] = str;
        stats[3] = mag;
        stats[4] = spd;
        stats[5] = def;
        stats[6] = res;
        stats[7] = skl;
        stats[8] = lck;
        stats[9] = lvl;
        stats[10] = xp;
        return stats;
    }
}

class Growths{
    int hp, str, mag, spd, def, res, skl, lck;
    
    public Growths(int[] stats) {       //stratum enemies, bosses
        hp = stats[0];
        str = stats[1];
        mag = stats[2];
        spd = stats[3];
        def = stats[4];
        res = stats[5];
        skl = stats[6];
        lck = stats[7];
    }
    
    public Growths(PClass pclass) {     //player characters with class selected 
        int[] growths = assignVariation(pclass.growths);
        hp = growths[0];
        str = growths[1];
        mag = growths[2];
        spd = growths[3];
        def = growths[4];
        res = growths[5];
        skl = growths[6];
        lck = growths[7];
    }
    
    private static int[] assignVariation(int[] growths) {
        for(int i = 0; i < 4; i++) {
            int rng = (int)(Math.random() * 8), value = 10;
            if(i%2 != 0) {  //bane
                value = -10;
            }
            if(i >= 2) {    //double
                value *= 2;
            }
            growths[rng] += value;
        }
        for(int i = 0; i < growths.length; i++) {       //boundary checks
            if(growths[i] < 5) {
                growths[i] = 5;
            }
            else if(growths[i] > 95) {
                growths[i] = 95;
            }
        }
        return growths;
    }
    
    public Growths() {          //player characters with no class selected
        str = (int)(Math.random() * 71) + 5;
        mag = (int)(Math.random() * 71) + 5;
        spd = (int)(Math.random() * 71) + 5;
        if(str + mag + spd > 150) {
            str -= 10;
            mag -= 10;
            spd -= 10;
        }
        if(str + mag + spd < 60) {
            str += 10 + (int)(Math.random() * 41);
            mag += 10 + (int)(Math.random() * 41);
            spd += 10 + (int)(Math.random() * 41);
        }
        def = (int)(Math.random() * 71) + 5;
        res = (int)(Math.random() * 71) + 5;
        if(str + mag + spd + def + res > 230) {
            str -= 10;
            mag -= 10;
            spd -= 10;
            def -= 10;
            res -= 10;
        }
        if(str + mag + spd + def + res > 100) {
            str += 10 + (int)(Math.random() * 31);
            mag += 10 + (int)(Math.random() * 31);
            spd += 10 + (int)(Math.random() * 31);
            def += 10 + (int)(Math.random() * 31);
            res += 10 + (int)(Math.random() * 31);
        }
        skl = (int)(Math.random() * 71) + 5;
        lck = (int)(Math.random() * 71) + 5;
        while(str + mag + spd + def + res + skl + lck > 325) {
            str -= 5 + (int)(Math.random() * 16);
            mag -= 5 + (int)(Math.random() * 16);
            spd -= 5 + (int)(Math.random() * 16);
            def -= 5 + (int)(Math.random() * 16);
            res -= 5 + (int)(Math.random() * 16);
            skl -= 5 + (int)(Math.random() * 16);
            lck -= 5 + (int)(Math.random() * 16);
        }
        while(str + mag + spd + def + res + skl + lck < 230) {
            str += 5 + (int)(Math.random() * 16);
            mag += 5 + (int)(Math.random() * 16);
            spd += 5 + (int)(Math.random() * 16);
            def += 5 + (int)(Math.random() * 16);
            res += 5 + (int)(Math.random() * 16);
            skl += 5 + (int)(Math.random() * 16);
            lck += 5 + (int)(Math.random() * 16);
        }
        
        if(str < 5)
            str = 5;
        if(mag < 5)
            mag = 5;
        if(spd < 5)
            spd = 5;
        if(def < 5)
            def = 5;
        if(res < 5)
            res = 5;
        if(skl < 5)
            skl = 5;
        if(lck < 5)
            lck = 5;
        //
        if(str > 90)
            str = 90;
        if(mag > 90)
            mag = 90;
        if(spd > 90)
            spd = 90;
        if(def > 90)
            def = 90;
        if(res > 90)
            res = 90;
        if(skl > 90)
            skl = 90;
        if(lck > 80)
            lck = 80;
        hp = 330 + (int)(Math.random() * 20) - str - mag - spd - def - res - skl - lck;
    }
    
    public int[] toArray() {
        int[] growths = new int[8];
        growths[0] = hp;
        growths[1] = str;
        growths[2] = mag;
        growths[3] = spd;
        growths[4] = def;
        growths[5] = res;
        growths[6] = skl;
        growths[7] = lck;
        return growths;
    }
}

class WeaponRanks {
    int sword, axe, lance, staff, fire, wind, thunder,
            swordx, axex, lancex, staffx, firex, windx, thunderx;
    
    public WeaponRanks() {
        sword = 0;
        axe = 0;
        lance = 0;
        staff = 0;
        fire = 0;
        wind = 0;
        thunder = 0;
        swordx = 0;
        axex = 0;
        lancex = 0;
        staffx = 0;
        firex = 0;
        windx = 0;
        thunderx = 0;
    }
    
    public WeaponRanks(int sword, int axe, int lance, int staff, int fire, int wind, int thunder, int swordx, int axex, int lancex, int staffx, int firex, int windx, int thunderx) {
        this.sword = sword;
        this.axe = axe;
        this.lance = lance;
        this.staff = staff;
        this.fire = fire;
        this.wind = wind;
        this.thunder = thunder;
        this.swordx = swordx;
        this.axex = axex;
        this.lancex = lancex;
        this.staffx = staffx;
        this.firex = firex;
        this.windx = windx;
        this.thunderx = thunderx;
    }
    
    public WeaponRanks(int[] stats) {
        sword = stats[0];
        axe = stats[1];
        lance = stats[2];
        staff = stats[3];
        fire = stats[4];
        wind = stats[5];
        thunder = stats[6];
        swordx = stats[7];
        axex = stats[8];
        lancex = stats[9];
        staffx = stats[10];
        firex = stats[11];
        windx = stats[12];
        thunderx = stats[13];
    }
    
    public int[] toArray() {
        int[] stats = new int[14];
        stats[0] = sword;
        stats[1] = axe;
        stats[2] = lance;
        stats[3] = staff;
        stats[4] = fire;
        stats[5] = wind;
        stats[6] = thunder;
        stats[7] = swordx;
        stats[8] = axex;
        stats[9] = lancex;
        stats[10] = staffx;
        stats[11] = firex;
        stats[12] = windx;
        stats[13] = thunderx;
        return stats;
    }
    
    public static String toRank(int rank) {
        switch(rank) {
            case 0:
                return "E";
            case 1:
                return "D";
            case 2:
                return "C";
            case 3:
                return "B";
            case 4:
                return "A";
            case 5:
                return "S";
            default:
                return "broken plz fix";
        }
    }
    
    public static int getXpLevelUp(int level) {
        int[] values = {30, 40, 50, 60, 70};
        if(level > 4) {
            return Integer.MAX_VALUE;
        }
        return values[level];
    }
}

package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Stats implements Category{
    int lvl, xp, chp, thp, str, mag, spd, def, res, skl, lck;
    
    public Stats(int[] stats) {
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
    
    public Stats(int chp, int thp, int str, int mag, int spd, int def, int res, int skl, int lck, int lvl, int xp) {
        this.chp = chp;
        this.thp = thp;
        this.str = str;
        this.mag = mag;
        this.spd = spd;
        this.def = def;
        this.res = res;
        this.skl = skl;
        this.lck = lck;
        this.lvl = lvl;
        this.xp = xp;
    }
    
    public Stats() {    //generating player characters only
        str = (int)(Math.random() * 8) + 3;
        mag = (int)(Math.random() * 8) + 3;
        spd = (int)(Math.random() * 16);
        def = (int)(Math.random() * 7) + 2;
        res = (int)(Math.random() * 7) + 2;
        skl = (int)(Math.random() * 20) + 1;
        lck = (int)(Math.random() * 20) + 1;
        lvl = 1;
        xp = 0;
        thp = 100 + (int)(Math.random() * 4) - str - mag - spd - def - res - skl - lck;
        chp = thp;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!stats") || action.equals("w!reroll");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(action.equals("w!stats")) {        //w!stats <user>
            if (!args.isEmpty()) {
                if (!Bot.playermap.containsKey(args.get(0))) {
                    c.sendMessage("You did not specify a valid user.").queue();
                    return;
                }
                c.sendMessage(Bot.playermap.get(args.get(0)).showStats().build()).queue();
            } else {
                c.sendMessage("You did not specify a user. The correct usage is `w!stats <user>`.").queue();
            }         
        }
        else {      //w!reroll <user>
            if (!args.isEmpty()) {
                Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
                Player p = Bot.playermap.get(args.get(0));
                p.reroll();       
                c.sendMessage("Your stats have been rerolled!").queue();
                c.sendMessage(p.showStats().build()).queue();
                Bot.update();       //update config.txt
            } else {
                throw new ValidationException("You did not specify a user. The correct usage is `w!reroll <user>`.");
            }       
        }
    }
}

class Growths{
    int hp, str, mag, spd, def, res, skl, lck;
    
    public Growths(int[] stats) {
        hp = stats[0];
        str = stats[1];
        mag = stats[2];
        spd = stats[3];
        def = stats[4];
        res = stats[5];
        skl = stats[6];
        lck = stats[7];
    }
    
    public Growths() {    
        str = (int)(Math.random() * 81) + 5;
        mag = (int)(Math.random() * 81) + 5;
        spd = (int)(Math.random() * 81) + 5;
        if(str + mag + spd > 180) {
            str -= 10;
            mag -= 10;
            spd -= 10;
        }
        if(str + mag + spd < 60) {
            str += 10 + (int)(Math.random() * 41);
            mag += 10 + (int)(Math.random() * 41);
            spd += 10 + (int)(Math.random() * 41);
        }
        def = (int)(Math.random() * 81) + 5;
        res = (int)(Math.random() * 81) + 5;
        if(str + mag + spd + def + res > 280) {
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
        skl = (int)(Math.random() * 81) + 5;
        lck = (int)(Math.random() * 81) + 5;
        if(str + mag + spd + def + res + skl + lck > 395) {
            str -= 10+ (int)(Math.random() * 16);
            mag -= 10+ (int)(Math.random() * 16);
            def -= 10+ (int)(Math.random() * 16);
            res -= 10+ (int)(Math.random() * 16);
        }
        if(str + mag + spd + def + res + skl + lck < 300) {
            str += 10 + (int)(Math.random() * 16);
            mag += 10 + (int)(Math.random() * 16);
            def += 10 + (int)(Math.random() * 16);
            res += 10 + (int)(Math.random() * 16);
        }
        if(str < 5) {
            str = 5;
        }
        if(mag < 5) {
            mag = 5;
        }
        if(spd < 5) {
            spd = 5;
        }
        if(def < 5) {
            def = 5;
        }
        if(res < 5) {
            res = 5;
        }
        if(skl < 5) {
            skl = 5;
        }
        if(lck < 5) {
            lck = 5;
        }
        //
        if(str > 95) {
            str = 95;
        }
        if(mag > 95) {
            mag = 95;
        }
        if(spd > 95) {
            spd = 95;
        }
        if(def > 95) {
            def = 95;
        }
        if(res > 95) {
            res = 95;
        }
        if(skl > 95) {
            skl = 95;
        }
        if(lck > 95) {
            lck = 95;
        }
        hp = 400 + (int)(Math.random() * 20) - str - mag - spd - def - res - skl - lck;
    }
}

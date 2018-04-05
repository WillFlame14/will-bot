package bot.willbot;

import java.awt.Color;
import net.dv8tion.jda.core.EmbedBuilder;

public class Player {
    String username;
    Stats stats;
    Growths growths;
    Weapon weapon;
    Skill skill;
    Long authorid;
    boolean[] levelup = new boolean[8];
    
    public Player(String username, Stats stats, Growths growths, Weapon weapon, Skill skill, long authorid) {
        this.username = username;
        this.stats = stats;
        this.growths = growths;
        this.weapon = weapon;
        this.skill = skill;
        this.authorid = authorid;
    }
    
    public String toDisplayString() {
        return "**" + username + "'s Stats:**"
                + "\nLv: " + stats.lvl
                + "\nHP: " + stats.chp + "/" + stats.thp
                + "\nStr: " + stats.str
                + "\nMag: " + stats.mag
                + "\nSpd: " + stats.spd
                + "\nDef: " + stats.def
                + "\nRes: " + stats.res
                + "\nSkl: " + stats.skl
                + "\nLck: " + stats.lck;
    }
    
    public String toEmbedString() {
        return "\nLv: " + stats.lvl 
                + "\nHP: " + stats.chp + "/" + stats.thp + (levelup[0]?" **(+1)**":"")
                + "\nStr: " + stats.str + (levelup[1]?" **(+1)**":"")
                + "\nMag: " + stats.mag + (levelup[2]?" **(+1)**":"")
                + "\nSpd: " + stats.spd + (levelup[3]?" **(+1)**":"")
                + "\nDef: " + stats.def + (levelup[4]?" **(+1)**":"")
                + "\nRes: " + stats.res + (levelup[5]?" **(+1)**":"")
                + "\nSkl: " + stats.skl + (levelup[6]?" **(+1)**":"")
                + "\nLck: " + stats.lck + (levelup[7]?" **(+1)**":"");
    }
    
    public String toString() {
        return username + ",.," + stats.chp + ",.," + stats.thp + ",.," + stats.str + ",.," + stats.mag + ",.," + stats.spd
                + ",.," + stats.def + ",.," + stats.res + ",.," + stats.skl + ",.," + stats.lck + ",.," + stats.lvl + ",.," + stats.xp
                + ",.," + growths.hp + ",.," + growths.str + ",.," + growths.mag + ",.," + growths.spd + ",.," + growths.def + ",.," + growths.res 
                + ",.," + growths.skl + ",.," + growths.lck + ",.,"+ weapon.name() + ",.," + skill.name() + ",.," + authorid;
    }
    
    public Player clone() {
        int[]oldstats = new int[11];
        int[]oldgrowths = new int[8];
        oldstats[0] = stats.chp;
        oldstats[1] = stats.thp;
        oldstats[2] = stats.str;
        oldstats[3] = stats.mag;
        oldstats[4] = stats.spd;
        oldstats[5] = stats.def;
        oldstats[6] = stats.res;
        oldstats[7] = stats.skl;
        oldstats[8] = stats.lck;
        oldstats[9] = stats.lvl;
        oldstats[10] = stats.xp;
        oldgrowths[0] = growths.hp;
        oldgrowths[1] = growths.str;
        oldgrowths[2] = growths.mag;
        oldgrowths[3] = growths.spd;
        oldgrowths[4] = growths.def;
        oldgrowths[5] = growths.res;
        oldgrowths[6] = growths.skl;
        oldgrowths[7] = growths.lck;
        
        return new Player(username, new Stats(oldstats), new Growths(oldgrowths), weapon, skill, authorid);
    }
    
    public void reroll() {
        stats = new Stats();
    }
    
    public void levelup() {
        for(int i = 0; i < 8; i++) {
            levelup[i] = false;
        }
        if(Math.random() * 100 < growths.hp) {
            stats.thp++;
            stats.chp++;
            levelup[0] = true;
        }
        if(Math.random() * 100 < growths.str) {
            stats.str++;
            levelup[1] = true;
        }
        if(Math.random() * 100 < growths.mag) {
            stats.mag++;
            levelup[2] = true;
        }
        if(Math.random() * 100 < growths.spd) {
            stats.spd++;
            levelup[3] = true;
        }
        if(Math.random() * 100 < growths.def) {
            stats.def++;
            levelup[4] = true;
        }
        if(Math.random() * 100 < growths.res) {
            stats.res++;
            levelup[5] = true;
        }
        if(Math.random() * 100 < growths.skl) {
            stats.skl++;
            levelup[6] = true;
        }
        if(Math.random() * 100 < growths.lck) {
            stats.lck++;
            levelup[7] = true;
        }
        stats.lvl++;
    }
    
    public EmbedBuilder showStats() {
        EmbedBuilder statsEmbed = new EmbedBuilder();
        statsEmbed.setTitle("**" + username + "**", null);
        statsEmbed.setColor(Color.blue);
        statsEmbed.setDescription(toEmbedString());
        return statsEmbed;
    }
    
    public boolean activateSkill() {
        switch(skill) {
            case Adept:
            case Vantage:
            case Cancel:
                return stats.spd > Math.random() * 100;
            case Resolve:
            case Wrath:
                return stats.chp <= stats.thp / 2;
            default:        //100% activation skills, like Crit+10
                return true;
        }
    }
}

enum Colour {
    RED, BLUE, GREEN, COLOURLESS;
}

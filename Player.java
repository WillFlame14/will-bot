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
    
    public String toEmbedString() {
        return "\n**Lv:** " + stats.lvl 
                + "\n**HP:** " + stats.chp + "/" + stats.thp + (levelup[0]?" **(+1)**":"")
                + "\n**Str:** " + stats.str + (levelup[1]?" **(+1)**":"")
                + "\n**Mag:** " + stats.mag + (levelup[2]?" **(+1)**":"")
                + "\n**Spd:** " + stats.spd + (levelup[3]?" **(+1)**":"")
                + "\n**Def:** " + stats.def + (levelup[4]?" **(+1)**":"")
                + "\n**Res:** " + stats.res + (levelup[5]?" **(+1)**":"")
                + "\n**Skl:** " + stats.skl + (levelup[6]?" **(+1)**":"")
                + "\n**Lck:** " + stats.lck + (levelup[7]?" **(+1)**":"");
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
        boolean aptitude = (skill == Skill.Aptitude);
        for(int i = 0; i < 8; i++) {
            levelup[i] = false;
        }
        if(Math.random() * 100 < growths.hp + (aptitude?10:0)) {
            stats.thp++;
            stats.chp++;
            levelup[0] = true;
        }
        if(Math.random() * 100 < growths.str + (aptitude?10:0)) {
            stats.str++;
            levelup[1] = true;
        }
        if(Math.random() * 100 < growths.mag + (aptitude?10:0)) {
            stats.mag++;
            levelup[2] = true;
        }
        if(Math.random() * 100 < growths.spd + (aptitude?10:0)) {
            stats.spd++;
            levelup[3] = true;
        }
        if(Math.random() * 100 < growths.def + (aptitude?10:0)) {
            stats.def++;
            levelup[4] = true;
        }
        if(Math.random() * 100 < growths.res + (aptitude?10:0)) {
            stats.res++;
            levelup[5] = true;
        }
        if(Math.random() * 100 < growths.skl + (aptitude?10:0)) {
            stats.skl++;
            levelup[6] = true;
        }
        if(Math.random() * 100 < growths.lck + (aptitude?10:0)) {
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
                return stats.spd > (int)(Math.random() * 100);
            case Resolve:
            case Wrath:
                return stats.chp <= stats.thp / 2;
            case Pavise:
                return stats.skl > (int)(Math.random() * 100);
            default:        //100% activation skills: Aptitude, Crit+10, Fortune
                return true;
        }
    }
}

enum Colour {
    RED, BLUE, GREEN, COLOURLESS;
}

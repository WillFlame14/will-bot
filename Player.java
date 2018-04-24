package bot.willbot;

import java.awt.Color;
import net.dv8tion.jda.core.EmbedBuilder;

public class Player {
    String username;
    Stats stats;
    Growths growths;
    Weapon weapon;
    WeaponRanks ranks;
    Skill skill;
    Long authorid;
    static boolean[] levelup = new boolean[8];
    
    public Player(String username, Stats stats, Growths growths, Weapon weapon, WeaponRanks ranks, Skill skill, long authorid) {
        this.username = username;
        this.stats = stats;
        this.growths = growths;
        this.weapon = weapon;
        this.ranks = ranks;
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
        String s = username + ",.,";
        for(int i: stats.toArray()) {
            s += i + ",.,";
        }
        for(int i: growths.toArray()) {
            s += i + ",.,";
        }
        s += weapon.name() + ",.,";
        for(int i: ranks.toArray()) {
            s += i + ",.,";
        }
        s += skill.name() + ",.," + authorid;
        return s;
    }
    
    public String getWeaponRanks() {
        return "\n**Sword** " + WeaponRanks.toRank(ranks.sword) 
                + "\n**Axe:** " + WeaponRanks.toRank(ranks.axe) 
                + "\n**Lance:** " + WeaponRanks.toRank(ranks.lance) 
                + "\n**Staff:** " + WeaponRanks.toRank(ranks.staff) 
                + "\n**Fire:** " + WeaponRanks.toRank(ranks.fire) 
                + "\n**Wind:** " + WeaponRanks.toRank(ranks.wind) 
                + "\n**Thunder:** " + WeaponRanks.toRank(ranks.thunder); 
    }
    
    public Player duplicate() {
        return new Player(username, new Stats(stats.toArray()), new Growths(growths.toArray()), weapon, new WeaponRanks(ranks.toArray()), skill, authorid);
    }
    
    public void reroll() {
        stats = new Stats();
    }
    
    public void levelup() {
        boolean aptitude = (skill == Skill.Aptitude), blossom = (skill == Skill.Blossom);
        int[] newstats = stats.toArray();
        int[] growthrates = growths.toArray();
        for(int i = 0; i < 8; i++) {
            levelup[i] = false;
            if(Math.random() * 100 < growthrates[i] + (aptitude?10:0)) {
                newstats[i + 1]++;      //+1 since chp is not counted
                levelup[i] = true;
            }
            else if(blossom && Math.random() * 100 < growthrates[i]) {      //try again if failed
                newstats[i + 1]++;  
                levelup[i] = true;
            }
        }
        stats = new Stats(newstats);
        stats.lvl++;
    }
    
    public EmbedBuilder showStats() {
        EmbedBuilder statsEmbed = new EmbedBuilder();
        statsEmbed.setTitle("**" + username + "**", null);
        statsEmbed.setColor(Color.blue);
        statsEmbed.setDescription(toEmbedString());
        return statsEmbed;
    }
    
    public EmbedBuilder showRanks() {
        EmbedBuilder statsEmbed = new EmbedBuilder();
        statsEmbed.setTitle("**" + username + "**", null);
        statsEmbed.setColor(Color.blue);
        statsEmbed.setDescription(getWeaponRanks());
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
            case Luna:
            case Pavise:
            case Sol:
                return stats.skl > (int)(Math.random() * 100);
            default:        //100% activation skills: Aptitude, Blossom, Crit+15, Discipline, Fortune, Gamble, Paragon
                return true;
        }
    }
    
    public static void clearLevelUp() {
        for(int i = 0; i < levelup.length; i++) {
            levelup[i] = false;
        }
    }

    public boolean checkSame(Player p) {
        return ((weapon == p.weapon) && (stats.chp == p.stats.chp) && (stats.lvl == stats.lvl));
    }
    
    public boolean isBoss() {
        return true;
    }
}

class Boss extends Player {
    int bossid;
    
    public Boss(String username, Stats stats, Weapon weapon, Skill skill, int authorid, int bid) {
        super(username, stats, new Growths(), weapon, new WeaponRanks(), skill, authorid);
        bossid = bid;
    }
}

enum Colour {
    RED, BLUE, GREEN, COLOURLESS;
}

class Players {
    Player player;
    Player enemy;
    
    public Players(Player p, Player e) {
        player = p;
        enemy = e;
    }
}

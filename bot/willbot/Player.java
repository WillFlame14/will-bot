package bot.willbot;

import java.awt.Color;
import java.util.*;
import net.dv8tion.jda.core.EmbedBuilder;

public class Player {
    String username;
    Stats stats;
    Growths growths;
    Weapon weapon;
    WeaponRanks ranks;
    Skill skill;
    Long authorid;
    PClass pclass;
    static boolean[] levelup = new boolean[8];
    
    public Player(String username, Stats stats, Growths growths, Weapon weapon, WeaponRanks ranks, Skill skill, PClass pclass, long authorid) {
        this.username = username;
        this.stats = stats;
        this.growths = growths;
        this.weapon = weapon;
        this.ranks = ranks;
        this.skill = skill;
        this.pclass = pclass;
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
        s += skill.name() + ",.," + pclass.name() + ",.," + authorid;
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
        return new Player(username, new Stats(stats.toArray()), new Growths(growths.toArray()), weapon, new WeaponRanks(ranks.toArray()), skill, pclass, authorid);
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
        statsEmbed.setTitle("**" + username + "** - " + pclass.toString(), null);
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
    
    public Boss(String username, Stats stats, Weapon weapon, Skill skill, int authorid, PClass pclass, int bid) {
        super(username, stats, new Growths(), weapon, new WeaponRanks(), skill, pclass, authorid);
        bossid = bid;
    }
}

enum PClass {                      //hp, str, mag, spd, def, res, skl, lck
    Myrmidon("Myrmidon", 3, new int[] {18, 6, 0, 11, 4, 0, 10, 0}, new int[] {65, 40, 20, 50, 40, 40, 45, 30}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Sword))), 
    Soldier("Soldier", 3, new int[] {19, 8, 0, 9, 6, 1, 8, 0}, new int[] {70, 50, 20, 50, 40, 40, 45, 25}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Lance))), 
    Fighter("Fighter", 3, new int[] {20, 10, 0, 8, 7, 0, 6, 0}, new int[] {80, 50, 5, 45, 40, 30, 60, 25}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Axe))), 
    Cavalier("Cavalier", 5, new int[] {19, 6, 0, 8, 8, 0, 5, 0}, new int[] {75, 50, 10, 35, 45, 40, 40, 25}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Sword, WeaponType.Lance))),
    PegasusKnight("Pegasus Knight", 4, new int[] {16, 5, 0, 9, 4, 8, 6, 0}, new int[] {65, 35, 15, 50, 35, 35, 50, 45}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Lance))), 
    Knight("Knight", 2, new int[] {22, 9, 0, 5, 11, 4, 6, 0}, new int[] {60, 30, 10, 35, 35, 10, 20, 30}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Sword, WeaponType.Axe, WeaponType.Lance))), 
    WyvernRider("Wyvern Rider", 4, new int[] {20, 7, 0, 5, 9, 0, 5, 0}, new int[] {80, 65, 0, 55, 45, 5, 55, 15}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Axe))),
    Priest("Priest", 3, new int[] {17, 0, 6, 3, 1, 10, 3, 0}, new int[] {60, 30, 35, 60, 20, 60, 30, 75}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Staff))),
    FireMage("Fire Mage", 3, new int[] {17, 4, 4, 7, 3, 5, 6, 0}, new int[] {50, 20, 65, 40, 25, 55, 40, 25}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Fire))),
    WindMage("Wind Mage", 3, new int[] {16, 3, 6, 7, 2, 6, 6, 0}, new int[] {60, 20, 65, 40, 30, 45, 40, 20}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Wind))),
    ThunderMage("Thunder Mage", 3, new int[] {16, 5, 5, 4, 2, 5, 7, 0}, new int[] {55, 15, 65, 45, 30, 45, 45, 20}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Thunder))),
    Hero("Hero", 3, new int[] {18, 6, 0, 11, 4, 0, 10, 0}, new int[] {65, 40, 20, 50, 40, 40, 45, 30}, new ArrayList<WeaponType>(Arrays.asList(WeaponType.Sword, WeaponType.Axe, WeaponType.Lance)));
    
    String displayName;
    int mov;
    int[] stats, growths;
    ArrayList<WeaponType> usableWeapons;
    
    PClass(String dn, int move, int[] stats, int[] growths, ArrayList<WeaponType> weapons) {
        displayName = dn;
        mov = move;
        this.stats = stats;
        this.growths = growths;
        usableWeapons = weapons;
    }
    
    public String toString() {
        return displayName;
    }
    
    public static PClass roll() {
        return PClass.values()[(int)(Math.random() * PClass.values().length)];
    }
}

class Players {
    Player player;
    Player enemy;
    
    public Players(Player p, Player e) {
        player = p;
        enemy = e;
    }
}

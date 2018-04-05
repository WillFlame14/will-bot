package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Attack implements Category{
    static boolean userdouble, enemydouble, userSkill, enemySkill, stratum;
    static int userCritChance, enemyCritChance, playerDmg, enemyDmg;
    static String levelup = "";
    static Player player, enemy;
    MessageChannel c;
    
    public abstract boolean isActionApplicable(String action);
    
    public abstract void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException;
    
    protected EmbedBuilder battleCalc(Player p, Player e, boolean physical) {
        player = p;
        enemy = e;
        EmbedBuilder battleCalc = new EmbedBuilder();
        int triangle = 0;
        String arrow1 = "", arrow2 = "";
        switch(player.weapon.colour) {
            case RED:
                if(enemy.weapon.colour.equals(Colour.BLUE)) {
                    triangle = -10;
                    arrow1 = "⬇";
                    arrow2 = "⬆";
                }
                else if (enemy.weapon.colour.equals(Colour.GREEN)){
                    triangle = 10;
                    arrow1 = "⬆";
                    arrow2 = "⬇";
                }
                break;
            case GREEN:
                if(enemy.weapon.colour.equals(Colour.BLUE)) {
                    triangle = 10;
                    arrow1 = "⬆";
                    arrow2 = "⬇";
                }
                else if (enemy.weapon.colour.equals(Colour.RED)){
                    triangle = -10;
                    arrow1 = "⬇";
                    arrow2 = "⬆";
                }
                break;
            case BLUE:
                if(enemy.weapon.colour.equals(Colour.GREEN)) {
                    triangle = -10;
                    arrow1 = "⬇";
                    arrow2 = "⬆";
                }
                else if (enemy.weapon.colour.equals(Colour.RED)){
                    triangle = 10;
                    arrow1 = "⬆";
                    arrow2 = "⬇";
                }
                break;
        }
        userCritChance = player.stats.lck + triangle - enemy.stats.lck;
        enemyCritChance = enemy.stats.lck - triangle - player.stats.lck;
        if(userCritChance < 0) {
            userCritChance = 0;
        }
        if(enemyCritChance < 0) {
            enemyCritChance = 0;
        }
        playerDmg = physical?player.stats.str - enemy.stats.def : player.stats.mag - enemy.stats.res;
        enemyDmg = enemyDmg = physical?enemy.stats.str - player.stats.def : enemy.stats.mag - player.stats.res;
        if(playerDmg < 0) {
            playerDmg = 0;
        }
        if(enemyDmg < 0) {
            enemyDmg = 0;
        }
        
        int userResolve1 = 0, userResolve2 = 0, enemyResolve1 = 0, enemyResolve2 = 0;      //the bonuses from Resolve
        boolean userHighlght = false, enemyHighlight = false;
        userSkill = player.activateSkill();
        enemySkill = enemy.activateSkill();
        
        if(userSkill) {
            switch (player.skill) {
                case Crit15:
                    userCritChance += 15;
                    userHighlght = true;
                    break;
                case Resolve:
                    int oldSpd = player.stats.spd, oldSkl = player.stats.skl;
                    player.stats.spd *= 1.5;
                    player.stats.skl *= 1.5;
                    userResolve1 = player.stats.spd - oldSpd;
                    userResolve2 = player.stats.skl - oldSkl;
                    userHighlght = true;
                    break;
                case Wrath:
                    userCritChance += 50;
                    userHighlght = true;
                    break;
            }
        }
        
        if(enemySkill) {
            switch (enemy.skill) {
                case Crit15:
                    enemyCritChance += 15;
                    enemyHighlight = true;
                    break;
                case Resolve:
                    int oldSpd = enemy.stats.spd, oldSkl = enemy.stats.skl;
                    enemy.stats.spd *= 1.5;
                    enemy.stats.skl *= 1.5;
                    enemyResolve1 = enemy.stats.spd - oldSpd;
                    enemyResolve2 = enemy.stats.skl - oldSkl;
                    enemyHighlight = true;
                    break;
                case Wrath:
                    enemyCritChance += 50;
                    enemyHighlight = true;
                    break;
            }
        }
        
        userdouble = (player.stats.spd - enemy.stats.spd > 3);
        enemydouble = (enemy.stats.spd - player.stats.spd > 3);
        
        String username = player.username, enemyname = enemy.username;
        battleCalc.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname), null);
        battleCalc.setDescription("==============================");
        battleCalc.addField(Utilities.bold(username) + " " + arrow1, "Lv: " + player.stats.lvl
                + "\nHP: " + player.stats.chp + "/" + player.stats.thp
                + "\nWeapon: " + player.weapon.displayName
                + "\nSkill: " + (userHighlght?Utilities.highlight(player.skill.displayName):player.skill.displayName)
                + "\nDamage: " + playerDmg + (userdouble?"x2":"")
                + "\nCritical: " + userCritChance + "%", true);
        battleCalc.addField(Utilities.bold(enemyname) + " " + arrow2, "Lv: " + enemy.stats.lvl
                + "\nHP: " + enemy.stats.chp + "/" + enemy.stats.thp
                + "\nWeapon: " + enemy.weapon.displayName
                + "\nSkill: " + (enemyHighlight?Utilities.highlight(enemy.skill.displayName):enemy.skill.displayName)
                + "\nDamage: " + enemyDmg + (enemydouble?"x2":"")
                + "\nCritical: " + enemyCritChance + "%", true);
        battleCalc.setFooter("Use `w!confirm` to confirm.", null);
        
        if(userResolve2 != 0) {       //subtract the stats gained from Resolve- SKL must be checked as Spd has the potential to be 0
            player.stats.spd -= userResolve1;
            player.stats.skl -= userResolve2;
        }
        else if(enemyResolve2 != 0) {       //subtract the stats gained from Resolve- SKL must be checked as Spd has the potential to be 0
            enemy.stats.spd -= enemyResolve1;
            enemy.stats.skl -= enemyResolve2;
        }
        return battleCalc;
    }
    
    protected EmbedBuilder battleResult() {
        String username = player.username, enemyname = enemy.username, battleText = "";
        Skill uSkill = (userSkill?player.skill:Skill.NA), eSkill = (enemySkill?enemy.skill:Skill.NA);
        boolean userCrit, enemyCrit;
        
        EmbedBuilder battleResult = new EmbedBuilder();
        battleResult.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname));
        
        //CALCULATING SKILLS - Note that Crit15, Resolve and Wrath are already incorporated
        
        //VANTAGE ROUND
        if(eSkill == Skill.Vantage) {       //then enemy cannot have Cancel
            enemyCrit = enemyCritChance > Math.random() * 100;
            battleText += "\n" + Utilities.bold(enemyname) + " attacked " + Utilities.bold(username) + "!" + (enemyCrit ? " Critical Hit!" : "")
                    + "\nDealt " + "**" + enemyDmg * (enemyCrit?3:1) + "** damage.";
            player.stats.chp -= enemyDmg * (enemyCrit?3:1);
            if (player.stats.chp < 1) {
                player.stats.chp = 0;
                battleText += getResults();
                battleResult.addField("==============================", battleText, true);
                return battleResult;
            }
        }
        
        boolean repeat = true;      //pretend Adept is on until it's not
        
        //ROUND 1
        while(repeat) {
            userCrit = userCritChance > Math.random() * 100;
            battleText = Utilities.bold(username) + " attacked " + Utilities.bold(enemyname) + "!" + (userCrit?" Critical Hit!":"")
                    + "\nDealt " + "**" + playerDmg * (userCrit?3:1) + "** damage.";
            enemy.stats.chp -= playerDmg * (userCrit?3:1);
            if(enemy.stats.chp < 1) {
                enemy.stats.chp = 0;
                battleText += getResults();
                battleResult.addField("==============================", battleText, true);
                return battleResult;
            }
            repeat = false;
            if(uSkill == Skill.Adept) {
                battleText += "\n\n" + username + "'s " + Utilities.bold("Adept") + "activated!\n";
                repeat = true;
            }
        }
        
        repeat = true;
        
        if(uSkill != Skill.Cancel) {        //can be Cancel-ed, which overrides Adept
            while(repeat) {
                enemyCrit = enemyCritChance > Math.random() * 100;
                battleText += "\n" + Utilities.bold(enemyname) + " attacked " + Utilities.bold(username) + "!" + (enemyCrit?" Critical Hit!":"")
                    + "\nDealt " + "**" + enemyDmg * (enemyCrit?3:1) + "** damage.";
                player.stats.chp -= enemyDmg * (enemyCrit?3:1);
                if(player.stats.chp < 1) {
                    player.stats.chp = 0;
                    battleText += getResults();
                    battleResult.addField("==============================", battleText, true);
                    return battleResult;
                }
                repeat = false;
                if(eSkill == Skill.Adept) {
                    battleText += "\n\n" + enemyname + "'s " + Utilities.bold("Adept") + "activated!\n";
                    repeat = true;
                }
            }
        }
        else {
            battleText += "\n\n" + Utilities.bold(username) + "'s Cancel activated!\n";
        }
        
        repeat = true;
        //ROUND 2- Skills must be re-RNG'd, but only if they double and are not Cancel-ed
        
        if(userdouble) {
            if(eSkill != Skill.Cancel && uSkill != Skill.Cancel) {        //can be Cancel-ed from previous round, but only if their counter wasn't Cancel-ed
                //chance for Skills (not Crit15, Resolve or Wrath) to activate again
                if(!player.activateSkill()) {
                    uSkill = Skill.NA;      //if player cannot re-activate their skill, turn it off
                }
                while(repeat) {
                    userCrit = userCritChance > Math.random() * 100;        //reroll crits
                    battleText += "\n" + Utilities.bold(username) + " attacked " + Utilities.bold(enemyname) + "!" + (userCrit ? " Critical Hit!" : "")
                            + "\nDealt " + "**" + playerDmg * (userCrit?3:1) + "** damage.";
                    enemy.stats.chp -= playerDmg * (userCrit?3:1);
                    if (enemy.stats.chp < 1) {
                        enemy.stats.chp = 0;
                        battleText += getResults();
                        battleResult.addField("==============================", battleText, true);
                        return battleResult;
                    }
                    repeat = false;
                    if(uSkill == Skill.Adept) {
                        battleText += "\n\n" + username + "'s " + Utilities.bold("Adept") + "activated!\n";
                        repeat = true;
                    }
                }
            }
            else {
                battleText += "\n\n" + Utilities.bold(enemyname) + "'s Cancel activated!\n";
            }
        }
        else {
            uSkill = Skill.NA;      //if no double, uSkill stops
        }
        
        repeat = true;
        
        if(enemydouble) {     
            if(uSkill != Skill.Cancel) {        //uSkill has already been updated
                while(repeat) {
                    enemyCrit = enemyCritChance > Math.random() * 100;      //reroll crits
                    battleText += "\n" + Utilities.bold(enemyname) + " attacked " + Utilities.bold(username) + "!" + (enemyCrit ? " Critical Hit!" : "")
                            + "\nDealt " + "**" + enemyDmg * (enemyCrit?3:1) + "** damage.";
                    player.stats.chp -= enemyDmg * (enemyCrit?3:1);
                    if (player.stats.chp < 1) {
                        player.stats.chp = 0;
                        battleText += getResults();
                        battleResult.addField("==============================", battleText, true);
                        return battleResult;
                    }
                    repeat = false;
                    if(eSkill == Skill.Adept) {
                        battleText += "\n\n" + enemyname + "'s " + Utilities.bold("Adept") + "activated!\n";
                        repeat = true;
                    }
                }
            }
            else {
                battleText += "\n\n" + Utilities.bold(username) + "'s Cancel activated!\n";
            }
        }
        battleText += getResults();
        battleResult.addField("==============================", battleText, true);
        return battleResult;
    }
    
    protected static String getResults() {
        String results = "\n\n" + Utilities.bold(player.username) + " has **" + player.stats.chp + "/" + player.stats.thp + " HP**!"
                + "\n" + Utilities.bold(enemy.username) + " has **" + enemy.stats.chp + "/" + enemy.stats.thp + " HP**!";
        if(player.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(enemy.username) + " is victorious!";
            enemy.stats.xp += Utilities.xpGained(enemy, player);
            if(enemy.stats.xp >= Utilities.getXpLevelUp(enemy.stats.lvl)) {
                levelup = enemy.username;
                enemy.stats.xp = 0;
            }
        }
        else if(enemy.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(player.username) + " is victorious!";
            player.stats.xp += Utilities.xpGained(player, enemy);
            
            if(enemy.username.contains("Fighter")){
                Bot.playermap.remove(enemy.username, enemy);
            }
            if(player.stats.xp >= Utilities.getXpLevelUp(player.stats.lvl)) {
                levelup = player.username;
                player.stats.xp = 0;
            }
        }
        return results;
    }
}

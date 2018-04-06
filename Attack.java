package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Attack implements Category{
    static boolean playerdouble, enemydouble, playerSkill, enemySkill, stratum;
    static int playerCritChance, enemyCritChance, playerDmg, enemyDmg, playerHitChance, enemyHitChance, pAttackSpeed, eAttackSpeed, pAttackSkill, eAttackSkill;
    static String levelup = "";
    static Player player, enemy;
    MessageChannel c;
    
    public abstract boolean isActionApplicable(String action);
    
    public abstract void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException;
    
    protected EmbedBuilder battleCalc(Player p, Player e, boolean physical) {
        player = p;
        enemy = e;
        pAttackSpeed = player.stats.spd + ((player.stats.str - player.weapon.wt < 0) ? 0 : (player.stats.str - player.weapon.wt));
        eAttackSpeed = enemy.stats.spd + ((enemy.stats.str - enemy.weapon.wt < 0) ? 0 : (enemy.stats.str - enemy.weapon.wt));
        pAttackSkill = player.stats.skl;
        eAttackSkill = enemy.stats.skl;
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
        playerCritChance = (player.stats.skl / 2) + player.weapon.crt - enemy.stats.lck;
        enemyCritChance = (enemy.stats.skl / 2) + enemy.weapon.crt - player.stats.lck;
        if(playerCritChance < 0) {
            playerCritChance = 0;
        }
        if(enemyCritChance < 0) {
            enemyCritChance = 0;
        }
        
        playerDmg = physical?player.stats.str - enemy.stats.def : player.stats.mag - enemy.stats.res + (triangle / 10);
        enemyDmg = enemyDmg = physical?enemy.stats.str - player.stats.def : enemy.stats.mag - player.stats.res - (triangle / 10);
        if(playerDmg < 0) {
            playerDmg = 0;
        }
        if(enemyDmg < 0) {
            enemyDmg = 0;
        }
        
        boolean playerHighlight = false, enemyHighlight = false;
        playerSkill = player.activateSkill();
        enemySkill = enemy.activateSkill();
        
        if(playerSkill) {
            switch (player.skill) {
                case Crit15:
                    playerCritChance += 15;
                    playerHighlight = true;
                    break;
                case Fortune:
                    enemyCritChance = 0;
                    playerHighlight = true;
                case Resolve:
                    pAttackSpeed *= 1.5;
                    pAttackSkill *= 1.5;
                    playerHighlight = true;
                    break;
                case Wrath:
                    playerCritChance += 50;
                    playerHighlight = true;
                    break;
            }
        }
        
        if(enemySkill) {
            switch (enemy.skill) {
                case Crit15:
                    enemyCritChance += 15;
                    enemyHighlight = true;
                    break;
                case Fortune:
                    enemyCritChance = 0;
                    enemyHighlight = true;
                case Resolve:
                    eAttackSpeed *= 1.5;
                    eAttackSkill *= 1.5;
                    enemyHighlight = true;
                    break;
                case Wrath:
                    enemyCritChance += 50;
                    enemyHighlight = true;
                    break;
            }
        }
        playerHitChance = (player.stats.skl * 2 + player.stats.lck + player.weapon.accuracy) - (eAttackSpeed * 2 + enemy.stats.lck) + triangle;
        enemyHitChance = (enemy.stats.skl * 2 + enemy.stats.lck + enemy.weapon.accuracy) - (pAttackSpeed * 2 + player.stats.lck) - triangle;
        if(playerHitChance < 0) 
            playerHitChance = 0;
        if(enemyHitChance < 0) 
            enemyHitChance = 0;
        playerdouble = (pAttackSpeed - eAttackSpeed > 3);
        enemydouble = (eAttackSpeed - pAttackSpeed > 3);
        
        String username = player.username, enemyname = enemy.username;
        battleCalc.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname), null);
        battleCalc.setDescription("==============================");
        battleCalc.addField(Utilities.bold(username) + " " + arrow1, "**Lv:** " + player.stats.lvl
                + "\n**HP:** " + player.stats.chp + "/" + player.stats.thp
                + "\n**Weapon:** " + player.weapon.displayName
                + "\n**Skill:** " + (playerHighlight?Utilities.highlight(player.skill.displayName):player.skill.displayName)
                + "\n**Damage:** " + playerDmg + (playerdouble?(player.weapon.displayName.contains("Brave")?"x4":"x2"):(player.weapon.displayName.contains("Brave")?"x2":""))
                + "\n**Hit:** " + playerHitChance + "%" 
                + "\n**Critical:** " + playerCritChance + "%", true);
        battleCalc.addField(Utilities.bold(enemyname) + " " + arrow2, "**Lv:** " + enemy.stats.lvl
                + "\n**HP:** " + enemy.stats.chp + "/" + enemy.stats.thp
                + "\n**Weapon:** " + enemy.weapon.displayName
                + "\n**Skill:** " + (enemyHighlight?Utilities.highlight(enemy.skill.displayName):enemy.skill.displayName)
                + "\n**Damage:** " + enemyDmg + (enemydouble?(enemy.weapon.displayName.contains("Brave")?"x4":"x2"):(enemy.weapon.displayName.contains("Brave")?"x2":""))
                + "\n**Hit:** " + enemyHitChance + "%" 
                + "\n**Critical:** " + enemyCritChance + "%", true);
        battleCalc.setFooter("Use `w!confirm` to confirm.", null);
        return battleCalc;
    }
    
    protected EmbedBuilder battleResult() {
        String username = player.username, enemyname = enemy.username, battleText = "";
        Skill uSkill = (playerSkill?player.skill:Skill.NA), eSkill = (enemySkill?enemy.skill:Skill.NA);
        LinkedList<Integer> order = new LinkedList<>();
        
        EmbedBuilder battleResult = new EmbedBuilder();
        battleResult.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname));
        
        //CALCULATING SKILLS - Note that Crit15, Fortune, Resolve and Wrath are already incorporated
        
        //CALCULATING ORDER
        if(eSkill == Skill.Vantage) {
            order.add(checkHit(enemyHitChance)?-1:-2);
        }
        order.add(checkHit(playerHitChance)?1:2);
        if(uSkill == Skill.Adept || player.weapon.displayName.contains("Brave")) {
            order.add(checkHit(playerHitChance)?1:2);
        }
        if(uSkill != Skill.Cancel) {
            order.add(checkHit(enemyHitChance)?-1:-2);
            if(eSkill == Skill.Adept || enemy.weapon.displayName.contains("Brave")) {
                order.add(checkHit(enemyHitChance)?-1:-2);
            }
        }
        //ROUND 2- Skills must be re-RNG'd, but only if they double and are not Cancel-ed
        if(playerdouble && (eSkill != Skill.Cancel || order.get(order.size() - 1) == 1)) {     //if true, user activated Cancel on enemy and did not miss
            if(!player.activateSkill()) {
                    uSkill = Skill.NA;      //if player cannot re-activate their skill, turn it off
                }
            order.add(checkHit(playerHitChance)?1:2);
            if(uSkill == Skill.Adept || player.weapon.displayName.contains("Brave")) {
                order.add(checkHit(playerHitChance)?1:2);
            }
        }
        if(enemydouble && (uSkill != Skill.Cancel || order.get(order.size() - 1) == -1)) {    //if true, enemy activated Cancel on user's double and did not miss   
            if(!enemy.activateSkill()) {
                    eSkill = Skill.NA;      //if player cannot re-activate their skill, turn it off
                }
            order.add(checkHit(enemyHitChance)?-1:-2);
            if(eSkill == Skill.Adept || enemy.weapon.displayName.contains("Brave")) {
                order.add(checkHit(enemyHitChance)?-1:-2);
            }
        }
        
        Skill skill;
        for(int i = 0; i < order.size(); i++) {
            skill = Skill.NA;
            if(null != order.get(i)) switch (order.get(i)) {
                case 1:
                    if(i > 0 && order.get(i - 1) == 1 && !player.weapon.displayName.contains("Brave")) {     //user just attacked, and it wasn't because of Brave
                        skill = player.skill;
                    }
                    if(i > 0 && order.get(i - 1) == -1) {       //if it was just enemy phase
                        battleText += "\n";
                    }
                    battleText = playerAttack(battleText, username, enemyname, skill);
                    break;
                case 2:
                    battleText += "\n\n" + username + " missed!\n";
                    break;
                case -1:
                    if(i > 0 && order.get(i - 1) == -1 && !enemy.weapon.displayName.contains("Brave")) {     //enemy just attacked, and it wasn't because of Brave
                        skill = enemy.skill;
                    }   
                    if(i > 0 && order.get(i - 1) == 1) {       //if it was just player phase
                        battleText += "\n";
                    }
                    battleText = enemyAttack(battleText, enemyname, username, skill);
                    break;
                case -2:
                    battleText += "\n\n" + enemyname + " missed!\n";
                    break;
                default:
                    break;
            }
            if(checkDeath(battleText, battleResult)) {
                return battleResult;
            }
        }
        
        battleText += getResults();
        battleResult.addField("==============================", battleText, true);
        return battleResult;
    }
    
    private boolean checkHit(int hitChance) {
        return Math.random() * 100 < hitChance;
    }

    private boolean checkDeath(String battleText, EmbedBuilder battleResult) {
        if (player.stats.chp < 1) {
            player.stats.chp = 0;
            battleText += getResults();
            battleResult.addField("==============================", battleText, true);
            return true;
        }
        else if (enemy.stats.chp < 1) {
            enemy.stats.chp = 0;
            battleText += getResults();
            battleResult.addField("==============================", battleText, true);
            return true;
        }
        else {
            return false;
        }
    }

    private String playerAttack(String battleText, String username, String enemyname, Skill skill) {
        boolean userCrit;
        userCrit = playerCritChance > Math.random() * 100;        //roll crits
        if(enemy.activateSkill() && enemy.skill == Skill.Pavise) {
            playerDmg = 0;
            battleText += "\n\n" + enemy.username + "'s Pavise activated!\n";
        }
        if(skill != Skill.NA) {     //some skill activated
            battleText += "\n\n" + username + "'s " + Utilities.bold(skill.displayName) + " activated!\n";
        }
        battleText += "\n" + Utilities.bold(username) + " attacked " + Utilities.bold(enemyname) + "!" + (userCrit ? " Critical Hit!" : "")
                + "\nDealt " + "**" + playerDmg * (userCrit?3:1) + "** damage.";
        enemy.stats.chp -= playerDmg * (userCrit?3:1);
        return battleText;
    }

    private String enemyAttack(String battleText, String enemyname, String username, Skill skill) {
        boolean enemyCrit;
        enemyCrit = enemyCritChance > Math.random() * 100;
        if(player.activateSkill() && player.skill == Skill.Pavise) {
            enemyDmg = 0;
            battleText += "\n\n" + player.username + "'s Pavise activated!\n";
        }
        if(skill != Skill.NA) {     //some skill activated
            battleText += "\n\n" + enemyname + "'s " + Utilities.bold(skill.displayName) + " activated!\n";
        }
        battleText += "\n" + Utilities.bold(enemyname) + " attacked " + Utilities.bold(username) + "!" + (enemyCrit ? " Critical Hit!" : "")
                + "\nDealt " + "**" + enemyDmg * (enemyCrit?3:1) + "** damage.";
        player.stats.chp -= enemyDmg * (enemyCrit?3:1);
        return battleText;
    }
    
    protected static String getResults() {
        String results = "\n\n" + Utilities.bold(player.username) + " has **" + player.stats.chp + "/" + player.stats.thp + " HP**!"
                + "\n" + Utilities.bold(enemy.username) + " has **" + enemy.stats.chp + "/" + enemy.stats.thp + " HP**!";
        if(player.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(enemy.username) + " is victorious!";
            enemy.stats.xp += Utilities.xpGained(enemy, player) * (enemy.skill == Skill.Paragon?2:1);
            if(enemy.stats.xp >= Utilities.getXpLevelUp(enemy.stats.lvl) && enemy.stats.lvl < 40) {
                levelup = enemy.username;
                enemy.stats.xp = 0;
            }
        }
        else if(enemy.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(player.username) + " is victorious!";
            player.stats.xp += Utilities.xpGained(player, enemy) * (player.skill == Skill.Paragon?2:1);
            
            if(enemy.username.contains("Fighter")){
                Bot.playermap.remove(enemy.username, enemy);
            }
            if(player.stats.xp >= Utilities.getXpLevelUp(player.stats.lvl) && player.stats.lvl < 40) {
                levelup = player.username;
                player.stats.xp = 0;
            }
        }
        return results;
    }
}

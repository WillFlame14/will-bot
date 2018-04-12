package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Attack implements Category{
    static boolean playerdouble, enemydouble, playerSkill, enemySkill, stratum, defaultuser, phys, bossAttack = false, defeatBoss = false;
    static int playerCritChance = 0, enemyCritChance = 0, playerDmg, enemyDmg, playerHitChance, enemyHitChance, pAttackSpeed, eAttackSpeed, pAttackSkill, eAttackSkill;
    static String levelup = "", weaponup = "";
    static Player player, enemy;
    MessageChannel c;
    
    public abstract boolean isActionApplicable(String action);
    
    public abstract void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException;
    
    protected EmbedBuilder battleCalc(Player p, Player e, boolean physical, boolean playerPhase) {
        player = p;
        enemy = e;
        phys = physical;
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
        
        playerDmg = (physical?player.stats.str - enemy.stats.def : player.stats.mag - enemy.stats.res) + (triangle / 10);
        enemyDmg = (physical?enemy.stats.str - player.stats.def : enemy.stats.mag - player.stats.res) - (triangle / 10);
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
                    break;
                case Gamble:
                    playerHitChance /= 2;
                    playerCritChance *= 2;
                    break;
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
                    break;
                case Gamble:
                    playerHitChance /= 2;
                    playerCritChance *= 2;
                    break;
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
        playerHitChance += (player.stats.skl * 2 + player.stats.lck + player.weapon.accuracy) - (eAttackSpeed * 2 + enemy.stats.lck) + triangle;
        enemyHitChance += (enemy.stats.skl * 2 + enemy.stats.lck + enemy.weapon.accuracy) - (pAttackSpeed * 2 + player.stats.lck) - triangle;
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
        Bot.attacksaves.put(player.username + " " + enemy.username, new AttackSave(playerdouble, enemydouble, playerSkill, enemySkill, phys, 
                playerCritChance, enemyCritChance, playerDmg, enemyDmg, playerHitChance, enemyHitChance, pAttackSpeed, eAttackSpeed, pAttackSkill, eAttackSkill, 
                player, enemy));
        if(playerPhase) {
            Bot.attackusers.put(player.authorid, player.username + " " + enemy.username);
        }
        else {
            Bot.enemyattackusers.put(player.authorid, player.username + " " + enemy.username);
        }
        return battleCalc;
    }
    
    protected EmbedBuilder battleResult(long id, boolean playerPhase) {
        AttackSave as;
        if(playerPhase) {
            as = Bot.attacksaves.get(Bot.attackusers.get(id));
        }
        else {
            as = Bot.attacksaves.get(Bot.enemyattackusers.get(id));
        }
        playerdouble = as.playerdouble;
        enemydouble = as.enemydouble;
        playerSkill = as.playerSkill;
        enemySkill = as.enemySkill;
        phys = as.phys;
        playerCritChance = as.playerCritChance; 
        enemyCritChance = as.enemyCritChance;
        playerDmg = as.playerDmg;
        enemyDmg = as.enemyDmg;
        playerHitChance = as.playerHitChance;
        enemyHitChance = as.enemyHitChance;
        pAttackSpeed = as.pAttackSpeed;
        eAttackSpeed = as.eAttackSpeed;
        pAttackSkill = as.pAttackSkill;
        eAttackSkill = as.eAttackSkill;
        player = as.player;
        enemy = as.enemy;
        Bot.enemySave.remove(player + " " + enemy);
        String username = player.username, enemyname = enemy.username, battleText = "";
        Skill eSkill = (enemySkill?enemy.skill:Skill.NA);
        LinkedList<Integer> order = new LinkedList<>();
        
        EmbedBuilder battleResult = new EmbedBuilder();
        battleResult.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname));
        
        //CALCULATING SKILLS - Note that Crit15, Fortune, Resolve and Wrath are already incorporated
        
        //CALCULATING ORDER - 1 is normal attack, 2 is miss
        if(eSkill == Skill.Vantage) {       //let's just use the already calculated activateSkill().
            order.add(checkHit(enemyHitChance)?-1:-2);
        }
        
        order.add(checkHit(playerHitChance)?1:2);       //player phase
        if(player.weapon.displayName.contains("Brave")) {
            order.add(checkHit(playerHitChance)?1:2);
        }
        order.add(checkHit(enemyHitChance)?-1:-2);      //enemy phase
        if(enemy.weapon.displayName.contains("Brave")) {
            order.add(checkHit(enemyHitChance)?-1:-2);
        }
        order.add(0);   //change of phase
        //ROUND 2- Skills must be re-RNG'd, but only if they double and are not Cancel-ed
        if(playerdouble) {  
            order.add(checkHit(playerHitChance)?1:2);
            if(player.weapon.displayName.contains("Brave")) {
                order.add(checkHit(playerHitChance)?1:2);
            }
        }
        if(enemydouble) {
            order.add(checkHit(enemyHitChance)?-1:-2);
            if(enemy.weapon.displayName.contains("Brave")) {
                order.add(checkHit(enemyHitChance)?-1:-2);
            }
        }
        
        boolean playercancel = false, enemycancel = false, stopper = false;
        for(int i = 0; i < order.size(); i++) {     //all misses are already accounted for
            Skill skill = Skill.NA;
            if(null != order.get(i)) {
                switch (order.get(i)) {
                    case 1:
                        if(enemycancel) {
                            if(!player.weapon.displayName.contains("Brave") || stopper) {
                                enemycancel = false;
                                stopper = false;
                            }
                            else {
                                stopper = true;
                            }
                            break;
                        }
                        skill = player.activateSkill()?player.skill:Skill.NA;
                        battleText = performAttack(battleText, skill, true);
                        giveWeaponExp(true);
                        break;
                    case 2:
                        battleText += "\n\n" + username + " missed!\n";
                        break;
                    case 0:
                        battleText += "\n";
                        break;
                    case -1:
                        if(playercancel) {
                            if(!enemy.weapon.displayName.contains("Brave") || stopper) {
                                enemycancel = false;
                                stopper = false;
                            }
                            else {
                                stopper = true;
                            }
                            break;
                        }
                        skill = enemy.activateSkill()?enemy.skill:Skill.NA;
                        battleText = performAttack(battleText, skill, false);
                        giveWeaponExp(false);
                        break;
                    case -2:
                        battleText += "\n\n" + enemyname + " missed!\n";
                        break;
                    default:
                        break;
                }
            }
            switch(skill) {
                case Adept:
                    i--;
                    break;
                case Cancel:
                    if(order.get(i) == 1) {
                        playercancel = true;
                    }
                    else {
                        enemycancel = true;
                    }
                    break;
            }
            if(stopper) {
                stopper = false;
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

    private String performAttack(String battleText, Skill skill, boolean playerAtk) {
        boolean crit;
        int critChance = playerAtk?playerCritChance:enemyCritChance, damage = playerAtk?playerDmg:enemyDmg;
        Player p = playerAtk?player:enemy;
        Player e = (!playerAtk)?player:enemy;
        crit = critChance > Math.random() * 100;        //roll crits
        
        if(e.activateSkill() && e.skill == Skill.Pavise) {
            damage = 0;
            battleText += "\n" + e.username + "'s Pavise activated!\n";
        }
        if(skill == Skill.Luna) {
            damage += (phys?e.stats.def:e.stats.res) / 2;
        }
        if(crit) {
            damage *= 3;
        }
        if(skill != Skill.NA && (skill == Skill.Adept || skill == Skill.Cancel || skill == Skill.Vantage || skill == Skill.Luna || skill == Skill.Sol)) {     //some skill activated
            battleText += "\n" + p.username + "'s " + Utilities.bold(skill.displayName) + " activated!\n";
        }
        
        battleText += "\n" + Utilities.bold(p.username) + " attacked " + Utilities.bold(e.username) + "!" + (crit ? " Critical Hit!" : "")
                + "\nDealt " + "**" + damage + "** damage.\n";
        
        if(skill == Skill.Sol) {
            battleText += "\n" + Utilities.bold(p.username) + " regained " + (damage / 2) + " HP.\n";
            p.stats.chp += damage / 2;
            if(p.stats.chp > p.stats.thp) {
                p.stats.chp = p.stats.thp;
            }
        }
        e.stats.chp -= damage;
        return battleText;
    }
    
    protected static String getResults() {
        String results = "\n\n" + Utilities.bold(player.username) + " has **" + player.stats.chp + "/" + player.stats.thp + " HP**!"
                + "\n" + Utilities.bold(enemy.username) + " has **" + enemy.stats.chp + "/" + enemy.stats.thp + " HP**!";
        if(player.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(enemy.username) + " is victorious!";
            if(player.authorid < 0) {
                enemy.stats.xp += Utilities.xpGained(enemy, player) * (enemy.skill == Skill.Paragon?2:1) * (enemy.skill == Skill.Blossom?0.5:1);
            }
            if(player.username.contains("Fighter")) {       //if during enemy phase
                Bot.playermap.remove(player.username, player);
                Bot.idmap.remove(player.username);
            }
            Player pSave = player;
            try {
                player = (Boss)player;
                int id = ((Boss)player).bossid;
                boolean complete = true;
                for(int i = 0; i < BossBattle.getSize(id); i++) {
                    if(BossBattle.rooms.get(id).get(i).stats.chp > 0) {
                        complete = false;
                    }
                }
                if(complete) {
                    defeatBoss = true;
                }
            }
            catch(ClassCastException cce) {
                player = pSave;
            }
            if(enemy.stats.xp >= Utilities.getXpLevelUp(enemy.stats.lvl) && enemy.stats.lvl < 40) {
                levelup = enemy.username;
                enemy.stats.xp = 0;
            }
        }
        else if(enemy.stats.chp < 1) {
            results += "\n\n" + Utilities.bold(player.username) + " is victorious!";
            if(enemy.authorid > 0) {
                player.stats.xp += Utilities.xpGained(player, enemy) * (player.skill == Skill.Paragon?2:1) * (player.skill == Skill.Blossom?0.5:1);
            }
            
            if(enemy.username.contains("Fighter")) {
                Bot.playermap.remove(enemy.username, enemy);
                Bot.idmap.remove(enemy.username);
            }
            Player eSave = enemy;
            try {
                enemy = (Boss)enemy;
                int id = ((Boss)enemy).bossid;
                boolean complete = true;
                for(int i = 0; i < BossBattle.getSize(id); i++) {
                    if(BossBattle.rooms.get(id).get(i).stats.chp > 0) {
                        complete = false;
                    }
                }
                if(complete) {
                    defeatBoss = true;
                }
            }
            catch(ClassCastException e) {
                enemy = eSave;
            }
            if(player.stats.xp >= Utilities.getXpLevelUp(player.stats.lvl) && player.stats.lvl < 40) {
                levelup = player.username;
                player.stats.xp = 0;
            }
        }
        
        int[] pranks = player.ranks.toArray();      //xp starts at index 7
        int[] eranks = enemy.ranks.toArray();
        for(int i = 0; i < 7; i++) {
            if(pranks[i + 7] > WeaponRanks.getXpLevelUp(pranks[i])) {
                pranks[i]++;
                pranks[i + 7] = 0;
                weaponup += player.username;
            }
            if(eranks[i + 7] > WeaponRanks.getXpLevelUp(eranks[i])) {
                eranks[i]++;
                eranks[i + 7] = 0;
                weaponup += " " + enemy.username;
            }
        }
        player.ranks = new WeaponRanks(pranks);
        enemy.ranks = new WeaponRanks(eranks);
        return results;
    }
    
    private void giveWeaponExp(boolean playerAtk) {
        Player p = playerAtk ? player:enemy;
        if(p.authorid < 0) {
            return;
        }
        int exp = p.weapon.wex * (p.skill == Skill.Discipline?2:1);
        switch(p.weapon.colour) {
            case RED:
                p.ranks.swordx += exp;
                break;
            case GREEN:
                p.ranks.axex += exp;
                break;
            case BLUE:
                p.ranks.lancex += exp;
                break;
            default:
                if(p.weapon.staff) {
                    p.ranks.staffx += exp;
                }
                else if(p.weapon.displayName.toLowerCase().contains("fire")) {
                    p.ranks.firex += exp;
                }
                else if(p.weapon.displayName.toLowerCase().contains("wind")) {
                    p.ranks.firex += exp;
                }
                else if(p.weapon.displayName.toLowerCase().contains("thunder")) {
                    p.ranks.firex += exp;
                }
        }
    }
}

class AttackSave {
    boolean playerdouble, enemydouble, playerSkill, enemySkill, phys;
    int playerCritChance, enemyCritChance, playerDmg, enemyDmg, playerHitChance, enemyHitChance, pAttackSpeed, eAttackSpeed, pAttackSkill, eAttackSkill;
    Player player, enemy;
    
    public AttackSave(boolean pd, boolean ed, boolean ps, boolean es, boolean ph, int pcc, int ecc, int pdmg, int edmg, int phc, int ehc, int paspd, int easpd, int paskl, int easkl, Player p, Player e) {
        playerdouble = pd;
        enemydouble = ed;
        playerSkill = ps;
        enemySkill = es;
        phys = ph;
        playerCritChance = pcc; 
        enemyCritChance = ecc;
        playerDmg = pdmg;
        enemyDmg = edmg;
        playerHitChance = phc;
        enemyHitChance = ehc;
        pAttackSpeed = paspd;
        eAttackSpeed = easpd;
        pAttackSkill = paskl;
        eAttackSkill = easkl;
        player = p;
        enemy = e;
    }
}


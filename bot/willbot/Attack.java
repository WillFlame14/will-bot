package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Attack implements Category{
    static boolean playerdouble, enemydouble, playerSkill, enemySkill, stratum, defaultuser, phys, bossAttack = false, defeatBoss = false, failure = false;
    static int playerCritChance = 0, enemyCritChance = 0, playerDmg, enemyDmg, playerHitChance, enemyHitChance, pAttackSpeed, eAttackSpeed, pAttackSkill, eAttackSkill;
    static String levelup = "", weaponup = "";
    static ArrayList<Player> playerRemove = new ArrayList<>();
    static HashMap<Long, String> heals = new HashMap<>();
    static Player player, enemy;
    static MessageChannel c;
    
    public abstract boolean isActionApplicable(String action);
    
    public abstract void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException;
    
    protected static EmbedBuilder battleCalc(Player p, Player e, boolean physical, boolean playerPhase) {
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
    
    protected static EmbedBuilder battleResult(long id, boolean playerPhase) {
        //set all the local variables to the save
        assignSavedValues(playerPhase?Bot.attacksaves.get(Bot.attackusers.get(id)):Bot.attacksaves.get(Bot.enemyattackusers.get(id))); 
        Bot.enemySave.remove(player + " " + enemy);     //remove this save from the list
        
        String username = player.username, enemyname = enemy.username, battleText = "";
        Skill eSkill = (enemySkill?enemy.skill:Skill.NA);
        LinkedList<Integer> order = new LinkedList<>();     //order of attacks
        EmbedBuilder battleResult = new EmbedBuilder();
        battleResult.setTitle(Utilities.bold(username) + " vs. " + Utilities.bold(enemyname));
        
        //CALCULATING SKILLS - Note that Crit15, Fortune, Resolve and Wrath are already incorporated
        
        //CALCULATING ORDER - 1 is normal attack, 2 is miss. Negative numbers are for the opponent.
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
        
        boolean playercancel = false, enemycancel = false, stopper = false, adept = false;
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
                        adept = false;
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
                    if(adept) {     //this is an Adept attack - prevents Adept from activating on itself
                        adept = false;
                        break;
                    }
                    battleText += "\n" + ((order.get(i) > 0)?player.username:enemy.username) + "'s **Adept** activated!\n";     //this has to be here and not in performAttack() so it doesn't unnecessarily show up
                    i--;        //allows them to perform the same attack
                    adept = true;
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

    private static void assignSavedValues(AttackSave as) {
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
    }
    
    private static boolean checkHit(int hitChance) {
        return Math.random() * 100 < hitChance;
    }

    private static boolean checkDeath(String battleText, EmbedBuilder battleResult) {
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

    private static String performAttack(String battleText, Skill skill, boolean playerAtk) {
        int critChance = playerAtk?playerCritChance:enemyCritChance, damage = playerAtk?playerDmg:enemyDmg;     //figure out the attack stats based on who is currently attacking
        Player p = playerAtk?player:enemy;
        Player e = (!playerAtk)?player:enemy;
        boolean crit = critChance > Math.random() * 100;        //roll crits
        
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
        if(skill != Skill.NA && (skill == Skill.Cancel || skill == Skill.Vantage || skill == Skill.Luna || skill == Skill.Sol)) {     //some skill activated
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
        
        //IF COMBAT ENDS
        if(player.stats.chp < 1 || enemy.stats.chp < 1) {
            Player dead = (player.stats.chp < 1)?player:enemy;     
            Player survived = (player.stats.chp < 1)?enemy:player;
            results += "\n\n" + Utilities.bold(survived.username) + " is victorious!";
            if(dead.authorid < 0) {
                survived.stats.xp += Utilities.xpGained(survived, dead) * (survived.skill == Skill.Paragon?2:1) * (survived.skill == Skill.Blossom?0.5:1);
            }
            if(dead.username.contains("Fighter")) {       
                Bot.playermap.remove(dead.username, dead);
                Bot.idmap.remove(dead.username);
            }
            
            Player pSave = dead;
            try {
                dead = (Boss)dead;      //A boss died.
                int id = ((Boss)dead).bossid;
                boolean complete = true;
                for(int i = 0; i < BossBattle.getSize(id); i++) {
                    if(BossBattle.rooms.get(id).get(i).stats.chp > 0) {
                        complete = false;
                    }
                }
                if(complete) {
                    defeatBoss = true;
                }
                if(!BossBattle.battles.get(id).removePlayer(dead)) {        //try to remove them from the map
                    System.out.println("Failed to remove " + dead.username + ". Symbol was " + BossBattle.battles.get(id).playerValues.get(dead));
                }
                BossBattle.deadBosses.get(id).add(dead);
            }
            catch(ClassCastException cce) {
                dead = pSave;
            }
            
            if(BossBattle.inboss.contains(dead)) {       //person who died was in a boss battle
                int id = BossBattle.toBoss.get(dead);
                playerRemove.add(dead);
                BossBattle.rooms.get(id).remove(dead);
                BossBattle.toBoss.remove(dead);
                BossBattle.inboss.remove(dead);
                failure = true;
                for(Player p: BossBattle.rooms.get(id)) {     //all players are defeated?
                    if(p.authorid > 0) {        //there is still a player alive
                        failure = false;
                    }
                }
            }
            
            if(survived.stats.xp >= Utilities.getXpLevelUp(survived.stats.lvl) && survived.stats.lvl < 40) {
                levelup = survived.username;
                survived.stats.xp = 0;
            }
        }
        
        //IF COMBAT DOES NOT END
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
                weaponup += enemy.username;
            }
        }
        player.ranks = new WeaponRanks(pranks);
        enemy.ranks = new WeaponRanks(eranks);
        return results;
    }
    
    private static void giveWeaponExp(boolean playerAtk) {
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

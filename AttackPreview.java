package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AttackPreview extends Attack{
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!attack");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        c = event.getChannel();
        Player defaultUser = null, opponent = null;
        bossAttack = false;
        stratum = false;
        if(args.size() == 1 && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
            defaultUser = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
            args.add(0, defaultUser.username);      //insert username so it becomes properly formatted
        }
        if(args.size() > 1) {       //w!attack <player> <enemy>
            if(!Bot.playermap.containsKey(args.get(0)) || !Bot.playermap.containsKey(args.get(1))) {
                if(args.get(1).startsWith("stratum")) {
                    try {
                        opponent = null;
                        int level = Integer.parseInt(args.get(1).substring(7));
                        opponent = Utilities.generateStratumOpponent(level);
                        stratum = true;
                    }
                    catch(Exception e) {
                        throw new ValidationException("The stratum level was not recognized.");
                    }
                }
                else {
                    throw new ValidationException("You did not specify valid usernames.");
                }
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            Player p = defaultuser?defaultUser:Bot.playermap.get(args.get(0));
            Player e = stratum?opponent:Bot.playermap.get(args.get(1));
            
            if(p.stats.chp == 0) {
                throw new ValidationException(p.username + " is currently at the brink of death. Please restore HP before continuining.");
            }
            if(p == e) {
                throw new ValidationException("You cannot attack yourself.");
            }
            
            Player eSave = e;
            try {
                e = (Boss)e;
            }
            catch(ClassCastException cce) {
                e = eSave;
            }
            if(!BossBattle.inboss.contains(p) && BossBattle.inboss.contains(e)) {       //outsider attempts to attack inboss
                throw new ValidationException(p.username + " is currently in a boss battle.");
            }
            if(BossBattle.inboss.contains(p) && e instanceof Boss && BossBattle.turnover.get(((Boss)e).bossid).contains(p)) {
                throw new ValidationException("You have already taken your turn.");
            }
            if((BossBattle.inboss.contains(p) && !(e instanceof Boss)) || (!BossBattle.inboss.contains(p) && e instanceof Boss)) {
                throw new ValidationException("You can only attack bosses in boss battles.");
            }
            if((BossBattle.inboss.contains(p) && BossBattle.deadBosses.get(BossBattle.toBoss.get(p)).contains(e))) {
                throw new ValidationException("That boss has already been defeated.");
            }
            if(BossBattle.battles.get(BossBattle.toBoss.get(p)).getDistance(p, e) != p.weapon.range) {
                throw new ValidationException("That unit is not in range.");
            }
            
            c.sendMessage(battleCalc(p, e, p.weapon.physical, true).build()).queue();
            if(stratum) {
                battleCalc(e, p, p.weapon.physical, false);       //instantiate the EmbedBuilder for enemy phase
            }
            Bot.enemySave.put(p.username + " " + e.username, new Players(p.duplicate(), e.duplicate()));
        }
        else {
            c.sendMessage("You did not specify enough arguments. The correct usage is `w!attack <player> <enemy>`").queue();
            return;
        }
        Bot.calculated.replace(event.getAuthor().getIdLong(), true);
    }

}

class AttackConfirm extends Attack{
    public boolean isActionApplicable(String action) {
        return action.equals("w!confirm");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        c = event.getChannel();
        if(Bot.calculated.get(event.getAuthor().getIdLong())) {
            String[] currentPlayers = Bot.attackusers.get(event.getAuthor().getIdLong()).split(" ");
            if(!Bot.enemySave.get(Bot.attackusers.get(event.getAuthor().getIdLong())).player.checkSame(Bot.playermap.get(currentPlayers[0]))
             || !Bot.enemySave.get(Bot.attackusers.get(event.getAuthor().getIdLong())).enemy.checkSame(Bot.playermap.get(currentPlayers[1]))) {
                throw new ValidationException("Your interaction with the opponent has changed since your calculation. Please re-calculate.");
            }
            c.sendMessage(battleResult(event.getAuthor().getIdLong(), true).build()).queue();
            checkLevelUps(c);
            String[] players = Bot.attackusers.get(event.getAuthor().getIdLong()).split(" ");
            Player pl = Bot.playermap.get(players[0]);
            if(Bot.playermap.containsKey(players[1]) && pl.stats.chp > 0) {    //stratum opponent did not die, player did not die
                Player en = Bot.playermap.get(players[1]);
                if(en.username.contains("Fighter")) {      //only stratum opponents have their own enemy turn
                    c.sendMessage(battleResult(en.authorid, false).build()).queue();
                    checkLevelUps(c);
                }
                if(defeatBoss) {
                    bossDefeated(((Boss)en).bossid, c);
                }
                else {
                    if(BossBattle.inboss.contains(pl)) {
                        BossBattle.turnover.get(((Boss)en).bossid).add(pl);
                    }
                    checkPlayerDeath(c);
                    if(en instanceof Boss && BossBattle.turnover.get(((Boss)en).bossid).size() == BossBattle.getSize(((Boss)en).bossid)) {
                        BossBattle.bossAttack(((Boss)en).bossid);
                        bossEnemyPhase(((Boss)en).bossid, c);
                        if(!defeatBoss) {       //once boss is defeated, the map doesn't exist anymore
                            c.sendMessage(BossBattle.battles.get(BossBattle.toBoss.get(pl)).toString()).queue();        //send the updated map
                        }
                        defeatBoss = false;
                    }
                    if(failure) {
                        int id = ((Boss)en).bossid;
                        for(int i = 0; i < BossBattle.getSize(id); i++) {
                            BossBattle.rooms.get(id).get(i).stats.chp = BossBattle.rooms.get(id).get(i).stats.thp;     //refill HP on all bosses
                            c.sendMessage("The team was defeated. Better luck next time...").queue();
                            BossBattle.turnover.remove(id);
                            BossBattle.moveover.remove(id);
                            BossBattle.rooms.remove(id);
                            BossBattle.battles.remove(id);
                            BossBattle.bossMove.remove(id);
                            BossBattle.deadBosses.remove(id);
                            failure = false;
                        }
                    }
                }
            }
            Bot.calculated.replace(event.getAuthor().getIdLong(), false);
            Bot.update();
        }
        else {
            throw new ValidationException("Your command was not recognized. Please try again.");
        }
    }

    public static void bossEnemyPhase(int id, MessageChannel c) {
        c.sendMessage("==========================\n**ENEMY PHASE**\n==========================").queue();
        int size = BossBattle.getSize(id);
        ArrayList<Player> characters = BossBattle.rooms.get(id);
        for(int i = 0; i < size; i++) {     //only go through the bosses
            Boss b = (Boss)characters.get(i);
            if(BossBattle.deadBosses.get(b.bossid).contains(b)) {       //this boss is dead, so they don't take a turn
                continue;
            }
            if(!Bot.attackusers.containsKey(b.authorid)) {      //they healed or only moved
                if(heals.containsKey(b.authorid)) {
                    c.sendMessage(heals.get(b.authorid)).queue();
                }
                else if(BossBattle.bossMove.get(b.bossid).contains(b)) {
                    BossBattle.bossMove.get(b.bossid).remove(b);        //remove them from the moved list
                }
                else {
                    c.sendMessage("Something went wrong. Please notify WillFlame.").queue();
                }
                continue;   //they don't have a battleResult
            }
            c.sendMessage(battleResult(b.authorid, true).build()).queue();
            checkLevelUps(c);
        }
        BossBattle.turnover.get(id).clear();        //clear all turns
        BossBattle.moveover.get(id).clear();        //clear all moves
        if(defeatBoss) {
            bossDefeated(id, c);
        }
        checkPlayerDeath(c);
    }

    private static void bossDefeated(int id, MessageChannel c) {
        String pcs = "";
        for(int i = BossBattle.getSize(id); i < BossBattle.getSize(id) * 2; i++) {      //cycles through players
            BossBattle.rooms.get(id).get(i - BossBattle.getSize(id)).stats.chp = BossBattle.rooms.get(id).get(i - BossBattle.getSize(id)).stats.thp;     //refill HP on all bosses
            BossBattle.inboss.remove(BossBattle.rooms.get(id).get(i));
            BossBattle.toBoss.remove(BossBattle.rooms.get(id).get(i));
            pcs += Utilities.bold(BossBattle.rooms.get(id).get(i).username) + ", ";
            BossBattle.rooms.get(id).get(i).weapon = BossBattle.getReward(id);
        }
        c.sendMessage("Congratulations, " + pcs + "for defeating " + Utilities.bold(BossBattle.getName(id)) + ". **Reward:** " + BossBattle.getReward(id).displayName).queue();
        
        BossBattle.turnover.remove(id);
        BossBattle.moveover.remove(id);
        BossBattle.rooms.remove(id);
        BossBattle.battles.remove(id);
        BossBattle.bossMove.remove(id);
        BossBattle.deadBosses.remove(id);
    }

    private static void checkLevelUps(MessageChannel c) {
        if(!levelup.equals("")) {
            Player p = Bot.playermap.get(levelup);
            p.levelup();
            c.sendMessage("\n⏫ " + Utilities.bold(levelup) + " has leveled up!\n").queue();
            c.sendMessage(p.showStats().build()).queue();
            Player.clearLevelUp();
            levelup = "";
        }
        if(!weaponup.equals("")) {
            String[] names = weaponup.split(" ");
            for(String s: names) {
                Player p = Bot.playermap.get(s);
                c.sendMessage("\n⏫ " + Utilities.bold(s) + "'s weapon rank increased!\n").queue();
            }
            weaponup = "";
        }
    }
    
    private static void checkPlayerDeath(MessageChannel c) {
        for(Player p: playerRemove) {
            c.sendMessage(p.username + " has fainted and has been removed from the room.").queue();
        }
        playerRemove.clear();
    }
}

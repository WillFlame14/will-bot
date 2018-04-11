package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AttackPreview extends Attack{
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!attackp") || action.equals("w!attackm");
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
        if(args.size() > 1) {       //w!attackp <player> <enemy>
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
            
            c.sendMessage(battleCalc(p, e, action.equals("w!attackp"), true).build()).queue();
            if(stratum) {
                battleCalc(e, p, action.equals("w!attackp"), false);       //instantiate the EmbedBuilder for enemy phase
            }
            if((e instanceof Boss) && BossBattle.turnover.get(((Boss)e).bossid).size() == BossBattle.getSize(((Boss)e).bossid) - 1) {  //time for boss retalization - just calculating
                int id = ((Boss)e).bossid, size = BossBattle.getSize(id);
                ArrayList<Player> players = BossBattle.rooms.get(id);
                for(int i = 0; i < size; i++) {     //only go through the bosses
                    Player toAttack = players.get(size);        //default, attack the first person
                    int highestrating = Integer.MIN_VALUE;
                    boolean bossphysical = players.get(i).weapon.physical;
                    for(int j = size; j < size * 2; j++) {      //only go through the players
                        int currentrating = 0;
                        boolean playerphysical = players.get(j).weapon.physical;
                        int dmg = (bossphysical?players.get(i).stats.str : players.get(i).stats.mag) - (bossphysical? players.get(j).stats.def : players.get(j).stats.res);
                        currentrating += (dmg > players.get(j).stats.chp)?players.get(j).stats.chp:dmg;
                        int dmgTaken = (playerphysical? players.get(j).stats.str : players.get(j).stats.mag) - (playerphysical? players.get(i).stats.def : players.get(i).stats.res);
                        currentrating += (dmgTaken > players.get(i).stats.chp)?0:(players.get(i).stats.chp - dmgTaken);
                        
                        if(currentrating > highestrating) {
                            toAttack = players.get(j);
                        }
                    }
                    battleCalc(players.get(i), toAttack, players.get(i).weapon.physical, true);
                }
                bossAttack = true;
            }
            Bot.enemySave.put(p.username + " " + e.username, new Players(p.duplicate(), e.duplicate()));
        }
        else {
            c.sendMessage("You did not specify enough arguments. The correct usage is `" + action + " <player> <enemy>`").queue();
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
            checkLevelUps();
            String[] players = Bot.attackusers.get(event.getAuthor().getIdLong()).split(" ");
            Player pl = Bot.playermap.get(players[0]);
            if(Bot.playermap.containsKey(players[1]) && pl.stats.chp > 0) {    //stratum opponent did not die, player did not die
                Player en = Bot.playermap.get(players[1]);
                if(en.username.contains("Fighter")) {      //only stratum opponents have their own enemy turn
                    c.sendMessage(battleResult(en.authorid, false).build()).queue();
                    checkLevelUps();
                }
                if(defeatBoss) {
                    bossDefeated(((Boss)en).bossid);
                }
                if(BossBattle.inboss.contains(pl)) {
                    BossBattle.turnover.get(((Boss)en).bossid).add(pl);
                }
                if(bossAttack) {
                    c.sendMessage("==========================\n**ENEMY PHASE**\n==========================").queue();
                    int id = ((Boss)en).bossid, size = BossBattle.getSize(id);
                    ArrayList<Player> characters = BossBattle.rooms.get(id);
                    for(int i = 0; i < size; i++) {     //only go through the bosses
                        c.sendMessage(battleResult(characters.get(i).authorid, true).build()).queue();
                        checkLevelUps();
                    }
                    BossBattle.turnover.get(id).clear();        //clear all turns
                    if(defeatBoss) {
                        bossDefeated(id);
                    }
                    defeatBoss = false;
                }
            }
            
            Bot.calculated.replace(event.getAuthor().getIdLong(), false);
            Bot.update();
        }
        else {
            throw new ValidationException("Your command was not recognized. Please try again.");
        }
    }

    private void bossDefeated(int id) {
        c.sendMessage("hi").queue();
        String pcs = "";
        for(int i = BossBattle.getSize(id); i < BossBattle.getSize(id) * 2; i++) {
            BossBattle.inboss.remove(BossBattle.rooms.get(id).get(i));
            pcs += Utilities.bold(BossBattle.rooms.get(id).get(i).username) + ", ";
            BossBattle.rooms.get(id).get(i).weapon = BossBattle.getReward(id);
        }
        c.sendMessage("Congratulations, " + pcs + "for defeating " + Utilities.bold(BossBattle.getName(id)) + ". **Reward:** " + BossBattle.getReward(id).displayName).queue();
        
        BossBattle.turnover.remove(id);
        BossBattle.rooms.remove(id);
    }

    private void checkLevelUps() {
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
}

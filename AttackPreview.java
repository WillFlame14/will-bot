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
            stratum = false;
            if(p.stats.chp == 0) {
                throw new ValidationException(p.username + " is currently at the brink of death. Please restore HP before continuining.");
            }
            if(p == e) {
                throw new ValidationException("You cannot attack yourself.");
            }
            if(Bot.incombat.contains(e)) {
                throw new ValidationException(e.username + " is already in combat.");
            }
            c.sendMessage(battleCalc(p, e, action.equals("w!attackp")).build()).queue();
            battleCalc(e, p, action.equals("w!attackp"));       //instantiate the EmbedBuilder for enemy phase
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
            c.sendMessage(battleResult(event.getAuthor().getIdLong()).build()).queue();
            checkLevelUps();
            String[] players = Bot.attackusers.get(event.getAuthor().getIdLong()).split(" ");
            Player pl = Bot.playermap.get(players[0]);
            if(Bot.playermap.containsKey(players[1]) && pl.stats.chp > 0) {    //stratum opponent did not die, player did not die
                Player en = Bot.playermap.get(players[1]);
                if(en.stats.chp > 0) {      //user opponent did not die
                    c.sendMessage(battleResult(en.authorid).build()).queue();
                    checkLevelUps();
                }
            }
            Bot.calculated.replace(event.getAuthor().getIdLong(), false);
            Bot.update();
        }
        else {
            throw new ValidationException("Your command was not recognized. Please try again.");
        }
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

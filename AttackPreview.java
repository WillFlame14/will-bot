package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AttackPreview extends Attack{
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!attackp") || action.equals("w!attackm");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        Player enemy = null;
        if(args.size() > 1) {       //w!attackp <player> <enemy>
            if(!Bot.playermap.containsKey(args.get(0)) || !Bot.playermap.containsKey(args.get(1))) {
                if(args.get(1).startsWith("stratum")) {
                    try {
                        int level = Integer.parseInt(args.get(1).substring(7));
                        enemy = Utilities.generateStratumOpponent(level).clone();
                        enemy.username += (int)(Math.random() * 1000) + 1;      //add identifier
                        Bot.playermap.put(enemy.username, enemy);
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
            Player p = Bot.playermap.get(args.get(0));
            Player e = stratum?enemy:Bot.playermap.get(args.get(1));
            stratum = false;
            if(p.stats.chp == 0) {
                throw new ValidationException(p.username + " is currently at the brink of death. Please restore HP before continuining.");
            }
            if(p == e) {
                c.sendMessage("You cannot attack yourself.").queue();
                return;
            }
            c.sendMessage(battleCalc(p, e, action.equals("w!attackp")).build()).queue();
        } 
        else {
            c.sendMessage("You did not specify enough arguments. The correct usage is `" + action + " <player> <enemy>`").queue();
            return;
        }
        Bot.calculated.replace(event.getAuthor().getIdLong(), true);
        Bot.calcid = event.getMessage().getAuthor().getIdLong();
    }
}

class AttackConfirm extends Attack{
    public boolean isActionApplicable(String action) {
        return action.equals("w!confirm");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(Bot.calcid != event.getMessage().getAuthor().getIdLong()) {
            throw new ValidationException("You may not confirm someone else's action.");
        }
        if(Bot.calculated.get(event.getAuthor().getIdLong())) {
            c.sendMessage(battleResult().build()).queue();
            if(!levelup.equals("")) {
                Player p = Bot.playermap.get(levelup);
                p.levelup();
                c.sendMessage(levelup + " has leveled up!\n").queue();
                c.sendMessage(p.showStats().build()).queue();
                levelup = "";
            }
            Bot.calculated.replace(event.getAuthor().getIdLong(), false);
            Bot.update();
        }
        else {
            throw new ValidationException("Your command was not recognized. Please try again.");
        }
    }
}

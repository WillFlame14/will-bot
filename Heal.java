package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Heal implements Category{
    public boolean isActionApplicable(String action) {
        return action.equals("w!heal");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        Player user;
        if(args.size() == 1 && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
            user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
            args.add(0, user.username);     //so that args.get(0) works
        }
        else if(args.size() < 2) {     //w!heal <healer> <user getting healed> 
            throw new ValidationException("You did not provide enough arguments. The correct format is `w!heal <user> <recipient>`.");
        }
        Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
        user = Bot.playermap.get(args.get(0));
        if(user.weapon.staff) {
            if(!Bot.playermap.containsKey(args.get(1))) {
                c.sendMessage("You did not specify a valid user to heal.").queue();
                return;
            }
            Player recipient = Bot.playermap.get(args.get(1));
            if (user.equals(recipient)) {
                throw new ValidationException("You cannot heal yourself.");
            }
            recipient.stats.chp += user.weapon.mt + user.stats.mag;
            int overflow = recipient.stats.chp - recipient.stats.thp;
            if(overflow > 0) {
                recipient.stats.chp = recipient.stats.thp;
                c.sendMessage(Utilities.bold(user.username) + " healed " + Utilities.bold(recipient.username) + " for " + (user.weapon.mt + user.stats.mag - overflow) + " HP."
                        + "\n\n" + Utilities.bold(recipient.username + "'s HP: ") + recipient.stats.chp + "/" + recipient.stats.thp).queue();
            }
            else {
                c.sendMessage(Utilities.bold(user.username) + " healed " + Utilities.bold(recipient.username) + " for " + (user.weapon.mt + user.stats.mag) + " HP."
                        + "\n\n" + Utilities.bold(recipient.username + "'s HP: ") + recipient.stats.chp + "/" + recipient.stats.thp).queue();
            }
            user.ranks.staffx += user.weapon.wex;
            if(user.ranks.staffx > WeaponRanks.getXpLevelUp(user.ranks.staff)) {
                c.sendMessage("\n⏫ " + Utilities.bold(user.username) + "'s weapon rank increased!\n").queue();
                user.ranks.staff++;
                user.ranks.staffx = 0;
            }
            Bot.update();
        }
        else {
            throw new ValidationException("The user specified does not have a staff equipped.");
        }
    }
}

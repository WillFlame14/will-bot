package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BotSystem implements Category{
    
    public boolean isActionApplicable(String action) {
        switch(action) {
            case "w!attackhelp":
            case "w!changelog":
            case "w!classes":
            case "w!help":
            case "w!ping":
            case "w!register":
            case "w!say":
            case "w!select":
            case "w!users":
                return true;
            default:
                return false;
        }
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        switch(action) {
            case "w!attackhelp":
                c.sendMessage(Bot.attackHelpEmbed).queue();
                break;
            case "w!changelog":
                c.sendMessage(Bot.changelog).queue();
                break;
            case "w!classes":
                PClass[] pclasses = PClass.values();
                String s = "```List of Classes:\n";
                for (int i = 1; i < pclasses.length - 1; i++) {
                    s += pclasses[i].toString() + ", ";
                }
                s += pclasses[pclasses.length - 1] + "```";
                c.sendMessage(s).queue();
                break;    
            case "w!help":
                c.sendMessage(Bot.helpEmbed).queue();
                break;
            case "w!ping":
                c.sendMessage("Pong! Time: " + Utilities.bold(Bot.jda.getPing() + " ms")).queue();
                break;
            case "w!register":
                if(!args.isEmpty()) {
                    String name = args.get(0);
                    if(Bot.playermap.containsKey(name)) {
                        throw new ValidationException("This username is already in use. Please try again.");
                    }
                    PClass pclass = PClass.Hero;
                    Player p;
                    if(args.size() > 1) {
                        try {
                            pclass = PClass.valueOf(args.get(1));
                            p = new Player(name, new Stats(pclass), new Growths(pclass), Weapon.Fist, new WeaponRanks(), Skill.NA, pclass, event.getAuthor().getIdLong());
                        }
                        catch(IllegalArgumentException e) {
                            throw new ValidationException("That class does not exist. Please try again. Note that usernames cannot contain spaces.");
                        }
                    }
                    else {
                        p = new Player(name, new Stats(), new Growths(), Weapon.Fist, new WeaponRanks(), Skill.NA, pclass, event.getAuthor().getIdLong());
                    }
                    Bot.playermap.put(args.get(0), p);
                    Bot.idmap.put(name, event.getAuthor().getIdLong());
                    Bot.calculated.put(event.getAuthor().getIdLong(), false);
                    c.sendMessage(name + " has been successfully added.").queue();
                    c.sendMessage(p.showStats().build()).queue();
                    Bot.update();       //update config.txt
                }
                else {        
                    throw new ValidationException("The correct format is `w!register <username> [<class>].`");  
                }
                break;
            case "w!say":
                String msg = "";
                if(args.contains("w!say")) {
                    throw new ValidationException("No crashing the bot. :C");
                }
                for(String st: args) {
                    msg += st + " ";
                }
                c.sendMessage(msg).queue();
                break;
            case "w!select":
                if(!args.isEmpty()) {
                    String name = args.get(0);
                    Utilities.checkPlayer(name, event.getMessage().getAuthor().getIdLong());
                    if(Bot.defaultPlayer.containsValue(Bot.playermap.get(name))) {      //since all characters have 1 authority, them as value means they are default
                        throw new ValidationException("You have already selected this character as default."); 
                    }
                    Bot.defaultPlayer.put(event.getAuthor().getIdLong(), Bot.playermap.get(name));
                    c.sendMessage(name + " has been set to your default character.").queue();
                    Bot.update();       //update config.txt
                }
                else {        
                    throw new ValidationException("You did not specify a username to select.");  
                }
                break;
            case "w!users":
                String names = "";
                Object[] nameset = Bot.playermap.keySet().toArray();
                for(int i = 0; i < nameset.length - 1; i++) {
                    names += nameset[i].toString() + ", ";
                }
                names += nameset[nameset.length - 1] + "```";
                c.sendMessage("```List of Registered Users:\n" + names).queue();
                break;
        }
    }
}

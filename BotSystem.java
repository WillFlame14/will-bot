package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BotSystem implements Category{
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!changelog") || action.equals("w!help") || action.equals("w!ping") || action.equals("w!register")
                || action.equals("w!roll") || action.equals("w!say") || action.equals("w!select") || action.equals("w!users");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        switch(action) {
            case "w!changelog":
                c.sendMessage(Bot.changelog).queue();
                break;
            case "w!help":
                c.sendMessage(Bot.helpEmbed).queue();
                break;
            case "w!ping":
                c.sendMessage("Pong! Time: " + Utilities.bold(Bot.jda.getPing() + " ms")).queue();
                break;
            case "w!roll":
                c.sendMessage("🎲You rolled a: " + (int)((Math.random() * 6) + 1)).queue();
                break;
            case "w!register":      //add class types?
                if(!args.isEmpty()) {
                    String name = args.get(0);
                    if(Bot.playermap.containsKey(name)) {
                        throw new ValidationException("This username is already in use. Please try again.");
                    }
                    if(name.contains(" ")) {
                        throw new ValidationException("A username cannot contain spaces. Please try again."); 
                    }
                    Player p = new Player(name, new Stats(), new Growths(), Weapon.Fist, new WeaponRanks(), Skill.NA, event.getAuthor().getIdLong());
                    Bot.playermap.put(args.get(0), p);
                    Bot.idmap.put(name, event.getAuthor().getIdLong());
                    Bot.calculated.put(event.getAuthor().getIdLong(), false);
                    c.sendMessage("You have been successfully added.").queue();
                    c.sendMessage(p.showStats().build()).queue();
                    Bot.update();       //update config.txt
                }
                else {        
                    throw new ValidationException("You did not specify a username to register.");  
                }
                break;
            case "w!say":
                String msg = "";
                if(args.contains("w!say")) {
                    throw new ValidationException("No crashing the bot. :C");
                }
                for(String s: args) {
                    msg += s + " ";
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
                names += nameset[nameset.length - 1];
                c.sendMessage("List of Registered Users:\n" + names).queue();
                break;
        }
    }
}

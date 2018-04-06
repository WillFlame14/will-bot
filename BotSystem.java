package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BotSystem implements Category{
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!help") || action.equals("w!ping") || action.equals("w!register") || action.equals("w!roll") || action.equals("w!users");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event) {
        MessageChannel c = event.getChannel();
        switch(action) {
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
                        c.sendMessage("This username is already in use. Please try again.").queue();
                        return;
                    }
                    Player p = new Player(name, new Stats(), new Growths(), Weapon.Fist, Skill.NA, event.getAuthor().getIdLong());
                    Bot.playermap.put(args.get(0), p);
                    Bot.idmap.put(name, event.getAuthor().getIdLong());
                    c.sendMessage("You have been successfully added.").queue();
                    c.sendMessage(p.showStats().build()).queue();
                    Bot.update();       //update config.txt
                }
                else {        
                    c.sendMessage("You did not specify a username to register.").queue();  
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

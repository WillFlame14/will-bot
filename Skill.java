package bot.willbot;

import java.util.ArrayList;
import java.util.HashMap;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Skill implements Category {
    NA("NA"), Adept("Adept"), Cancel("Cancel"), Crit15("Critical+15"), Resolve("Resolve"), Vantage("Vantage"), Wrath("Wrath");

    String displayName;
    
    Skill(String displayName) {
        this.displayName = displayName;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!assign") || action.equals("w!remove") || action.equals("w!skills");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        if(action.equals("w!assign")) {       //w!assign <user> <skill> 
            if(args.size() < 2) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!assign <user> <skill>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            Player user = Bot.playermap.get(args.get(0));
            try {
                Skill skill = Skill.valueOf(args.get(1));
                user.skill = skill;
                c.sendMessage("Your skill was assigned successfully.").queue();
                Bot.update();
            }
            catch(IllegalArgumentException e) {
                throw new ValidationException("You did not specify a valid skill to assign.");
            }
        }
        else if(action.equals("w!remove")) {      //w!remove <user>
            if(args.isEmpty()) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!assign <user> <skill>`");
            }
            Player user = Bot.playermap.get(args.get(0));
            if(user.skill == Skill.NA) {
                c.sendMessage("You do not currently have a skill equipped.").queue();
            }
            else {
                user.skill = Skill.NA;
                c.sendMessage("Your skill has been unequipped.").queue();
            }
        }
        else if(action.equals("w!skills")) {
            c.sendMessage("```Skills that are currently usable:\n"
                    + "Adept - Spd% chance of immediate double, regardless of speed.\n"
                    + "Cancel - Spd% chance of cancelling the opponent's counterattack.\n"
                    + "Crit15 - Increases critical chance by 15%.\n"
                    + "Resolve - Increases speed and skill by 1.5x when under 50% HP.\n"
                    + "Vantage - Spd% chance of attacking first, even when the enemy initiates.\n"
                    + "Wrath - Increases critical chance by 50% when under 50% HP.```").queue();
        }
    }
}

package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

enum Skill implements Category {
    NA("NA"), Adept("Adept"), Aptitude("Aptitude"), Blossom("Blossom"), Cancel("Cancel"), Crit15("Critical+15"), Discipline("Discipline"),
    Fortune("Fortune"), Paragon("Paragon"), Pavise("Pavise"), Resolve("Resolve"), Vantage("Vantage"), Wrath("Wrath");

    String displayName;
    
    Skill(String displayName) {
        this.displayName = displayName;
    }
    
    public String toString() {
        return displayName;
    }
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!assign") || action.equals("w!remove") || action.equals("w!skills");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        Player user = null;
        if(action.equals("w!assign")) {       //w!assign <user> <skill> 
            if(args.size() == 1 && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(0, user.username);     //so that args.get(0) works
            }
            else if (args.size() == 1) {    
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!assign <user> <skill>`");
            }
            Utilities.checkPlayer(args.get(0), event.getMessage().getAuthor().getIdLong());
            user = (user == null)?Bot.playermap.get(args.get(0)):user;
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
            if(args.isEmpty() && Bot.defaultPlayer.containsKey(event.getAuthor().getIdLong())) {    
                user = Bot.defaultPlayer.get(event.getAuthor().getIdLong());
                args.add(user.username);        //so that args.get(0) works
            }
            else if(args.isEmpty()) {
                throw new ValidationException("You did not provide enough arguments. The correct format is `w!assign <user> <skill>`");
            }
            user = Bot.playermap.get(args.get(0));
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
                    + "Aptitude - Adds 10% to all growth rates.\n"
                    + "Blossom - User gains 1/2 EXP,but has second chance for growth in stats.\n"
                    + "Cancel - Spd% chance of cancelling the opponent's counterattack.\n"
                    + "Crit15 - Increases critical chance by 15%.\n"
                    + "Discipline - Doubles weapon experience.\n"
                    + "Fortune - Negates critical attacks.\n"
                    + "Paragon - Doubles EXP earned.\n"
                    + "Pavise - Skl% chance of negating damage.\n"
                    + "Resolve - Increases speed and skill by 1.5x when under 50% HP.\n"
                    + "Vantage - Spd% chance of attacking first, even when the enemy initiates.\n"
                    + "Wrath - Increases critical chance by 50% when under 50% HP.```").queue();
        }
    }
}

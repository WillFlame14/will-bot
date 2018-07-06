package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Tags implements Category {
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!tag") || action.equals("w!t") || action.equals("w!taghelp") || action.equals("w!tags");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        switch(action) {
            case "w!tag":
            case "w!t":
                if(!args.isEmpty()) {
                    String name;
                    switch(args.get(0)) {
                        case "create":
                            name = args.get(1);
                            if(Bot.tags.containsKey(name)) {        //a tag already exists with this name
                                throw new ValidationException("This tag already exists.");
                            }
                            Bot.tags.put(name, new Tag(event.getAuthor().getIdLong(), 
                                    event.getMessage().getContentDisplay().substring(12 + (action.equals("w!t")?0:2) + name.length())));
                            c.sendMessage("Your tag has been created.").queue();
                            Bot.update();
                            break;
                        case "edit":
                            name = args.get(1);
                            if (!Bot.tags.containsKey(name)) {        //a tag does not exist with this name
                                throw new ValidationException("This tag does not exist.");
                            }
                            if(Bot.tags.get(name).userid != event.getAuthor().getIdLong()) {        //they did not create this tag
                                throw new ValidationException("You are not the owner of this tag.");
                            }
                            Bot.tags.get(name).contents = event.getMessage().getContentDisplay().substring(10 + (action.equals("w!t")?0:2) + name.length());      //overwrite
                            c.sendMessage("Your tag has been edited.").queue();
                            Bot.update();
                            break;
                        case "delete":
                            name = args.get(1);
                            if(!Bot.tags.containsKey(name)) {        //a tag does not exist with this name
                                throw new ValidationException("This tag does not exist.");
                            }
                            if(Bot.tags.get(name).userid != event.getAuthor().getIdLong()) {        //they did not create this tag
                                throw new ValidationException("You are not the owner of this tag.");
                            }
                            Bot.tags.remove(name);      
                            c.sendMessage("Your tag has been removed.").queue();
                            Bot.update();
                            break;    
                        default:        //use of tag
                            name = args.get(0);
                            c.sendMessage(render(args, Bot.tags.get(name).contents, event)).queue();
                            break;
                    }
                }
                else {
                    throw new ValidationException("The correct usage is `w!tag [create/edit/delete] <name> <content>. Use w!taghelp for more info.`");  
                }
                break;
            case "w!taghelp":
                c.sendMessage(Bot.tagHelpEmbed).queue();
                break;
            case "w!tags":
                Object[] tags = Bot.tags.keySet().toArray();
                String s = "```List of Tags:";
                for (Object t: tags) {
                    s += t + ",";
                }
                s = s.substring(0, s.length() - 1);     //remove the dangling comma
                s += "```";
                c.sendMessage(s).queue();
                break;  
        }
    }
    
    public String render(ArrayList<String> args, String contents, MessageReceivedEvent event) {
        if(contents.contains("{args")) {        //in case there are no args, because args.get(1) would throw IndexOutOfBounds
            if(args.size() == 1) {      //arg0 is tag name
                args.add("[argument not provided]");
            }
            contents = contents.replace("{args}", args.get(1));     
            while(contents.contains("{args;")) {
                int index = contents.indexOf("{args;");
                int argnum = Integer.parseInt(contents.charAt(index + 6) + "");
                contents = contents.replace("{args;" + argnum + "}", args.get(argnum + 1));
            }
        }
        contents = contents.replace("{user}", event.getAuthor().getName());     //but there is always a user
        return contents;
    }
}

class Tag {
    String contents;
    long userid;
    
    public Tag(long u, String c) {
        userid = u;
        contents = c;
    }
    
}

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
    
    public String render(ArrayList<String> args, String contents, MessageReceivedEvent event)throws ValidationException {
        if(contents.contains("{args")) {
            try {
                contents = contents.replace("{args}", args.get(1)); 
            }
            catch(IndexOutOfBoundsException e) {        //no args provided
                contents = contents.replace("{args}", "[no argument provided]"); 
            }
            while(contents.contains("{args;")) {
                int index = contents.indexOf("{args;");
                int argnum = Integer.parseInt(contents.charAt(index + 6) + "");
                try {
                    contents = contents.replace("{args;" + argnum + "}", args.get(argnum + 1));
                }
                catch(IndexOutOfBoundsException e) {        
                    contents = contents.replace("{args;" + argnum + "}", "[no argument provided]"); 
                }
            }
        }
        contents = contents.replace("{user}", event.getAuthor().getName());     //but there is always a user
        contents = contents.replace("{argslen}", (args.size() - 1) + "");       //remember, arg0 is tag name, so there is always 1
        
        ArrayList<String> parts = parse(contents);
        for(int i = 0; i < parts.size(); i++) {
            String s = parts.get(i);
            if(s.startsWith("{if;")) {
                try {
                    s = s.substring(4);     //remove front braces and if - leaving the back brace is for the null "else"
                    String[] sections = s.split(";");       //operation;arg1;arg2;then;else
                    boolean result;
                    switch(sections[0]) {
                        case "=":
                            result = Integer.parseInt(sections[1]) == Integer.parseInt(sections[2]);
                            break;
                        case "!=":
                            result = Integer.parseInt(sections[1]) != Integer.parseInt(sections[2]);
                            break;
                        case ">":
                            result = Integer.parseInt(sections[1]) > Integer.parseInt(sections[2]);
                            break;
                        case ">=":
                            result = Integer.parseInt(sections[1]) >= Integer.parseInt(sections[2]);
                            break;
                        case "<=":
                            result = Integer.parseInt(sections[1]) <= Integer.parseInt(sections[2]);
                            break;
                        case "<":
                            result = Integer.parseInt(sections[1]) < Integer.parseInt(sections[2]);
                            break;
                        default:
                            throw new ValidationException();
                    }
                    if(sections[4].equals("}")) {
                        sections[4] = "";
                    }
                    else {      //if not, the danging '}' needs to be removed
                        sections[4] = sections[4].substring(0, sections[4].length() - 1);
                    }
                    parts.set(i, result?sections[3]:sections[4]);
                }
                catch(Exception e) {
                    throw new ValidationException("This tag was not formatted correctly. Please check the if statements.");
                }
            }
        }
        contents = "";
        for(String s: parts) {
            contents += s;        //all spaces are already included in the parts
        }
        return contents;
    }
    
    public ArrayList<String> parse(String contents) {
        ArrayList<String> parts = new ArrayList<>();
        String temp = "";
        for(int i = 0; i < contents.length(); i++) {
            if(contents.charAt(i) == '{' && i > 0) {        //i > 0 prevents inserting an unnecessary empty string if contents starts with '{'
                parts.add(temp);
                temp = "";      //add everything up to this point
            }
            temp += contents.charAt(i);
            if(contents.charAt(i) == '}') {       
                parts.add(temp);
                temp = "";      //add whatever was in the tag
            }
            if(i == contents.length() - 1 && parts.size() < 1) {        //if there are no brackets
                parts.add(temp);
            }
        }
        return parts;
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
package bot.willbot;

import java.util.*;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

interface Category {
    public abstract boolean isActionApplicable(String action);
    
    public abstract void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException;
}

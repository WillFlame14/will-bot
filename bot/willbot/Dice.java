package bot.willbot;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Dice implements Category {
    
    public boolean isActionApplicable(String action) {
        return action.equals("w!r") || action.equals("w!roll") || action.equals("w!rollhelp");
    }
    
    public void response(String action, ArrayList<String> args, MessageReceivedEvent event)throws ValidationException {
        MessageChannel c = event.getChannel();
        switch(action) {
            case "w!r":
            case "w!roll":
                int sides = 6,
                 amount = 1,
                 threshold,
                 sum = 0;
                boolean basic = false;
                String result = "🎲You rolled: ";
                if (!args.isEmpty()) {
                    if (args.size() > 1) {       //condense all spaces
                        String str = "";
                        for (String st : args) {
                            str += st;
                        }
                        args.set(0, str);
                    }
                    if (args.get(0).contains("+")) {       //multiple types of dice
                        String[] parts = args.get(0).split("\\+");
                        for (String st : parts) {
                            String[] results;
                            if (st.contains("d")) {
                                if (st.contains(">") || st.contains("<")) {        //check
                                    boolean above;
                                    above = st.contains(">");
                                    amount = Integer.parseInt(st.substring(0, st.indexOf("d")));
                                    sides = Integer.parseInt(st.substring(st.indexOf("d") + 1, st.indexOf(above ? ">" : "<")));
                                    threshold = Integer.parseInt(st.substring(st.indexOf(above ? ">" : "<") + 1));
                                    results = roll(amount, sides, threshold, above);
                                } 
                                else {      //sum
                                    amount = Integer.parseInt(st.substring(0, st.indexOf("d")));
                                    sides = Integer.parseInt(st.substring(st.indexOf("d") + 1));
                                    results = roll(amount, sides);
                                }
                                result += results[0] + "+ ";
                                sum += Integer.parseInt(results[1]);
                            } 
                            else {      //integer
                                amount = Integer.parseInt(st);
                                result += amount + " + ";
                                sum += amount;
                            }
                        }
                        result = result.substring(0, result.length() - 2);      //remove dangling plus
                    } else {      //one type of dice
                        if (args.get(0).contains("d")) {
                            if (args.get(0).contains(">") || args.get(0).contains("<")) {        //check
                                boolean above;
                                above = args.get(0).contains(">");
                                amount = Integer.parseInt(args.get(0).substring(0, args.get(0).indexOf("d")));
                                sides = Integer.parseInt(args.get(0).substring(args.get(0).indexOf("d") + 1, args.get(0).indexOf(above ? ">" : "<")));
                                threshold = Integer.parseInt(args.get(0).substring(args.get(0).indexOf(above ? ">" : "<") + 1));
                                String[] results = roll(amount, sides, threshold, above);
                                result += results[0];
                                sum += Integer.parseInt(results[1]);
                            } 
                            else {      //sum
                                amount = Integer.parseInt(args.get(0).substring(0, args.get(0).indexOf("d")));
                                sides = Integer.parseInt(args.get(0).substring(args.get(0).indexOf("d") + 1));
                                String[] results = roll(amount, sides);
                                result += results[0];
                                sum += Integer.parseInt(results[1]);
                            }
                        } 
                        else {      //6-sided dice
                            throw new ValidationException("That is not a valid die.");
                        }
                    }
                } else {      // roll one 6-sided die
                    result += roll(amount, sides)[1];
                    basic = true;
                }
                c.sendMessage(result + (basic ? "" : "= " + sum)).queue();      //always a dangling space
                break;
            case "w!rollhelp":
                c.sendMessage(Bot.rollHelpEmbed).queue();
                break;
        }
    }
    
    private String[] roll(int amount, int sides) {
        String[] result = new String[2];
        result[0] = "(";
        int sum = 0;
        for(int i = 0; i < amount; i++) {
            int rng = (int)(Math.random() * sides) + 1;
            result[0] += rng + "+";
            sum += rng;
        }
        result[0] = result[0].substring(0, result[0].length() - 1) + ") ";     //remove dangling +, add close bracket
        result[1] = sum + "";
        return result;
    }
    
    private String[] roll(int amount, int sides, int threshold, boolean above) {
        String[] result = new String[2];
        result[0] = "(";
        int passed = 0;
        for(int i = 0; i < amount; i++) {
            int rng = (int)(Math.random() * sides) + 1;
            if(above ? rng > threshold : rng < threshold) {     //pass
                result[0] += rng + " ";
                passed++;
            }
            else {      //fail
                result[0] += "~~" + rng + "~~ ";
            }
        }
        result[0] = result[0].substring(0, result[0].length() - 1) + ", " + passed + (passed == 1?" success" : " successes") + ") ";     //remove dangling space, add end
        result[1] = passed + "";
        return result;
    }
}

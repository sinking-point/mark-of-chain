import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MyEvents {
	
	static Map<String, Markov> markMap = new ConcurrentHashMap<>();

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
    	Markov mark;
        synchronized(markMap) {
        	if(!markMap.containsKey(event.getGuild().getStringID())) {
        		mark = new Markov(2, event.getGuild().getStringID());
        		markMap.put(event.getGuild().getStringID(), mark);
        	} else {
        		mark = markMap.get(event.getGuild().getStringID());
        	}
        }
        
        String message = event.getMessage().getContent();
        
        mark.listen(message.toLowerCase());
        
        if(event.getMessage().getMentions().contains(event.getClient().getOurUser())) {
        	BotUtils.sendMessage(event.getChannel(), mark.speak());
        }
    }

}
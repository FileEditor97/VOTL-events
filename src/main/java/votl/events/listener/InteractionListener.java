package votl.events.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import votl.events.App;
import votl.events.objects.constants.Constants;

public class InteractionListener extends ListenerAdapter {

	private final App bot;

	public InteractionListener(App bot) {
		this.bot = bot;
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		try {
			if (event.getComponentId().startsWith("report")) {
				event.deferReply().queue();
				String userId = event.getComponentId().split(":")[1];
				MessageEmbed embed = event.getMessage().getEmbeds().get(0);
				// Delete message
				event.getMessage().delete().queue();
				// Report message
				// This uses development server's channel for reports
				MessageChannel channel = bot.JDA.getTextChannelById(1204730889066782730L);
				if (channel != null) {
					channel.sendMessage("Valentine reported\nSender: %s\nReceiver: %s".formatted(userId, event.getUser().getId()))
						.addEmbeds(new EmbedBuilder(embed).setColor(Constants.COLOR_FAILURE).build())
						.queue();
				}
				// Reply
				event.getHook().editOriginalEmbeds(bot.getEmbedUtil().getEmbed()
					.setDescription(bot.getLocaleUtil().getText(event, "bot.events.valentine.reported"))
					.build()
				).queue();
			}
		} catch (Throwable t) {
			// Logs throwable and trys to respond to the user with the error
			// Thrown errors are not user's error, but code's fault as such things should be catched earlier and replied properly
			bot.getLogger().error("ButtonInteraction Exception", t);
		}
	}

}

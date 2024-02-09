package votl.events.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import votl.events.App;
import votl.events.objects.constants.Constants;

public class InteractionListener extends ListenerAdapter {

	private final App bot;
	private final DiscordLocale globalLocale = DiscordLocale.RUSSIAN;

	public InteractionListener(App bot) {
		this.bot = bot;
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		final String buttonId = event.getComponentId();
		try {
			if (buttonId.startsWith("report")) {
				event.deferReply().queue();
				String userId = event.getComponentId().split(":")[1];
				MessageEmbed embed = event.getMessage().getEmbeds().get(0);
				// Delete message
				event.getMessage().delete().queue();
				// Report message
				// This uses development server's channel for reports
				MessageChannel channel = bot.JDA.getTextChannelById(1204730889066782730L);
				if (channel != null) {
					channel.sendMessage(Constants.FAILURE+"Valentine reported\nSender: <@%s>\nReceiver: <@%s>".formatted(userId, event.getUser().getId()))
						.addEmbeds(new EmbedBuilder(embed).setTitle(null).setFooter(null).setColor(Constants.COLOR_FAILURE).build())
						.queue();
				}
				// Reply
				event.getHook().editOriginalEmbeds(bot.getEmbedUtil().getEmbed()
					.setDescription(bot.getLocaleUtil().getText(event, "bot.events.valentine.reported"))
					.build()
				).queue();
			} else if (buttonId.startsWith("allow")) {
				event.deferEdit().queue();
				String targetId = buttonId.split(":")[1];

				event.getJDA().retrieveUserById(targetId).queue(target -> {
					target.openPrivateChannel().queue(targetPc -> {
						Button report = Button.secondary("report:"+event.getMessage().getEmbeds().get(0).getTitle(), "Report");
						targetPc.sendMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0))
							.setColor(0xAF2655)
							.setTitle(bot.getLocaleUtil().getLocalized(globalLocale, "bot.events.valentine.received"))
							.setFooter(bot.getLocaleUtil().getLocalized(globalLocale, "bot.events.valentine.report"))
							.build()
						).addActionRow(report).queue(done -> {
							event.getHook().editOriginal(event.getMessage().getContentRaw()+"\n"+Constants.SUCCESS+" SENT").setEmbeds().setComponents().queue();
						}, 
						failure -> {
							event.getHook().editOriginal(event.getMessage().getContentRaw()+"\n"+Constants.FAILURE+" Unable to send message to target\n"+failure.getMessage()).setEmbeds().setComponents().queue();
						});
					});
				},
				failure -> {
					event.getHook().editOriginal(event.getMessage().getContentRaw()+"\n"+Constants.FAILURE+" User not found").setEmbeds().setComponents().queue();
				});
			} else if (buttonId.equals("reject")) {
				event.editMessage(event.getMessage().getContentRaw()+"\n"+Constants.FAILURE+" REJECTED").setEmbeds().setComponents().queue();
			}
		} catch (Throwable t) {
			// Logs throwable and trys to respond to the user with the error
			// Thrown errors are not user's error, but code's fault as such things should be catched earlier and replied properly
			bot.getLogger().error("ButtonInteraction Exception", t);
		}
	}

}

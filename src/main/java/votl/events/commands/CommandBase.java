package votl.events.commands;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.MessageEmbed;
import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;

public abstract class CommandBase extends SlashCommand {
	
	public CommandBase(App bot) {
		this.bot = bot;
		this.lu = bot.getLocaleUtil();
	}

	// reply to event
	public final void createReply(SlashCommandEvent event, @Nonnull String msg) {
		event.reply(msg).setEphemeral(true).queue();
	}

	public final void createReplyEmbed(SlashCommandEvent event, @Nonnull MessageEmbed... embeds) {
		event.deferReply(true).addEmbeds(embeds).queue();
	}

	// Error
	public final void createError(SlashCommandEvent event, @Nonnull String path) {
		createReplyEmbed(event, bot.getEmbedUtil().getError(event, path));
	}

	public final void createError(SlashCommandEvent event, @Nonnull String path, String reason) {
		createReplyEmbed(event, bot.getEmbedUtil().getError(event, path, reason));
	}

	
	// editOriginal with InteractionHook
	public final void editHook(SlashCommandEvent event, @Nonnull String msg) {
		event.getHook().editOriginal(msg).queue();
	}

	public final void editHookEmbed(SlashCommandEvent event, @Nonnull MessageEmbed... embeds) {
		event.getHook().editOriginalEmbeds(embeds).queue();
	}

	// Error
	public final void editError(SlashCommandEvent event, @Nonnull String path) {
		editHookEmbed(event, bot.getEmbedUtil().getError(event, path));
	}

	public final void editError(SlashCommandEvent event, @Nonnull String path, String reason) {
		editHookEmbed(event, bot.getEmbedUtil().getError(event, path, reason));
	}

}

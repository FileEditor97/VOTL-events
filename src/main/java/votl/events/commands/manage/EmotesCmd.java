package votl.events.commands.manage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.Emotes;
import votl.events.objects.Keyword;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class EmotesCmd extends CommandBase {
	
	public EmotesCmd(App bot) {
		super(bot);
		this.name = "emotes";
		this.path = "bot.manage.emotes";
		this.children = new SlashCommand[]{new Add(bot), new Remove(bot), new View(bot)};
		this.category = CmdCategory.MANAGE;
		this.adminCommand = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {}

	private class Add extends SlashCommand {
		public Add(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "add";
			this.path = "bot.manage.emotes.add";
			this.options = List.of(
				new OptionData(OptionType.STRING, "trigger", lu.getText(path+".trigger.help"), true)
					.setMaxLength(40),
				new OptionData(OptionType.STRING, "emoji", lu.getText(path+".emoji.help"), true),
				new OptionData(OptionType.INTEGER, "expire_days", lu.getText(path+".expire_days.help"))
					.setRequiredRange(1, 90),
				new OptionData(OptionType.BOOLEAN, "exact", lu.getText(path+".exact.help"))
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			long guildId = event.getGuild().getIdLong();
			String trigger = event.optString("trigger").strip().toLowerCase();
			String emoji = event.optString("emoji");
			Boolean exact = event.optBoolean("exact", false);
			if (event.hasOption("expire_days")) {
				Instant until = Instant.now().plus(event.optInteger("expire_days"), ChronoUnit.DAYS);
				bot.getDBUtil().emotes.addEmote(guildId, trigger, emoji, null, until, exact);
			} else {
				bot.getDBUtil().emotes.addEmote(guildId, trigger, emoji, exact);
			}
			bot.addEmojiKeyword(guildId, trigger, Emoji.fromFormatted(emoji), exact);

			createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setColor(Constants.COLOR_SUCCESS)
				.setDescription(lu.getText(event, path+".done").formatted(trigger, emoji))
				.build());
		}
	}

	private class Remove extends SlashCommand {
		public Remove(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "remove";
			this.path = "bot.manage.emotes.remove";
			this.options = List.of(
				new OptionData(OptionType.STRING, "trigger", lu.getText(path+".trigger.help"), true)
					.setMaxLength(40)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			long guildId = event.getGuild().getIdLong();
			String trigger = event.optString("trigger").strip().toLowerCase();
			if (bot.getDBUtil().emotes.getId(trigger) == null) {
				createError(event, path+".not_found", "Received: "+trigger);
				return;
			}
			bot.getDBUtil().emotes.deleteEmote(trigger);
			if (bot.removeEmojiKeyword(guildId, trigger)) {
				createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
					.setColor(Constants.COLOR_SUCCESS)
					.setDescription(lu.getText(event, path+".done_deleted").formatted(trigger))
					.build());
			} else {
				createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
					.setColor(Constants.COLOR_SUCCESS)
					.setDescription(lu.getText(event, path+".done").formatted(trigger))
					.build());
			}
		}
	}

	private class View extends SlashCommand {
		public View(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "view";
			this.path = "bot.manage.emotes.view";
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			long guildId = event.getGuild().getIdLong();
			EmbedBuilder builder = bot.getEmbedUtil().getEmbed(event)
				.setTitle(lu.getText(event, path+".title"));
			
			List<Keyword> keywords = bot.getDBUtil().emotes.getEmotes(guildId);
			if (keywords.isEmpty()) {
				builder.setDescription(lu.getText(event, path+".empty"));
			} else {
				keywords.forEach(keyword -> {
					builder.appendDescription("%s | %s `%s`\n".formatted(keyword.getEmoji(), keyword.isExact() ? Emotes.CHECK_C.getEmote() : Emotes.CROSS_C.getEmote(), keyword.getTrigger()));
				});
			}
			createReplyEmbed(event, builder.build());
		}
	}

}

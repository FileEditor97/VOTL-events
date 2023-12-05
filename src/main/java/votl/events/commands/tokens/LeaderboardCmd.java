package votl.events.commands.tokens;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;

public class LeaderboardCmd extends CommandBase {
	
	public LeaderboardCmd(App bot) {
		super(bot);
		this.name = "leaderboard";
		this.path = "bot.tokens.leaderboard";
		this.options = List.of(
			new OptionData(OptionType.BOOLEAN, "full", lu.getText(path+".full.help"))
		);
		this.category = CmdCategory.TOKENS;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.deferReply().queue();
		final DiscordLocale locale = event.getGuildLocale();
		EmbedBuilder builder = new EmbedBuilder().setColor(0xA40808)
			.setAuthor(lu.getLocalized(locale, path+".title"), null, "https://imgur.com/WJhoFXS.png")
			.addField(createField(locale, path+".amount", false, bot.getDBUtil().tokens.getTopAmount(event.getGuild().getIdLong())));

		if (event.optBoolean("full", false))
			builder.addField(createField(locale, path+".earned", true, bot.getDBUtil().tokenUpdates.getTopEarned(event.getGuild().getIdLong())))
				.addField(createField(locale, path+".spend", true, bot.getDBUtil().tokenUpdates.getTopSpend(event.getGuild().getIdLong())));
		
		event.getHook().editOriginalEmbeds(builder.build()).queue();
	}

	private Field createField(final DiscordLocale locale, final String titlePath, final boolean inline, List<Pair<Long, Integer>> data) {
		if (data.isEmpty()) {
			return new Field(lu.getLocalized(locale, titlePath), lu.getLocalized(locale, path+".empty"), inline);
		}
		StringBuffer buffer = new StringBuffer();
		data.forEach(entry -> {
			buffer.append("1. <@%s> | **%d**\n".formatted(entry.getLeft(), entry.getRight()));
		});
		return new Field(lu.getLocalized(locale, titlePath), buffer.toString(), inline);
	}
}

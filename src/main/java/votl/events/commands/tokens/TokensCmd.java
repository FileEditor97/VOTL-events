package votl.events.commands.tokens;

import java.time.Instant;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.Emotes;
import votl.events.objects.EventActions;
import votl.events.objects.ActionLog;
import votl.events.objects.constants.CmdCategory;

public class TokensCmd extends CommandBase {

	final List<EventActions> allowed = List.of(EventActions.ADD_TOKENS, EventActions.REMOVE_TOKENS, EventActions.TRANSFER);
	
	public TokensCmd(App bot) {
		super(bot);
		this.name = "tokens";
		this.path = "bot.tokens.tokens";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help")),
			new OptionData(OptionType.BOOLEAN, "history", lu.getText(path+".history.help"))
		);
		this.category = CmdCategory.TOKENS;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.deferReply().queue();
		// Get user from option or use author
		Member target = event.optMember("user", event.getMember());

		// Show user's toke history, maximum ~10
		Boolean showHistory = event.optBoolean("history", false);

		Integer tokenAmount = bot.getDBUtil().tokens.getTokens(event.getGuild().getIdLong(), target.getIdLong());

		EmbedBuilder builder = bot.getEmbedUtil().getEmbed(event).setColor(0xA40808)
			.setTitle(lu.getText(event, path+".embed.title").formatted(target.getUser().getName()))
			.setThumbnail(target.getEffectiveAvatarUrl())
			.addField(lu.getText(event, path+".embed.tokens"), tokenAmount.toString()+" "+Emotes.RISE_TOKEN.getEmote(), false);

		if (showHistory) {
			builder.addField(lu.getText(event, path+".embed.history"), getHistory(
				event.getGuildLocale(),
				bot.getDBUtil().tokenUpdates.getUserLogs(event.getGuild().getIdLong(), target.getIdLong(), 0, 8)
			), false);
		}

		event.getHook().editOriginalEmbeds(builder.build()).queue();
	}

	private String getHistory(DiscordLocale locale, List<ActionLog> input) {
		StringBuffer buffer = new StringBuffer();
		List<ActionLog> actions = input.stream().filter(event -> allowed.contains(event.getEventType())).toList();
		
		if (actions.isEmpty())
			buffer.append(lu.getLocalized(locale, path+".embed.empty"));
		else
			actions.forEach(action -> {
				Instant time = action.getTime();
				String tokens = "%+d".formatted(action.getTokenAmount());
				String reason = action.getReason();
				buffer.append("`%5s` | %s | `%s`\n".formatted(tokens, TimeFormat.DATE_TIME_SHORT.format(time), reason));
			});
		
		return buffer.toString();
	}
}

package votl.events.commands.tokens;

import java.time.Instant;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.CooldownScope;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.ActionLog.EventAction;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class RemoveCmd extends CommandBase {
	
	public RemoveCmd(App bot) {
		super(bot);
		this.name = "remove";
		this.path = "bot.tokens.remove";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"), true),
			new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help"), true)
				.setRequiredRange(1, 600),
			new OptionData(OptionType.STRING, "reason", lu.getText(path+".reason.help")),
			new OptionData(OptionType.BOOLEAN, "to_bank", lu.getText(path+".to_bank.help"))
		);
		this.category = CmdCategory.TOKENS;
		this.adminCommand = true;
		this.cooldown = 10;
		this.cooldownScope = CooldownScope.USER_GUILD;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Member target = event.optMember("user");
		if (target.getUser().isBot()) {
			createReply(event, "User is bot");
			return;
		}
		event.deferReply().queue();
		
		Integer removeTokens = event.optInteger("amount");
		String reason = event.optString("reason", lu.getLocalized(event.getGuildLocale(), path+".no_reason"));

		long guildId = event.getGuild().getIdLong();
		long targetId = target.getIdLong();

		Integer currentAmount = bot.getDBUtil().tokens.getTokens(guildId, targetId);
		if (currentAmount - removeTokens < 0) {
			editHookEmbed(event, bot.getEmbedUtil().getError(event, path+".not_enough", "%d / %d".formatted(removeTokens, currentAmount)));
			return;
		}

		Instant updateTime = Instant.now();
		bot.getDBUtil().tokens.changeTokens(guildId, targetId, -removeTokens, updateTime);
		if (event.optBoolean("to_bank", false))
			bot.getDBUtil().bank.changeAmount(guildId, removeTokens);
		bot.getDBUtil().tokenUpdates.logAction(guildId, targetId, event.getUser().getIdLong(), updateTime,
			EventAction.REMOVE_TOKENS, -removeTokens, reason);

		event.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Constants.COLOR_SUCCESS).setDescription(lu.getText(event, path+".done")
				.replace("{user}", target.getAsMention())
				.replace("{amount}", removeTokens.toString())
			).build()).queue();
	}

}

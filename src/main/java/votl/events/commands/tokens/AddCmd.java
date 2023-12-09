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

public class AddCmd extends CommandBase {
	
	public AddCmd(App bot) {
		super(bot);
		this.name = "add";
		this.path = "bot.tokens.add";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"), true),
			new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help"), true)
				.setRequiredRange(1, 200),
			new OptionData(OptionType.STRING, "reason", lu.getText(path+".reason.help"))
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
		Integer addTokens = event.optInteger("amount");
		String reason = event.optString("reason", lu.getLocalized(event.getGuildLocale(), path+".no_reason"));

		long guildId = event.getGuild().getIdLong();
		long targetId = target.getIdLong();

		Instant updateTime = Instant.now();
		bot.getDBUtil().tokens.changeTokens(guildId, targetId, addTokens, updateTime);
		bot.getDBUtil().tokenUpdates.logAction(guildId, targetId, event.getUser().getIdLong(), updateTime,
			EventAction.ADD_TOKENS, addTokens, reason);

		event.replyEmbeds(new EmbedBuilder().setColor(Constants.COLOR_SUCCESS).setDescription(lu.getText(event, path+".done")
				.replace("{user}", target.getAsMention())
				.replace("{amount}", addTokens.toString())
			).build()).queue();
	}

}

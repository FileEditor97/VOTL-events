package votl.events.commands.tokens;

import java.time.Instant;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.CooldownScope;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.EventActions;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class TransferCmd extends CommandBase {
	
	public TransferCmd(App bot) {
		super(bot);
		this.name = "transfer";
		this.path = "bot.tokens.transfer";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"), true),
			new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help"), true)
				.setRequiredRange(2, 400)
		);
		this.category = CmdCategory.TOKENS;
		this.cooldown = 10;
		this.cooldownScope = CooldownScope.USER;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Member target = event.optMember("user");
		if (target.getUser().isBot() || target.equals(event.getMember())) {
			createError(event, path+".no_user");
			return;
		}

		long authorId = event.getUser().getIdLong();
		final long guildId = event.getGuild().getIdLong();
		int currentAmount = bot.getDBUtil().tokens.getTokens(guildId, authorId);
		int transferAmount = event.optInteger("amount");
		if (currentAmount - transferAmount < 0) {
			createError(event, path+".not_enough", "Balance: %d".formatted(currentAmount));
			return;
		}
		event.deferReply().queue();

		int cutPercent = bot.getDBUtil().bank.getTransferCut(guildId);
		int minTransfer = cutPercent==0 ? 2 : Math.round(100/cutPercent);
		if (transferAmount < minTransfer) {
			editError(event, path+".min", "`%d < [%d]`".formatted(transferAmount, minTransfer));
			return;
		}
		int transfered = bot.getDBUtil().tokenUpdates.getTransferedAmount(guildId, authorId, Instant.now());
		int maxTransfer = bot.getDBUtil().bank.getMaxTransferAmount(guildId);
		if (transfered+transferAmount > maxTransfer) {
			editError(event, path+".max", "`%d+%d > [%d]`".formatted(transfered, transferAmount, maxTransfer));
			return;
		}

		// Add bank cut
		int bankCut = Math.round(transferAmount*cutPercent/100);
		bot.getDBUtil().bank.changeAmount(guildId, bankCut);
		// Transfer left amount
		Instant currentTime = Instant.now();
		bot.getDBUtil().tokens.addTokens(guildId, authorId, -transferAmount, currentTime);
		transferAmount = transferAmount-bankCut;
		bot.getDBUtil().tokens.addTokens(guildId, target.getIdLong(), transferAmount, currentTime);
		// Log for both users - sender and receiver
		bot.getDBUtil().tokenUpdates.logAction(guildId, authorId, null, currentTime, EventActions.TRANSFER, -transferAmount-bankCut, "--> @"+target.getUser().getName());
		bot.getDBUtil().tokenUpdates.logAction(guildId, target.getIdLong(), null, currentTime, EventActions.TRANSFER, transferAmount, "<-- @"+event.getUser().getName());
		
		editHookEmbed(event, bot.getEmbedUtil().getEmbed(event).setColor(Constants.COLOR_SUCCESS)
			.setDescription(lu.getText(event, path+".done").replace("{amount}", "%d||(+%d -> Bank)||".formatted(transferAmount, bankCut)).replace("{user}", target.getAsMention()))
			.build()
		);
	}
	
}

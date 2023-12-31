package votl.events.commands.shop;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.App;
import votl.events.base.command.CooldownScope;
import votl.events.base.command.SlashCommandEvent;
import votl.events.base.waiter.EventWaiter;
import votl.events.commands.CommandBase;
import votl.events.objects.ActionLog.EventAction;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ExchangeCmd extends CommandBase {

	private EventWaiter waiter;

	public ExchangeCmd(App bot, EventWaiter waiter) {
		super(bot);
		this.name = "exchange";
		this.path = "bot.shop.exchange";
		this.options = List.of(
			new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help"), true)
				.setRequiredRange(10, 400)
		);
		this.category = CmdCategory.TOKENS;
		this.cooldown = 10;
		this.cooldownScope = CooldownScope.USER;
		this.waiter = waiter;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		long authorId = event.getUser().getIdLong();
		final long guildId = event.getGuild().getIdLong();
		int currentAmount = bot.getDBUtil().tokens.getTokens(guildId, authorId);
		int exchangeAmount = event.optInteger("amount");
		if (currentAmount - exchangeAmount < 0) {
			createError(event, path+".not_enough", "Balance: %d".formatted(currentAmount));
			return;
		}
		event.deferReply().queue();

		Pair<Integer, Integer> range = bot.getDBUtil().bank.getExchangeAmountRange(guildId);
		if (exchangeAmount < range.getLeft()) {
			editError(event, path+".min", "`%d < [%d]`".formatted(exchangeAmount, range.getLeft()));
			return;
		}
		int exchangedLastMonth = bot.getDBUtil().tokenUpdates.getExchangedAmount(guildId, authorId, Instant.now());
		if (exchangedLastMonth+exchangeAmount > range.getRight()) {
			editError(event, path+".max", "`%d+%d > [%d]`".formatted(exchangedLastMonth, exchangeAmount, range.getRight()));
			return;
		}

		Float rate = bot.getDBUtil().bank.getExchangeRate(guildId);
		if (rate == 0f) {
			editError(event, path+".disabled");
			return;
		}
		int coins = Math.round(exchangeAmount*rate);

		Button confirm = Button.success("confirm", lu.getText(event, path+".button"));

		event.getHook().editOriginalEmbeds(bot.getEmbedUtil().getEmbed(event).setColor(Constants.COLOR_WARNING)
			.setDescription(lu.getText(event, path+".confirm").replace("{tokens}", String.valueOf(exchangeAmount)).replace("{coins}", String.valueOf(coins)))
			.build()).setActionRow(confirm).queue(msg ->
		{
			waiter.waitForEvent(
				ButtonInteractionEvent.class,
				e -> e.getMessageId().equals(msg.getId()) && e.getMember().equals(event.getMember()),
				actionEvent -> {
					// Remove from user
					bot.getDBUtil().tokens.changeTokens(guildId, authorId, -exchangeAmount, Instant.now());
					// To bank
					bot.getDBUtil().bank.changeAmount(guildId, exchangeAmount);
					// Log action
					bot.getDBUtil().tokenUpdates.logAction(guildId, authorId, null, Instant.now(), EventAction.EXCHANGED, -exchangeAmount, "Exchange to %d coins".formatted(coins));
					// Send request
					MessageChannel channel = bot.JDA.getTextChannelById(bot.getDBUtil().bank.getRequestsChannel(guildId));
					if (channel != null) {
						channel.sendMessage("Exchange request by %s\n> %d -> %d".formatted(event.getMember().getAsMention(), exchangeAmount, coins)).queue();
					}
					// Reply
					msg.editMessageEmbeds(bot.getEmbedUtil().getEmbed(event).setColor(Constants.COLOR_SUCCESS)
						.setDescription(lu.getText(event, path+".done").replace("{tokens}", String.valueOf(exchangeAmount)).replace("{coins}", String.valueOf(coins)))
						.build()).queue();
				},
				30,
				TimeUnit.SECONDS,
				() -> {
					msg.editMessageComponents(ActionRow.of(Button.secondary("timed_out", lu.getText(event, "errors.timed_out"))).asDisabled()).queue();
				});
		});
	}
	
}

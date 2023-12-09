package votl.events.commands.shop;

import java.time.Instant;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.ShopItem;
import votl.events.objects.ActionLog.EventAction;
import votl.events.objects.ShopItem.ItemType;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ShopCmd extends CommandBase {
	
	public ShopCmd(App bot) {
		super(bot);
		this.name = "shop";
		this.path = "bot.shop.shop";
		this.children = new SlashCommand[]{new Buy(bot), new View(bot)};
		this.category = CmdCategory.SHOP;
	}

	@Override
	protected void execute(SlashCommandEvent event) {}

	private class Buy extends SlashCommand {
		
		public Buy(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "buy";
			this.path = "bot.shop.shop.buy";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "item", lu.getText(path+".item.help"), true, true)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			int itemId = event.optInteger("item");
			ShopItem item = bot.getDBUtil().items.getItem(itemId);
			if (item == null) {
				createError(event, path+".not_found", "ID: %d".formatted(itemId));
			}

			event.deferReply().queue();
			long guildId = event.getGuild().getIdLong();
			long userId = event.getUser().getIdLong();
			int balance = bot.getDBUtil().tokens.getTokens(guildId, userId);
			int price = item.getPrice();
			if (balance < price) {
				editError(event, path+".not_enough", "Balance: %d\nPrice: %d".formatted(balance, price));
				return;
			}
			if (item.getAmount() == 0) {
				editError(event, path+".unavailable");
				return;
			} else if (item.getAmount() > 0) {
				bot.getDBUtil().items.changeAmount(itemId, -1);		// subtract 1 amount
			}

			Instant currentTime = Instant.now();
			bot.getDBUtil().tokens.changeTokens(guildId, userId, -price, currentTime);	// remove from user's balance
			bot.getDBUtil().bank.changeAmount(guildId, price);							// add to bank's balance
			bot.getDBUtil().tokenUpdates.logAction(guildId, userId, null, currentTime, EventAction.BUY_ITEM, -price, item.getName());		// log action
			// TODO: assign item
			MessageChannel channel = bot.JDA.getTextChannelById(bot.getDBUtil().bank.getRequestsChannel(guildId));
			if (channel != null) {
				channel.sendMessage("Item request by %s\n> %s".formatted(event.getMember().getAsMention(), item.getName())).queue();
			}
			// reply
			editHookEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setColor(Constants.COLOR_SUCCESS)
				.setDescription(lu.getText(event, path+".done").formatted(item.getName(), price, item.getDescription(), balance-price))
				.build());
		}

	}

	private class View extends SlashCommand {
		
		public View(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "view";
			this.path = "bot.shop.shop.view";
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			event.deferReply().queue();
			
			long guildId = event.getGuild().getIdLong();
			EmbedBuilder builder = new EmbedBuilder().setColor(Constants.COLOR_DEFAULT)
				.setTitle(lu.getLocalized(event.getGuildLocale(), path+".title"))
				.setFooter(event.getGuild().getName());
			
			List<ShopItem> items = bot.getDBUtil().items.getItems(guildId);
			if (items.isEmpty()) {
				builder.setDescription(lu.getLocalized(event.getGuildLocale(), path+".empty"));
			} else {
				items.forEach(item -> {
					String text = item.formatted(event.getGuildLocale(), lu);
					if (item.getType() == ItemType.EXCHANGE)
						text.replace("{rate}", String.format("%.2f", bot.getDBUtil().bank.getExchangeRate(guildId)));
					builder.appendDescription(text);
				});
			}

			editHookEmbed(event, builder.build());
		}

	}

}

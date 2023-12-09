package votl.events.commands.shop;

import java.util.List;

import org.json.JSONObject;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.ShopItem;
import votl.events.objects.ShopItem.ItemType;
import votl.events.objects.constants.CmdCategory;

public class ItemsCmd extends CommandBase {
	
	public ItemsCmd(App bot) {
		super(bot);
		this.name = "items";
		this.path = "bot.shop.items";
		this.children = new SlashCommand[]{new Add(bot), new SetPrice(bot), new SetAmount(bot), new Remove(bot)};
		this.category = CmdCategory.SHOP;
		this.adminCommand = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {}

	private class Add extends SlashCommand {
		public Add(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "add";
			this.path = "bot.shop.items.add";
			this.options = List.of(
				new OptionData(OptionType.STRING, "name", lu.getText(path+".name.help"), true).setMaxLength(100),
				new OptionData(OptionType.INTEGER, "price", lu.getText(path+".price.help"), true).setRequiredRange(0, 300),
				new OptionData(OptionType.STRING, "description", lu.getText(path+".description.help")).setMaxLength(400),
				new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help")).setRequiredRange(-1, 100),
				new OptionData(OptionType.INTEGER, "type", lu.getText(path+".type.help")).addChoices(ShopItem.ItemType.getAsChoices()),
				new OptionData(OptionType.INTEGER, "duration", lu.getText(path+".duration.help")).setRequiredRange(0, 60),
				new OptionData(OptionType.INTEGER, "target_id", lu.getText(path+".target_id.help"))
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			event.deferReply(true).queue();
			long guildId = event.getGuild().getIdLong();

			String name = event.optString("name");
			String description = event.optString("description", "-");
			Integer price = event.optInteger("price");
			Integer amount = event.optInteger("amount", 0);
			ItemType type = ItemType.byType(event.optInteger("type", 0));

			JSONObject jsonObject = new JSONObject();
			switch (type) {
				case ROLE:
				case ROLE_CUSTOM:
				case ROLE_EXTEND:
				case VOICE_CHANNEL:
				case VOICE_EXTEND:
				case EMOTE:
					jsonObject.put("time", event.optInteger("duration", 0));
					break;
				default:
					break;
			}
			switch (type) {
				case ROLE:
					jsonObject.put("role", event.optInteger("target_id", 0));
					break;
				case VOICE_CHANNEL:
					jsonObject.put("category", event.optInteger("target_id", 0));
					break;
				default:
					break;
			}
			bot.getDBUtil().items.addItem(guildId, name, description, price, amount, type, jsonObject);

			String amountText = amount<0 ? lu.getText(event, "items.unlimited") : amount.toString();
			editHookEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setDescription(lu.getText(event, path+".done").formatted(name, description, type.getName(), price, amountText))
				.build());
		}
	}

	private class SetPrice extends SlashCommand {
		public SetPrice(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "setprice";
			this.path = "bot.shop.items.setprice";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "item_id", lu.getText(path+".item_id.help"), true, true),
				new OptionData(OptionType.INTEGER, "price", lu.getText(path+".price.help"), true).setRequiredRange(0, 300)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			Integer id = event.optInteger("item_id");
			Integer newPrice = event.optInteger("price");

			if (bot.getDBUtil().items.getName(id) == null) {
				createError(event, path+".not_found", "Received ID: %d".formatted(id));
				return;
			}
			bot.getDBUtil().items.setPrice(id, newPrice);

			createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setDescription(lu.getText(event, path+".done").formatted(id, newPrice))
				.build());
		}
	}

	private class SetAmount extends SlashCommand {
		public SetAmount(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "setamount";
			this.path = "bot.shop.items.setamount";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "item_id", lu.getText(path+".item_id.help"), true, true),
				new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help"), true).setRequiredRange(-1, 100)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			Integer id = event.optInteger("item_id");
			Integer newAmount = event.optInteger("amount");

			if (bot.getDBUtil().items.getName(id) == null) {
				createError(event, path+".not_found", "Received ID: %d".formatted(id));
				return;
			}
			bot.getDBUtil().items.setAmount(id, newAmount);

			String amountText = newAmount<0 ? lu.getText(event, "items.unlimited") : newAmount.toString();
			createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setDescription(lu.getText(event, path+".done").formatted(id, amountText))
				.build());
		}
	}

	private class Remove extends SlashCommand {
		public Remove(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "remove";
			this.path = "bot.shop.items.remove";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "item_id", lu.getText(path+".item_id.help"), true, true)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			Integer id = event.optInteger("item_id");

			if (bot.getDBUtil().items.getName(id) == null) {
				createError(event, path+".not_found", "Received ID: %d".formatted(id));
				return;
			}
			bot.getDBUtil().items.removeItem(id);

			createReplyEmbed(event, bot.getEmbedUtil().getEmbed(event)
				.setDescription(lu.getText(event, path+".done").formatted(id))
				.build());
		}
	}

}

package votl.events.commands.manage;

import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;
import votl.events.utils.exceptions.CheckException;

public class BankCmd extends CommandBase {
	
	public BankCmd(App bot) {
		super(bot);
		this.name = "bank";
		this.path = "bot.manage.bank";
		this.options = List.of(
			new OptionData(OptionType.CHANNEL, "logs", lu.getText(path+".logs.help")).setChannelTypes(ChannelType.TEXT),
			new OptionData(OptionType.INTEGER, "transfer_cut", lu.getText(path+".transfer_cut.help")).setRequiredRange(0, 60),
			new OptionData(OptionType.INTEGER, "max_transfer", lu.getText(path+".max_transfer.help")).setRequiredRange(50, 400),
			new OptionData(OptionType.NUMBER, "exchange_rate", lu.getText(path+".exchange_rate.help")).setRequiredRange(0, 20),
			new OptionData(OptionType.INTEGER, "min_exchange", lu.getText(path+".min_exchange.help")).setRequiredRange(1, 50),
			new OptionData(OptionType.INTEGER, "max_exchange", lu.getText(path+".max_exchange.help")).setRequiredRange(50, 200)
		);
		this.category = CmdCategory.MANAGE;
		this.adminCommand = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.deferReply().queue();
		StringBuffer response = new StringBuffer();

		long guildId = event.getGuild().getIdLong();

		if (event.hasOption("logs")) {
			GuildChannel channel = event.optGuildChannel("logs");

			try {
				bot.getCheckUtil().hasPermissions(event, event.getGuild(), event.getMember(), true, channel,
					new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS});
			} catch (CheckException ex) {
				event.getHook().editOriginal(ex.getEditData()).queue();
				return;
			}

			bot.getDBUtil().bank.setLogChannel(guildId, channel.getIdLong());
			response.append(lu.getText(event, path+".changed_log").formatted(channel.getAsMention()));
		}
		if (event.hasOption("transfer_cut")) {
			Integer value = event.optInteger("transfer_cut");
			bot.getDBUtil().bank.setTransferCut(guildId, value);
			response.append(lu.getText(event, path+".changed_cut").formatted(value));
		}
		if (event.hasOption("max_transfer")) {
			Integer value = event.optInteger("max_transfer");
			bot.getDBUtil().bank.setMaxTransferAmount(guildId, value);
			response.append(lu.getText(event, path+".changed_transfer_max").formatted(value));
		}
		if (event.hasOption("exchange_rate")) {
			Float value = (float) event.optDouble("exchange_rate");
			bot.getDBUtil().bank.setExchangeRate(guildId, value);
			response.append(lu.getText(event, path+".changed_rate").formatted(value));
		}
		if (event.hasOption("min_exchange")) {
			Integer value = event.optInteger("min_exchange");
			bot.getDBUtil().bank.setExchangeAmountMin(guildId, value);
			response.append(lu.getText(event, path+".changed_exchange_min").formatted(value));
		}
		if (event.hasOption("max_exchange")) {
			Integer value = event.optInteger("max_exchange");
			bot.getDBUtil().bank.setExchangeAmountMax(guildId, value);
			response.append(lu.getText(event, path+".changed_exchange_max").formatted(value));
		}
		
		if (response.isEmpty()) {
			editError(event, path+".no_options");
			return;
		}
		editHookEmbed(event, bot.getEmbedUtil().getEmbed(event)
			.setTitle(lu.getText(event, path+".embed_title"))
			.setDescription(response.toString())
			.setColor(Constants.COLOR_SUCCESS)
			.build());
	}
	
}

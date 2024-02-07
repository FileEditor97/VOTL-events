package votl.events.commands.owner;

import java.util.List;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ForceAmountCmd extends CommandBase {
	
	public ForceAmountCmd(App bot) {
		super(bot);
		this.name = "forceamount";
		this.path = "bot.owner.forceamount";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"), true),
			new OptionData(OptionType.INTEGER, "amount", lu.getText(path+".amount.help")).setRequiredRange(0, 100)
		);
		this.category = CmdCategory.OWNER;
		this.ownerCommand = true;
		this.guildOnly = false;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		long userId = event.optUser("user").getIdLong();
		Integer amount = event.optInteger("amount", 100);
		if (amount == 0) 
			bot.getDBUtil().valentines.purgeUser(userId);
		else
			bot.getDBUtil().valentines.forceAmount(userId, event.optInteger("amount", 100));
		
		event.reply(Constants.SUCCESS+" - "+userId).queue();
	}
}

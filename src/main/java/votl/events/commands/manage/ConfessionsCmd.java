package votl.events.commands.manage;

import java.util.List;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;

public class ConfessionsCmd extends CommandBase {
	
	public ConfessionsCmd(App bot) {
		super(bot);
		this.name = "confessions";
		this.path = "bot.manage.confessions";
		this.children = new SlashCommand[]{new Add(bot), new Remove(bot)};
		this.category = CmdCategory.MANAGE;
		this.adminCommand = true;
	}

	@Override
	protected void execute(SlashCommandEvent event) {}

	private class Add extends SlashCommand {
		public Add(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "add";
			this.path = "bot.manage.confessions.add";
			this.options = List.of(
				new OptionData(OptionType.STRING, "name", lu.getText(path+".name.help"), true).setMaxLength(100),
				new OptionData(OptionType.CHANNEL, "channel", lu.getText(path+".channel.help"), true).setChannelTypes(ChannelType.TEXT)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			event.deferReply().queue();
			String name = event.optString("name");
			MessageChannel channel = event.optMessageChannel("channel");
			int optionId = bot.getDBUtil().confess.createOption(event.getGuild().getIdLong(), channel.getIdLong(), name);
			event.getHook().editOriginal(lu.getText(event, path+".done").formatted(name, optionId)).queue();
		}
	}

	private class Remove extends SlashCommand {
		public Remove(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "remove";
			this.path = "bot.manage.confessions.remove";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "id", lu.getText(path+".id.help"), true).setMinValue(1)
			);
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			event.deferReply().queue();
			Integer optionId = event.optInteger("id");
			Long guildId = bot.getDBUtil().confess.getGuildId(optionId);
			if (guildId == null || guildId != event.getGuild().getIdLong()) {
				editError(event, path+".not_found");
				return;
			}
			bot.getDBUtil().confess.clearOption(optionId);
			bot.getDBUtil().confess.deleteOption(optionId);
			event.getHook().editOriginal(lu.getText(event, path+".done").formatted(optionId)).queue();
		}
	}

}

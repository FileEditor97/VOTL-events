package votl.events.commands.manage;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import votl.events.App;
import votl.events.base.command.SlashCommand;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ConfessionsCmd extends CommandBase {
	
	public ConfessionsCmd(App bot) {
		super(bot);
		this.name = "confessions";
		this.path = "bot.manage.confessions";
		this.children = new SlashCommand[]{new Add(bot), new View(bot), new Remove(bot), new Reset(bot)};
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
			bot.getDBUtil().confess.createOption(event.getGuild().getIdLong(), channel.getIdLong(), name);
			editHook(event, lu.getText(event, path+".done").formatted(name));
		}
	}

	private class View extends SlashCommand {
		public View(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "view";
			this.path = "bot.manage.confessions.view";
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			event.deferReply(true).queue();
			StringBuffer buffer = new StringBuffer();
			bot.getDBUtil().confess.getGuildOptions(event.getGuild().getIdLong()).forEach((k, v) -> buffer.append("%3d | <#%d> | %s\n".formatted(k, bot.getDBUtil().confess.getChannelId(k), v)));
			if (buffer.isEmpty()) {
				editHook(event, lu.getText(event, path+".empty"));
				return;
			}

			editHookEmbed(event, new EmbedBuilder().setColor(Constants.COLOR_DEFAULT)
				.setTitle(lu.getText(event, path+".title"))
				.setDescription(buffer.toString())
				.build());
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
			editHook(event, (lu.getText(event, path+".done").formatted(optionId)));
		}
	}

	private class Reset extends SlashCommand {
		public Reset(App bot) {
			this.bot = bot;
			this.lu = bot.getLocaleUtil();
			this.name = "reset";
			this.path = "bot.manage.confessions.reset";
			this.options = List.of(
				new OptionData(OptionType.INTEGER, "id", lu.getText(path+".id.help"), true).setMinValue(1),
				new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"))
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

			if (event.hasOption("user")) {
				// User
				User user = event.optUser("user");
				bot.getDBUtil().confess.clear(optionId, user.getIdLong());
				editHook(event, (lu.getText(event, path+".done_user").formatted(optionId, user.getAsMention())));
			} else {
				// All
				bot.getDBUtil().confess.clearOption(optionId);
				editHook(event, (lu.getText(event, path+".done_all").formatted(optionId)));
			}
		}
	}

}

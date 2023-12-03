package votl.events.commands.other;

import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;

public class PingCmd extends CommandBase {
	
	public PingCmd(App bot) {
		super(bot);
		this.name = "ping";
		this.path = "bot.other.ping";
		this.category = CmdCategory.OTHER;
		this.guildOnly = false;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		createReply(event, lu.getText(event, path+".loading"));

		event.getJDA().getRestPing().queue(time -> {
			editHook(event,
				lu.getText(event, "bot.other.ping.info_full")
					.replace("{websocket}", event.getJDA().getGatewayPing()+"")
					.replace("{rest}", time+"")
			);
		});	
	}
}

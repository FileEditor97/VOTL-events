package votl.events.commands.owner;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;

public class ShutdownCmd extends CommandBase {

	public ShutdownCmd(App bot) {
		super(bot);
		this.name = "shutdown";
		this.path = "bot.owner.shutdown";
		this.category = CmdCategory.OWNER;
		this.ownerCommand = true;
		this.guildOnly = false;
	}
	
	@Override
	protected void execute(SlashCommandEvent event) {
		createReply(event, "Shutting down...");
		event.getJDA().getPresence().setPresence(OnlineStatus.IDLE, Activity.competing("Shutting down..."));
		bot.getLogger().info("Shutting down, by '%s'".formatted(event.getUser().getName()));
		event.getJDA().shutdown();
	}
}

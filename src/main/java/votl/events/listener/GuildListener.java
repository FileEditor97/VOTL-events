package votl.events.listener;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import votl.events.App;

public class GuildListener extends ListenerAdapter {

	private final App bot;

	public GuildListener(App bot) {
		this.bot = bot;
	}

	@Override
	public void onGuildJoin(@Nonnull GuildJoinEvent event) {
		bot.getDBUtil().bank.createBank(event.getGuild().getIdLong());
		bot.getLogger().info("Joined guild '%s'(%s)".formatted(event.getGuild(), event.getGuild().getId()));
	}

	@Override
	public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
		bot.getLogger().info("Left guild '%s'(%s)".formatted(event.getGuild(), event.getGuild().getId()));
	}
	
}

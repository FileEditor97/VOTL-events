package votl.events.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import votl.events.App;
import votl.events.base.command.SlashCommandEvent;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;

public class StatusCmd extends CommandBase {

	public StatusCmd(App bot) {
		super(bot);
		this.name = "status";
		this.path = "bot.other.status";
		this.category = CmdCategory.OTHER;
		this.guildOnly = false;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		DiscordLocale userLocale = event.getUserLocale();
		EmbedBuilder builder = bot.getEmbedUtil().getEmbed();

		builder.setAuthor(event.getJDA().getSelfUser().getName(), event.getJDA().getSelfUser().getEffectiveAvatarUrl())
			.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
		
		builder.addField(
			lu.getLocalized(userLocale, "bot.other.status.embed.stats_title"),
			String.join(
				"\n",
				lu.getLocalized(userLocale, "bot.other.status.embed.stats.guilds").replace("{value}", String.valueOf(event.getJDA().getGuilds().size())),
				lu.getLocalized(userLocale, "bot.other.status.embed.stats.shard")
					.replace("{this}", String.valueOf(event.getJDA().getShardInfo().getShardId() + 1))
					.replace("{all}", String.valueOf(event.getJDA().getShardInfo().getShardTotal()))
			),
			false
		)
		.addField(lu.getLocalized(userLocale, "bot.other.status.embed.shard_title"),
			String.join(
				"\n",
				lu.getLocalized(userLocale, "bot.other.status.embed.shard.users").replace("{value}", String.valueOf(event.getJDA().getUsers().size())),
				lu.getLocalized(userLocale, "bot.other.status.embed.shard.guilds").replace("{value}", String.valueOf(event.getJDA().getGuilds().size()))
			),
			true
		)
		.addField("",
			String.join(
				"\n",
				lu.getLocalized(userLocale, "bot.other.status.embed.shard.text_channels").replace("{value}", String.valueOf(event.getJDA().getTextChannels().size())),
				lu.getLocalized(userLocale, "bot.other.status.embed.shard.voice_channels").replace("{value}", String.valueOf(event.getJDA().getVoiceChannels().size()))
			),
			true
		);

		builder.setFooter(lu.getLocalized(userLocale, "bot.other.status.embed.last_restart"))
			.setTimestamp(event.getClient().getStartTime());
		
		event.replyEmbeds(builder.build()).setEphemeral(event.isFromGuild()).queue();
	}

}

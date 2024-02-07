package votl.events.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import votl.events.App;
import votl.events.objects.Keyword;

public class MessageListener extends ListenerAdapter {

	private final App bot;

	private Map<Long, List<Keyword>> keywords = new HashMap<>();

	public MessageListener(App bot) {
		this.bot = bot;
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
			if (keywords.isEmpty()) return;

			List<Keyword> list = keywords.get(event.getGuild().getIdLong());
			if (list == null || list.isEmpty()) return;

			String content = event.getMessage().getContentStripped().toLowerCase();
			list.forEach(keyword -> {
				if ( (keyword.isExact() && content.equals(keyword.getTrigger())) || (!keyword.isExact() && content.contains(keyword.getTrigger())) ) {
					try {
						event.getMessage().addReaction(keyword.getEmoji()).queue(null, failure -> {
							new ErrorHandler().ignore(ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.UNKNOWN_MESSAGE);
						});
					} catch (InsufficientPermissionException ex) {
						// Catch and forget
					}
					return;
				}
			});
		}
	}

	public void setupKeywords() {
		bot.JDA.getGuilds().forEach(guild -> {
			List<Keyword> data = bot.getDBUtil().emotes.getEmotes(guild.getIdLong());
			if (!data.isEmpty()) {
				keywords.put(guild.getIdLong(), new ArrayList<>());
				keywords.get(guild.getIdLong()).addAll(data);
			}
		});
	}

	public void addKeyword(Long guildId, String trigger, EmojiUnion emoji, Boolean exact) {
		keywords.putIfAbsent(guildId, new ArrayList<>());
		keywords.get(guildId).add(new Keyword(trigger, exact, emoji));
	}

	// True if existed and removed
	public Boolean removeKeyword(Long guildId, String trigger) {
		List<Keyword> list = keywords.get(guildId);
		if (list != null) {
			for (Keyword keyword : list) {
				if (keyword.getTrigger().equals(trigger)) {
					list.remove(keyword);
					return true;
				}
			}
		}
		return false;
	}

	public Boolean removeKeyword(String trigger) {
		for (List<Keyword> list : keywords.values()) {
			for (Keyword keyword : list) {
				if (keyword.getTrigger().equals(trigger)) {
					list.remove(keyword);
					return true;
				}
			}
		}
		return false;
	}
	
}

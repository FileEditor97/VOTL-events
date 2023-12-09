package votl.events.listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import votl.events.App;

public class MessageListener extends ListenerAdapter {

	private final App bot;

	private Map<Long, Map<String, EmojiUnion>> keywords = new HashMap<>();

	public MessageListener(App bot) {
		this.bot = bot;
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
		if (keywords.isEmpty()) return;

		Map<String, EmojiUnion> map = keywords.get(event.getGuild().getIdLong());
		if (map == null || map.isEmpty()) return;

		String content = event.getMessage().getContentStripped().toLowerCase();
		map.entrySet().forEach(set -> {
			if (content.contains(set.getKey())) {
				event.getMessage().addReaction(set.getValue()).queue(null, failure -> {
					new ErrorHandler().ignore(ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.UNKNOWN_MESSAGE);
				});
				return;
			}
		});
	}

	public void setupKeywords() {
		bot.JDA.getGuilds().forEach(guild -> {
			Map<String, String> data = bot.getDBUtil().emotes.getEmotes(guild.getIdLong());
			if (!data.isEmpty()) {
				Map<String, EmojiUnion> map = new HashMap<>();
				data.forEach((k, v) -> {
					map.put(k, Emoji.fromFormatted(v));
				});
				keywords.put(guild.getIdLong(), map);
			}
		});
	}

	public void addKeyword(Long guildId, String trigger, EmojiUnion emoji) {
		keywords.putIfAbsent(guildId, new HashMap<>());
		keywords.get(guildId).put(trigger, emoji);
	}

	// True if existed and removed
	public Boolean removeKeyword(Long guildId, String trigger) {
		Map<String, EmojiUnion> map = keywords.get(guildId);
		if (map != null) {
			if (map.remove(trigger) != null) 
				return true;
		}
		return false;
	}

	public Boolean removeKeyword(String trigger) {
		for (Map<String, EmojiUnion> map : keywords.values()) {
			if (map.remove(trigger) != null)
				return true;
		}
		return false;
	}
	
}

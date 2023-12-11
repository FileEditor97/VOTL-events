package votl.events.utils.database.managers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import votl.events.objects.Keyword;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class EmoteManager extends SQLiteBase {
	
	private final String table = "emoteKeywords";

	public EmoteManager(ConnectionUtil cu) {
		super(cu);
	}

	public void addEmote(long guildId, String trigger, String emote, Long userId, Instant until, Boolean exact) {
		execute("INSERT INTO %s(guildId, trigger, emote, userId, until, exact) VALUES(%d, %s, %s, %d, %d, %d)"
			.formatted(table, guildId, quote(trigger), quote(emote), Optional.ofNullable(userId).orElse(0L), until.getEpochSecond(), exact?1:0));
	}

	public void addEmote(long guildId, String trigger, String emote, Boolean exact) {
		execute("INSERT INTO %s(guildId, trigger, emote, userId, exact) VALUES(%d, %s, %s, %d, %d)"
			.formatted(table, guildId, quote(trigger), quote(emote), 0L, exact?1:0));
	}

	public void deleteEmote(String trigger) {
		execute("DELETE FROM %s WHERE (trigger=%s)".formatted(table, quote(trigger)));
	}

	public void deleteEmote(int keywordId) {
		execute("DELETE FROM %s WHERE (id=%d)".formatted(table, keywordId));
	}

	public List<Keyword> getEmotes(long guildId) {
		final String sql = "SELECT trigger, emote, exact FROM %s WHERE (guildId=%d);".formatted(table, guildId);
		List<Map<String, Object>> data = select(sql, List.of("trigger", "emote", "exact"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> new Keyword((String) map.get("trigger"), ((int) map.getOrDefault("exact", 0))!=0, (String) map.get("emote"))).toList();
	}

	public Map<String, Object> getInfo(int keywordId) {
		final String sql = "SELECT * FROM %s WHERE (id=%d);".formatted(table, keywordId);
		Map<String, Object> data = selectOne(sql, List.of("guildId", "trigger", "emote", "userId", "until"));
		if (data == null || data.isEmpty()) return null;
		return data;
	}

	public String getTrigger(int keywordId) {
		return selectOne("SELECT trigger FROM %s WHERE (id=%d);".formatted(table, keywordId), "trigger", String.class);
	}

	public Integer getId(String trigger) {
		return selectOne("SELECT id FROM %s WHERE (trigger=%s);".formatted(table, quote(trigger)), "id", Integer.class);
	}

	public List<Integer> getExpired(Instant currentTime) {
		final String sql = "SELECT id FROM %s WHERE (until<%d);".formatted(table, currentTime.getEpochSecond());
		return select(sql, "id", Integer.class);
	}

	public Map<String, Object> getUserEmote(long guildId, long userId) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId);
		Map<String, Object> data = selectOne(sql, List.of("guildId", "trigger", "emote", "userId", "until"));
		if (data == null || data.isEmpty()) return null;
		return data;
	}

}

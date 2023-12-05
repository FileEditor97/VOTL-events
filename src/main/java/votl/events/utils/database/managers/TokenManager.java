package votl.events.utils.database.managers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class TokenManager extends SQLiteBase {
	
	private final String table = "eventTokens";

	public TokenManager(ConnectionUtil util) {
		super(util);
	}

	public void addTokens(long guildId, long userId, int tokenAmount, Instant currentTime) {
		execute(("INSERT INTO %s(guildId, userId, tokens, lastUpdated) VALUES(%d, %d, %d, %d)"+
			"ON CONFLICT(guildId, userId) DO UPDATE SET tokens=tokens+%d, lastUpdated=%d;")
			.formatted(table, guildId, userId, tokenAmount, currentTime.getEpochSecond(), tokenAmount, currentTime.getEpochSecond()));
	}

	public Integer getTokens(long guildId, long userId) {
		Integer data = selectOne("SELECT tokens FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId), "tokens", Integer.class);
		return data==null ? 0 : data;
	}

	public void removeGuildUser(long guildId, long userId) {
		execute("DELETE FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId));
	}

	public void removeGuild(long guildId) {
		execute("DELETE FROM %s WHERE (guildId=%d);".formatted(table, guildId));
	}

	public void removeUser(long userId) {
		execute("DELETE FROM %s WHERE (userId=%d);".formatted(table, userId));
	}

	// Leaderboard
	public List<Pair<Long, Integer>> getTopAmount(long guildId) {
		List<Map<String, Object>> data = select("SELECT userId, tokens FROM %s WHERE (guildId=%d AND tokens>0) ORDER BY tokens DESC LIMIT 10;".formatted(table, guildId), List.of("userId", "tokens"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> Pair.of(((Number) map.get("userId")).longValue(), (Integer) map.get("tokens"))).toList();
	}

}

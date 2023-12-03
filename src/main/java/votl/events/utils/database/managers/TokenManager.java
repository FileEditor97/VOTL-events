package votl.events.utils.database.managers;

import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class TokenManager extends SQLiteBase {
	
	private final String table = "eventTokens";

	public TokenManager(ConnectionUtil util) {
		super(util);
	}

	public void addTokens(long guildId, long userId, int tokenAmount, long epochSeconds) {
		execute(("INSERT INTO %s(guildId, userId, tokens, lastUpdated) VALUES(%d, %d, %d, %d)"+
			"ON CONFLICT(guildId, userId) DO UPDATE SET tokens=tokens+%d, lastUpdated=%d;")
			.formatted(table, guildId, userId, tokenAmount, epochSeconds, tokenAmount, epochSeconds));
	}

	public Integer getTokens(Long guildId, Long userId) {
		Integer data = selectOne("SELECT tokens FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId), "tokens", Integer.class);
		return data==null ? 0 : data;
	}

	public void removeGuildUser(Long guildId, Long userId) {
		execute("DELETE FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId));
	}

	public void removeGuild(Long guildId) {
		execute("DELETE FROM %s WHERE (guildId=%d);".formatted(table, guildId));
	}

	public void removeUser(Long userId) {
		execute("DELETE FROM %s WHERE (userId=%d);".formatted(table, userId));
	}

}

package votl.events.utils.database.managers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.objects.ActionLog.EventAction;
import votl.events.objects.ActionLog;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class TokenUpdatesManager extends SQLiteBase {

	private final String table = "tokenUpdates";
	
	public TokenUpdatesManager(ConnectionUtil util) {
		super(util);
	}

	public void logAction(long guildId, long targetId, Long creatorId, Instant currentTime, EventAction actionType, Integer tokenAmount, String reason) {
		execute("INSERT INTO %s(guildId, targetId, creatorId, datetime, type, tokenAmount, reason) VALUES(%d, %d, %d, %d, %d, %d, %s)"
			.formatted(table, guildId, targetId, Optional.ofNullable(creatorId).orElse(0L), currentTime.getEpochSecond(), actionType.getType(), tokenAmount, quote(reason)));
	}

	// Get user's last log
	public ActionLog getUserLastLog(long guildId, long targetId) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d AND targetId=%d) ORDER BY id DESC LIMIT 1;".formatted(table, guildId, targetId);
		Map<String, Object> data = selectOne(sql, List.of("id", "guildId", "targetId", "creatorId", "datetime", "type", "tokenAmount", "reason"));
		if (data == null || data.isEmpty()) return null;
		return new ActionLog(data);
	}

	// Get log by ID
	public ActionLog getLogById(int id) {
		final String sql = "SELECT * FROM %s WHERE (id=%d);".formatted(table, id);
		Map<String, Object> data = selectOne(sql, List.of("id", "guildId", "targetId", "creatorId", "datetime", "type", "tokenAmount", "reason"));
		if (data == null || data.isEmpty()) return null;
		return new ActionLog(data);
	}

	// Get user's logs
	public List<ActionLog> getUserLogs(long guildId, long targetId, int offset, int count) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d AND targetId=%d) ORDER BY id DESC LIMIT %d, %d;".formatted(table, guildId, targetId, offset, count);
		List<Map<String, Object>> data = select(sql, List.of("id", "guildId", "targetId", "creatorId", "datetime", "type", "tokenAmount", "reason"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> new ActionLog(map)).toList();
	}

	// Get logs in guild
	public List<ActionLog> getLogs(long guildId, int offset, int count) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d) ORDER BY id DESC LIMIT %d, %d;".formatted(table, guildId, offset, count);
		List<Map<String, Object>> data = select(sql, List.of("id", "guildId", "targetId", "creatorId", "datetime", "type", "tokenAmount", "reason"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> new ActionLog(map)).toList();
	}

	// Count all user's logs
	public Integer countUserLogs(long guildId, long targetId) {
		return selectOne("SELECT COUNT(*) AS counted FROM %s WHERE (guildId=%d AND targetId=%d);".formatted(table, guildId, targetId), "counted", Integer.class);
	}

	// Count all logs in guild
	public Integer countLogs(long guildId) {
		return selectOne("SELECT COUNT(*) AS counted FROM %s WHERE (guildId=%d);".formatted(table, guildId), "counted", Integer.class);
	}

	// Leaderboard
	public List<Pair<Long, Integer>> getTopEarned(long guildId) {
		final String sql = "SELECT targetId, SUM(tokenAmount) AS earned FROM %s WHERE (guildId=%d AND type IN (0)) GROUP BY targetId ORDER BY earned DESC LIMIT 10;".formatted(table, guildId);
		List<Map<String, Object>> data = select(sql, List.of("targetId", "earned"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> Pair.of(((Number) map.get("targetId")).longValue(), (Integer) map.get("earned"))).toList();
	}

	public List<Pair<Long, Integer>> getTopSpend(long guildId) {
		final String sql = "SELECT targetId, -SUM(tokenAmount) AS spend FROM %s WHERE (guildId=%d AND type IN (3, 4)) GROUP BY targetId ORDER BY spend DESC LIMIT 10;".formatted(table, guildId);
		List<Map<String, Object>> data = select(sql, List.of("targetId", "spend"));
		if (data == null || data.isEmpty()) return List.of();
		return data.stream().map(map -> Pair.of(((Number) map.get("targetId")).longValue(), (Integer) map.get("spend"))).toList();
	}

	// For limits checking
	public Integer getTransferedAmount(long guildId, long creatorId, Instant currentTime) {
		Integer data = selectOne("SELECT SUM(tokenAmount) AS transfered FROM %s WHERE (guildId=%d AND type=2 AND creatorId=%d AND datetime>%d);"
				.formatted(table, guildId, creatorId, currentTime.minus(30, ChronoUnit.DAYS).getEpochSecond()),
			"transfered", Integer.class);
		return data==null ? 0 : data;
	}

	public Integer getExchangedAmount(long guildId, long targetId, Instant currentTime) {
		Integer data = selectOne("SELECT SUM(tokenAmount) AS exchanged FROM %s WHERE (guildId=%d AND type=4 AND targetId=%d AND datetime>%d);"
				.formatted(table, guildId, targetId, currentTime.minus(30, ChronoUnit.DAYS).getEpochSecond()),
			"exchanged", Integer.class);
		return data==null ? 0 : data;
	}
	
}

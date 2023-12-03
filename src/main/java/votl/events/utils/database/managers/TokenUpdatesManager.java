package votl.events.utils.database.managers;

import java.util.List;
import java.util.Map;

import votl.events.objects.EventActions;
import votl.events.objects.EventLog;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class TokenUpdatesManager extends SQLiteBase {

	private final String table = "tokenUpdates";
	
	public TokenUpdatesManager(ConnectionUtil util) {
		super(util);
	}

	public void logAction(Long guildId, Long targetId, Long modId, Long epochSeconds, EventActions actionType, String data) {
		execute("INSERT INTO %s(guildId, targetId, modId, datetime, type, data) VALUES(%d, %d, %d, %d, %d, %s)"
			.formatted(table, guildId, targetId, modId, epochSeconds, actionType.getType(), quote(data)));
	}

	// Get user's last log
	public EventLog getUserLastLog(Long guildId, Long targetId) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d AND targetId=%d) ORDER BY id DESC LIMIT 1;".formatted(table, guildId, targetId);
		Map<String, Object> data = selectOne(sql, List.of("id", "guildId", "targetId", "modId", "datetime", "type", "data"));
		if (data == null || data.isEmpty()) return null;
		return new EventLog(data);
	}

	// Get log by ID
	public EventLog getLogById(Integer id) {
		final String sql = "SELECT * FROM %s WHERE (id=%d);".formatted(table, id);
		Map<String, Object> data = selectOne(sql, List.of("id", "guildId", "targetId", "modId", "datetime", "type", "data"));
		if (data == null || data.isEmpty()) return null;
		return new EventLog(data);
	}

	// Get user's logs
	public List<EventLog> getUserLogs(Long guildId, Long targetId, int offset, int count) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d AND targetId=%d) ORDER BY id DESC LIMIT %d, %d;".formatted(table, guildId, targetId, offset, count);
		List<Map<String, Object>> data = select(sql, List.of("id", "guildId", "targetId", "modId", "datetime", "type", "data"));
		if (data == null || data.isEmpty()) return null;
		return data.stream().map(map -> new EventLog(map)).toList();
	}

	// Get logs in guild
	public List<EventLog> getLogs(Long guildId, int offset, int count) {
		final String sql = "SELECT * FROM %s WHERE (guildId=%d) ORDER BY id DESC LIMIT %d, %d;".formatted(table, guildId, offset, count);
		List<Map<String, Object>> data = select(sql, List.of("id", "guildId", "targetId", "modId", "datetime", "type", "data"));
		if (data == null || data.isEmpty()) return null;
		return data.stream().map(map -> new EventLog(map)).toList();
	}

	// Count all user's logs
	public Integer countUserLogs(Long guildId, Long userId) {
		return selectOne("SELECT COUNT(*) AS counted FROM %s WHERE (guildId=%d AND userId=%d);".formatted(table, guildId, userId), "counted", Integer.class);
	}

	// Count all logs in guild
	public Integer countLogs(Long guildId) {
		return selectOne("SELECT COUNT(*) AS counted FROM %s WHERE (guildId=%d);".formatted(table, guildId), "counted", Integer.class);
	}
	
}

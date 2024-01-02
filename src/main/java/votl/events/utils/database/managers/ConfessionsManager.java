package votl.events.utils.database.managers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class ConfessionsManager extends SQLiteBase {
	
	private final String table_users = "confessed";
	private final String table_options = "confessionOptions";

	public ConfessionsManager(ConnectionUtil cu) {
		super(cu);
	}

	public void createOption(long guildId, long channelId, String name) {
		execute("INSERT INTO %s(guildId, channelId, name) VALUES(%d, %d, %s);".formatted(table_options, guildId, channelId, quote(name)));
		//return lastId();
	}

	public void deleteOption(int id) {
		execute("DELETE FROM %s WHERE (id=%d);".formatted(table_options, id));
	}

	public void deleteGuild(long guildId) {
		execute("DELETE FROM %s WHERE (guildId=%d);".formatted(table_options, guildId));
	}

	public Map<Integer, String> getGuildOptions(long guildId) {
		final String sql = "SELECT id, name FROM %s WHERE (guildId=%d);".formatted(table_options, guildId);
		List<Map<String, Object>> data = select(sql, List.of("id", "name"));
		if (data == null || data.isEmpty()) return Map.of();
		return data.stream().collect(Collectors.toMap(s -> (Integer) s.get("id"), s -> (String) s.getOrDefault("name", "")));
	}

	public Map<Integer, String> getAllOptions() {
		final String sql = "SELECT id, name FROM %s;".formatted(table_options);
		List<Map<String, Object>> data = select(sql, List.of("id", "name"));
		if (data == null || data.isEmpty()) return Map.of();
		return data.stream().collect(Collectors.toMap(s -> (Integer) s.get("id"), s -> (String) s.getOrDefault("name", "")));
	}

	public Long getChannelId(int id) {
		Long data = selectOne("SELECT channelId FROM %s WHERE (id=%d);".formatted(table_options, id), "channelId", Long.class);
		return data;
	}

	public Long getGuildId(int id) {
		Long data = selectOne("SELECT guildId FROM %s WHERE (id=%d);".formatted(table_options, id), "guildId", Long.class);
		return data;
	}

	public String getName(int id) {
		String data = selectOne("SELECT name FROM %s WHERE (id=%d);".formatted(table_options, id), "name", String.class);
		return data;
	}


	public void confessed(long userId, int optionId, Instant time) {
		execute("INSERT INTO %s(userId, optionId, timeCreated) VALUES(%d, %d, %d);".formatted(table_users, userId, optionId, time.getEpochSecond()));
	}

	public void clear(long optionId, long userId) {
		execute("DELETE FROM %s WHERE (userId=%d AND optionId=%d);".formatted(table_users, userId, optionId));
	}

	public void clearUser(long userId) {
		execute("DELETE FROM %s WHERE (userId=%d);".formatted(table_users, userId));
	}

	public void clearOption(long optionId) {
		execute("DELETE FROM %s WHERE (optionId=%d);".formatted(table_users, optionId));
	}

	public void clearAll() {
		execute("DELETE FROM %s;".formatted(table_users));
	}

	public Boolean hasConfessed(long userId, int optionId) {
		if (selectOne("SELECT optionId FROM %s WHERE (userId=%d AND optionId=%d);".formatted(table_users, userId, optionId), "optionId", Integer.class) == null) return false;
		return true;
	}

}

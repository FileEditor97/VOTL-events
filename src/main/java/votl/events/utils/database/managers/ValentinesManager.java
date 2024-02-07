package votl.events.utils.database.managers;

import java.time.Instant;

import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class ValentinesManager extends SQLiteBase {
	
	private final String table = "valentines";

	public ValentinesManager(ConnectionUtil cu) {
		super(cu);
	}

	public void addValentine(long userId, Instant time) {
		execute("INSERT INTO %s(userId, amount, lastSent) VALUES (%d, 1, %d) ON CONFLICT(userId) DO UPDATE SET amount=amount+1, lastSent=%<d;".formatted(table, userId, time.getEpochSecond()));
	}

	public int countValentines(long userId) {
		Integer data = selectOne("SELECT amount FROM %s WHERE (userId=%d);".formatted(table, userId), "amount", Integer.class);
		return data==null ? 0 : data;
	}

	public void purgeUser(long userId) {
		execute("DELETE FROM %s WHERE (userId=%d);".formatted(table, userId));
	}

	public void purgeAll() {
		execute("DELETE FROM %s;".formatted(table));
	}

	public void forceAmount(long userId, int amount) {
		execute("UPDATE %s SET amount=%d WHERE (userId=%d)".formatted(table, amount, userId));
	}

}

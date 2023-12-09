package votl.events.utils.database.managers;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.objects.ShopItem;
import votl.events.objects.ShopItem.ItemType;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class ItemsManager extends SQLiteBase {

	private final String table = "items";
	
	public ItemsManager(ConnectionUtil cu) {
		super(cu);
	}

	public void addItem(long guildId, String name, String description, int price, int amount, ItemType type, JSONObject data) {
		execute("INSERT INTO %s(guildId, name, description, price, amount, type, data) VALUES(%d, %s, %s, %d, %d, %d, %s);"
			.formatted(table, guildId, quote(name), quote(description), price, amount, type.getType(), quote(data.isEmpty() ? null : data.toString())));
	}

	public void removeItem(int id) {
		execute("DELETE FROM %s WHERE (id=%d);".formatted(table, id));
	}

	public void removeAllItems(long guildId) {
		execute("DELETE FROM %s WHERE (guildId=%d);".formatted(table, guildId));
	}

	public String getName(int id) {
		return selectOne("SELECT name FROM %s WHERE (id=%d);".formatted(table, id), "name", String.class);
	}

	public ShopItem getItem(int id) {
		Map<String, Object> data = selectOne("SELECT * FROM %s WHERE (id=%d);".formatted(table, id), List.of("id", "guildId", "name", "description", "price", "amount", "type", "data"));
		if (data.isEmpty()) return null;
		return new ShopItem(data);
	}

	public List<ShopItem> getItems(long guildId) {
		List<Map<String, Object>> data = select("SELECT * FROM %s WHERE (guildId=%d);".formatted(table, guildId), List.of("id", "guildId", "name", "description", "price", "amount", "type", "data"));
		if (data.isEmpty()) return List.of();
		return data.stream().map(map -> new ShopItem(map)).toList();
	}

	public List<Pair<Integer, String>> getItemsShort(long guildId) {
		List<Map<String, Object>> data = select("SELECT id, name FROM %s WHERE (guildId=%d);".formatted(table, guildId), List.of("id", "name"));
		if (data.isEmpty()) return List.of();
		return data.stream().map(map -> Pair.of((Integer) map.get("id"), (String) map.get("name"))).toList();
	}

	public void setPrice(int id, int price) {
		execute("UPDATE %s SET price=%d WHERE (id=%d);".formatted(table, price, id));
	}

	public void setAmount(int id, int amount) {
		execute("UPDATE %s SET amount=%d WHERE (id=%d);".formatted(table, amount, id));
	}

	public void changeAmount(int id, int amountChange) {
		execute("UPDATE %s SET amount=amount+%d WHERE (id=%d);".formatted(table, amountChange, id));
	}

	public Integer getAmount(int id) {
		Integer data = selectOne("SELECT amount FROM %s WHERE (id=%d);".formatted(table, id), "amount", Integer.class);
		return data==null ? 0 : data;
	}


}

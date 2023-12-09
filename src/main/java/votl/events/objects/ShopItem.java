package votl.events.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import votl.events.utils.message.LocaleUtil;

public class ShopItem {

	private final String emoji = Emotes.RISE_TOKEN.getEmote();

	private final int id;
	private final long guildId;
	private final String name;
	private final String description;
	private final int price;
	private final int amount;
	private final ItemType type;
	private final JSONObject data;
	
	public ShopItem(@Nonnull Map<String, Object> map) {
		this.id = (int) map.get("id");
		this.guildId = ((Number) map.get("guildId")).longValue();
		this.name = (String) map.get("name");
		this.description = (String) map.get("description");
		this.price = (int) map.get("price");
		this.amount = (int) map.getOrDefault("amount", 0);
		this.type = ItemType.byType((Integer) map.get("type"));
		this.data = Optional.ofNullable((String) map.get("data")).map(v -> new JSONObject(v)).orElse(null);
	}

	public int getId() {
		return id;
	}

	public long getGuildId() {
		return guildId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}

	public int getAmount() {
		return amount;
	}

	public ItemType getType() {
		return type;
	}

	public JSONObject getData() {
		return data;
	}

	public String formatted(DiscordLocale locale, LocaleUtil lu) {
		int time = getTime();
		switch (type) {
			case ITEM:
			case ROLE:
			case EMOTE:
				if (amount == 0)
					return "> `%d` | ~~%s~~\n[ %s %s ]\n**%s**\n\n".formatted(id, name, price, emoji, lu.getLocalized(locale, "items.none"));
				else if (amount == -1)
					return "> `%d` | %s\n[ %s %s ]\n\n".formatted(id, name, price, emoji);
				else
					return "> `%d` | %s\n[ %s %s ]\n%s: **%d**\n\n".formatted(id, name, price, emoji, lu.getLocalized(locale, "items.available"), amount);
			case EXCHANGE:
				return "> `%d` | %s\n[ 1 %s => {rate} Coins ]\n\n".formatted(id, name, emoji);
			case ROLE_CUSTOM:
			case VOICE_CHANNEL:
				if (amount == 0)
					return "> `%d` | ~~%s~~\n[ %s %s / %d %s ]\n**%s**\n\n".formatted(id, name, price, emoji, time,
						time > 1 ? lu.getLocalized(locale, "items.weeks") : lu.getLocalized(locale, "items.week"),
						lu.getLocalized(locale, "items.none"));
				else if (amount == -1)
					return "> `%d` | %s\n[ %s %s / %d %s ]\n\n".formatted(id, name, price, emoji, time,
						time > 1 ? lu.getLocalized(locale, "items.weeks") : lu.getLocalized(locale, "items.week"));
				else
					return "> `%d` | %s\n[ %s %s / %d %s ]\n%s: **%d**\n\n".formatted(id, name, price, emoji, time,
						time > 1 ? lu.getLocalized(locale, "items.weeks") : lu.getLocalized(locale, "items.week"),
						lu.getLocalized(locale, "items.available"), amount);
			case ROLE_EXTEND:
			case VOICE_EXTEND:
				return "> `%d` | %s\n[ %s %s / %d %s ]\n\n".formatted(id, name, price, emoji, time,
					time > 1 ? lu.getLocalized(locale, "items.weeks") : lu.getLocalized(locale, "items.week"));
			default:
				return "ERROR item ID %d\n\n".formatted(id);
		}
	}

	private int getTime() {
		if (data == null) return 0;
		try{
			return data.getInt("time");
		} catch(JSONException ex) {
			return 0;
		}
		
	}

	public enum ItemType{
		ITEM(0, "Custom item"),							// none
		EXCHANGE(1, "Exchange"),							// REMOVE?
		ROLE(2, "Role"),									// roleId, duration
		ROLE_CUSTOM(3, "Custom role"),					// duration
		ROLE_EXTEND(4, "Extend role duration"),			// duration
		VOICE_CHANNEL(5, "Voice channel"),				// channel where to create, duration
		VOICE_EXTEND(6, "Extend voice channel duration"),	// duration
		EMOTE(7, "Reaction emote");						// duration

		private final Integer type;
		private final String name;

		private static final Map<Integer, ItemType> BY_TYPE = new HashMap<Integer, ItemType>();

		static {
			for (ItemType itemType : ItemType.values()) {
				BY_TYPE.put(itemType.getType(), itemType);
			}
		}

		ItemType(Integer type, String name) {
			this.type = type;
			this.name = name;
		}

		public Integer getType() {
			return this.type;
		}
	
		public String getName() {
			return this.name;
		}
	
		public static ItemType byType(Integer type) {
			return BY_TYPE.get(type);
		}

		public static List<Choice> getAsChoices() {
			List<Choice> list = new ArrayList<>();
			for (ItemType value : values()) {
				list.add(new Choice(value.getName(), value.getType()));
			}
			return list;
		}
	}
	
}

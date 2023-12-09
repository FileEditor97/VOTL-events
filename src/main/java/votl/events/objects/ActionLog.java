package votl.events.objects;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

public class ActionLog {
	
	private final int id;
	private final long guildId;
	private final long targetId;
	private final long creatorId;
	private final Instant time;
	private final EventAction eventType;
	private final int tokenAmount;
	private final String reason;

	public ActionLog(@NotNull Map<String, Object> map) {
		this.id = (Integer) map.get("id");
		this.guildId = ((Number) map.get("guildId")).longValue();
		this.targetId = ((Number) map.get("targetId")).longValue();
		this.creatorId = ((Number) map.get("creatorId")).longValue();
		this.time = Instant.ofEpochSecond(((Number) map.get("datetime")).longValue());
		this.eventType = EventAction.byType((Integer) map.get("type"));
		this.tokenAmount = (int) map.get("tokenAmount");
		this.reason = (String) map.get("reason");
	}

	public Integer getId() {
		return id;
	}

	public Long getGuildId() {
		return guildId;
	}

	public Long getTargetId() {
		return targetId;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public Instant getTime() {
		return time;
	}

	public EventAction getEventType() {
		return eventType;
	}

	public int getTokenAmount() {
		return tokenAmount;
	}

	public String getReason() {
		return reason;
	}

	public enum EventAction{
		ADD_TOKENS(0, "Add tokens"),
		REMOVE_TOKENS(1, "Remove tokens"),
		TRANSFER(2, "Transfer tokens"),
		BUY_ITEM(3, "Buy item"),
		EXCHANGED(4, "Exchanged for Coins");

		private final Integer type;
		private final String name;

		private static final Map<Integer, EventAction> BY_TYPE = new HashMap<Integer, EventAction>();

		static {
			for (EventAction eventAction : EventAction.values()) {
				BY_TYPE.put(eventAction.getType(), eventAction);
			}
		}
	
		EventAction(Integer type, String name) {
			this.type = type;
			this.name = name;
		}

		public Integer getType() {
			return this.type;
		}
	
		public String getName() {
			return this.name;
		}
	
		public static EventAction byType(Integer type) {
			return BY_TYPE.get(type);
		}
	}

}

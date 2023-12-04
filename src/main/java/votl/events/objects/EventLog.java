package votl.events.objects;

import java.time.Instant;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class EventLog {
	
	private final int id;
	private final long guildId;
	private final long targetId;
	private final long creatorId;
	private final Instant time;
	private final EventActions eventType;
	private final int tokenAmount;
	private final String reason;

	public EventLog(@NotNull Map<String, Object> map) {
		this.id = (Integer) map.get("id");
		this.guildId = ((Number) map.get("guildId")).longValue();
		this.targetId = ((Number) map.get("targetId")).longValue();
		this.creatorId = ((Number) map.get("creatorId")).longValue();
		this.time = Instant.ofEpochSecond(((Number) map.get("datetime")).longValue());
		this.eventType = EventActions.byType((Integer) map.get("type"));
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

	public EventActions getEventType() {
		return eventType;
	}

	public int getTokenAmount() {
		return tokenAmount;
	}

	public String getReason() {
		return reason;
	}

}

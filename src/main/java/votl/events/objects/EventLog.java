package votl.events.objects;

import java.time.Instant;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class EventLog {
	
	private final Integer id;
	private final long guildId;
	private final long targetId;
	private final long modId;
	private final Instant time;
	private final EventActions eventType;
	private String data;

	public EventLog(@NotNull Map<String, Object> map) {
		this.id = (Integer) map.get("id");
		this.guildId = ((Number) map.get("guildId")).longValue();
		this.targetId = ((Number) map.get("targetId")).longValue();
		this.modId = ((Number) map.get("modId")).longValue();
		this.time = Instant.ofEpochSecond(((Number) map.get("datetime")).longValue());
		this.eventType = EventActions.byType((Integer) map.get("type"));
		this.data = (String) map.get("data");
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

	public Long getModId() {
		return modId;
	}

	public Instant getTime() {
		return time;
	}

	public EventActions getEventType() {
		return eventType;
	}

	public String getData() {
		return data;
	}

}

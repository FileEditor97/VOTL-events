package votl.events.objects;

import java.time.Instant;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class EventLog {
	
	private final Integer id;
	private final Long guildId;
	private final Long targetId;
	private final Long modId;
	private final Instant time;
	private final EventActions eventType;
	private String data;

	public EventLog(@NotNull Map<String, Object> map) {
		this.id = (Integer) map.get("id");
		this.guildId = ((Long) map.get("guildId")).longValue();
		this.targetId = ((Long) map.get("targetId")).longValue();
		this.modId = ((Long) map.get("modId")).longValue();
		this.time = Instant.ofEpochSecond(((Long) map.get("datetime")).longValue());
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

package votl.events.objects;

import java.util.HashMap;
import java.util.Map;

public enum EventActions {
	ADD_TOKENS(0, "Add tokens"),			// +15:Reason
	REMOVE_TOKENS(1, "Remove tokens"),	// -15:Reason
	TRANSFER(2, "Transfer tokens"),		// target_user_id
	BUY_ITEM(3, "Buy item"),				// item_id
	EXCHANGED(4, "Exchanged for Coins");
	
	private final Integer type;
	private final String name;

	private static final Map<Integer, EventActions> BY_TYPE = new HashMap<Integer, EventActions>();

	static {
		for (EventActions eventAction : EventActions.values()) {
			BY_TYPE.put(eventAction.getType(), eventAction);
		}
	}

	EventActions(Integer type, String name) {
		this.type = type;
		this.name = name;
	}

	public Integer getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public static EventActions byType(Integer type) {
		return BY_TYPE.get(type);
	}
}
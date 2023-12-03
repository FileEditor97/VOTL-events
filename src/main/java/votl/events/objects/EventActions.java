package votl.events.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EventActions {
	ADD_TOKENS(0, "Add tokens", "(\\+\\d+):(.+)"),
	REMOVE_TOKENS(1, "Remove tokens", "(-\\d+):(.+)");
	
	private final Integer type;
	private final String name;
	private final Pattern pattern;

	private static final Map<Integer, EventActions> BY_TYPE = new HashMap<Integer, EventActions>();

	static {
		for (EventActions eventAction : EventActions.values()) {
			BY_TYPE.put(eventAction.getType(), eventAction);
		}
	}

	EventActions(Integer type, String name, String regex) {
		this.type = type;
		this.name = name;
		this.pattern = Pattern.compile(regex);
	}

	public Integer getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	public Matcher matches(String text) {
		return pattern.matcher(text);
	}

	public static EventActions byType(Integer type) {
		return BY_TYPE.get(type);
	}
}
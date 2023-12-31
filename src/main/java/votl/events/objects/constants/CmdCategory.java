package votl.events.objects.constants;

import votl.events.base.command.Category;

public class CmdCategory {
	private CmdCategory() {
		throw new IllegalStateException("Utility class");
	}

	public static final Category OWNER = new Category("owner");
	public static final Category TOKENS = new Category("tokens");
	public static final Category MANAGE = new Category("manage");
	public static final Category SHOP = new Category("shop");
	public static final Category EVENTS = new Category("events");
	public static final Category OTHER = new Category("other");
}

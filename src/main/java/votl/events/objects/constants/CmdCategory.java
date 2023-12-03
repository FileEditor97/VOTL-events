package votl.events.objects.constants;

import votl.events.base.command.Category;

public class CmdCategory {
	private CmdCategory() {
		throw new IllegalStateException("Utility class");
	}

	public static final Category OWNER = new Category("owner");
	public static final Category TOKENS = new Category("tokens");
	public static final Category OTHER = new Category("other");
}

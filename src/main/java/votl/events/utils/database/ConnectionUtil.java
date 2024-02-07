package votl.events.utils.database;

import org.slf4j.Logger;

public class ConnectionUtil {
	
	private final String urlSQLite;

	protected final Logger logger;

	protected ConnectionUtil(String urlSQLite, Logger logger) {
		this.urlSQLite = urlSQLite;
		this.logger = logger;
	}

	protected String getUrlSQLite() {
		return urlSQLite;
	}

}

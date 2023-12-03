package votl.events.utils.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;

public class ConnectionUtil {
	
	private final String urlSQLite;

	protected final Logger logger;

	protected ConnectionUtil(String urlSQLite, Logger logger) {
		this.urlSQLite = urlSQLite;
		this.logger = logger;
	}

	protected Connection connectSQLite() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(urlSQLite);
		} catch (SQLException ex) {
			logger.error("SQLite: Connection error to database", ex);
			return null;
		}
		return conn;
	}

	protected PreparedStatement prepareStatement(final String sql) throws SQLException {
		return connectSQLite().prepareStatement(sql);
	}

}

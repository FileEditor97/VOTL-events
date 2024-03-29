package votl.events.utils.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteBase {

	private final ConnectionUtil util;

	protected SQLiteBase(ConnectionUtil connectionUtil) {
		this.util = connectionUtil;
	}

	// SELECT sql
	protected <T> T selectOne(final String sql, String selectKey, Class<T> className) {
		T result = null;

		util.logger.debug(sql);
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
			PreparedStatement st = conn.prepareStatement(sql)) {
			ResultSet rs = st.executeQuery();
			
			try {
				if (rs.next()) result = rs.getObject(selectKey, className);
			} catch (SQLException ex) {
				if (!rs.wasNull()) throw ex;
			}
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at SELECT\nrequest: {}", sql, ex);
		}
		return result;
	}

	protected <T> List<T> select(final String sql, String selectKey, Class<T> className) {
		List<T> results = new ArrayList<>();

		util.logger.debug(sql);
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
		PreparedStatement st = conn.prepareStatement(sql)) {
			ResultSet rs = st.executeQuery();
			
			while (rs.next()) {
				try {
					results.add(rs.getObject(selectKey, className));
				} catch (SQLException ex) {
					if (!rs.wasNull()) throw ex;
				}
			}
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at SELECT\nrequest: {}", sql, ex);
		}
		return results;
	}

	protected Map<String, Object> selectOne(final String sql, final List<String> selectKeys) {
		Map<String, Object> result = new HashMap<>();

		util.logger.debug(sql);
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
		PreparedStatement st = conn.prepareStatement(sql)) {
			ResultSet rs = st.executeQuery();

			for (String key : selectKeys) {
				result.put(key, rs.getObject(key));
			}
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at SELECT\nrequest: {}", sql, ex);
		}
		return result;
	}

	protected List<Map<String, Object>> select(final String sql, final List<String> selectKeys) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		util.logger.debug(sql);
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
		PreparedStatement st = conn.prepareStatement(sql)) {
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Map<String, Object> data = new HashMap<>();
				for (String key : selectKeys) {
					data.put(key, rs.getObject(key));
				}
				results.add(data);
			}
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at SELECT\nrequest: {}", sql, ex);
		}
		return results;
	}

	// Execute statement
	protected void execute(final String sql) {
		util.logger.debug(sql);
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
		PreparedStatement st = conn.prepareStatement(sql)) {
			st.executeUpdate();
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at statement execution\nrequest: {}", sql, ex);
		}
	}

	protected int lastId() {
		final String sql = "SELECT last_insert_rowid();";
		util.logger.debug(sql);
		int rowId = 0;
		try (Connection conn = DriverManager.getConnection(util.getUrlSQLite());
		PreparedStatement st = conn.prepareStatement(sql)) {
			ResultSet rs = st.executeQuery();

			rowId = rs.getInt(1);
		} catch (SQLException ex) {
			util.logger.warn("DB SQLite: Error at statement execution\nrequest: {}", sql, ex);
		}
		return rowId;
	}
	

	// UTILS
	protected String quote(Object value) {
		// Convert to string and replace '(single quote) with ''(2 single quotes) for sql
		if (value == null) return "NULL";
		String str = String.valueOf(value);
		if (str == "NULL") return str;

		return "'" + String.valueOf(value).replaceAll("'", "''") + "'"; // smt's -> 'smt''s'
	}

}

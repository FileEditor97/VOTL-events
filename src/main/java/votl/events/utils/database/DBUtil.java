package votl.events.utils.database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import votl.events.App;
import votl.events.utils.database.managers.BankManager;
import votl.events.utils.database.managers.ConfessionsManager;
import votl.events.utils.database.managers.EmoteManager;
import votl.events.utils.database.managers.ItemsManager;
import votl.events.utils.database.managers.TokenManager;
import votl.events.utils.database.managers.TokenUpdatesManager;
import votl.events.utils.file.FileManager;

public class DBUtil {
	
	private final FileManager fileManager;

	protected final Logger logger = (Logger) LoggerFactory.getLogger(DBUtil.class);

	private final ConnectionUtil connectionUtil;

	public final TokenManager tokens;
	public final TokenUpdatesManager tokenUpdates;
	public final BankManager bank;
	public final EmoteManager emotes;
	public final ItemsManager items;
	public final ConfessionsManager confess;

	public DBUtil(FileManager fileManager) {
		this.fileManager = fileManager;
		this.connectionUtil = new ConnectionUtil("jdbc:sqlite:%s".formatted(fileManager.getFiles().get("database")), logger);

		tokens = new TokenManager(connectionUtil);
		tokenUpdates = new TokenUpdatesManager(connectionUtil);
		bank = new BankManager(connectionUtil);
		emotes = new EmoteManager(connectionUtil);
		items = new ItemsManager(connectionUtil);
		confess = new ConfessionsManager(connectionUtil);

		updateDB();
	}


	// 0 - no version or error
	// 1> - compare active db version with resources
	// if version lower -> apply instruction for creating new tables, adding/removing collumns
	// in the end set active db version to resources
	public Integer getActiveDBVersion() {
		Integer version = 0;
		try (Connection conn = connectionUtil.connectSQLite();
		PreparedStatement st = conn.prepareStatement("PRAGMA user_version")) {
			version = st.executeQuery().getInt(1);
		} catch(SQLException ex) {
			logger.warn("SQLite: Failed to get active database version", ex);
		}
		return version;
	}

	public Integer getResourcesDBVersion() {
		Integer version = 0;
		try {
			File tempFile = File.createTempFile("locale-", ".json");
			if (!fileManager.export(getClass().getResourceAsStream("/server.db"), tempFile.toPath())) {
				logger.error("Failed to write temp file {}!", tempFile.getName());
				return version;
			} else {
				try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath());
				PreparedStatement st = conn.prepareStatement("PRAGMA user_version")) {
					version = st.executeQuery().getInt(1);
				} catch(SQLException ex) {
					logger.warn("Failed to get resources database version", ex);
				}
			}
			tempFile.delete();
		} catch (IOException ioException) {
			logger.error("Exception at version check\n", ioException);
		}
		return version;
	}

	public List<List<String>> loadInstructions(Integer activeVersion) {
		List<String> lines = new ArrayList<>();
		try {
			File tempFile = File.createTempFile("database_updates", ".tmp");
			if (!fileManager.export(App.class.getResourceAsStream("/database_updates"), tempFile.toPath())) {
				logger.error("Failed to write temp file {}!", tempFile.getName());
			} else {
				lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
			}
		} catch (Exception ex) {
			logger.error("SQLite: Failed to open update file", ex);
		}
		lines = lines.subList(activeVersion - 1, lines.size());
		List<List<String>> result = new ArrayList<>();
		lines.forEach(line -> {
			String[] points = line.split(";");
			List<String> list = points.length == 0 ? Arrays.asList(line) : Arrays.asList(points);
			if (!list.isEmpty()) result.add(list);
		});
		return result;
	}

	private void updateDB() {
		// 0 - skip
		Integer newVersion = getResourcesDBVersion();
		if (newVersion == 0) return;
		Integer activeVersion = getActiveDBVersion();
		if (activeVersion == 0) return;

		if (newVersion > activeVersion) {
			try (Connection conn = connectionUtil.connectSQLite();
				Statement st = conn.createStatement()) {
				if (activeVersion < newVersion) {
					for (List<String> version : loadInstructions(activeVersion)) {
						for (String sql : version) {
							logger.debug(sql);
							st.execute(sql);
						}
					}
				}
			} catch(SQLException ex) {
				logger.warn("SQLite: Failed to execute update!\nPerform database update manually or delete it.\n{}", ex.getMessage());
				return;
			}
			
			// Update version
			try (Connection conn = connectionUtil.connectSQLite();
			Statement st = conn.createStatement()) {
				st.execute("PRAGMA user_version = "+newVersion.toString());
				logger.info("SQLite: Database version updated to {}", newVersion);
			} catch(SQLException ex) {
				logger.warn("SQLite: Failed to set active database version", ex);
			}
		}
	}
}

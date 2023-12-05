package votl.events.services;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import votl.events.App;
import votl.events.utils.database.DBUtil;

public class ScheduledCheck {
	
	private final App bot;
	private final DBUtil db;

	public ScheduledCheck(App bot) {
		this.bot = bot;
		this.db = bot.getDBUtil();
	}

	public void rareChecks() {
		CompletableFuture.runAsync(() -> {
			checkEmotes();
		});
	}

	private void checkEmotes() {
		try {
			List<Integer> list = db.emotes.getExpired(Instant.now());
			list.forEach(keywordId -> {
				String trigger = db.emotes.getTrigger(keywordId);
				if (trigger != null)
					bot.removeEmojiKeyword(trigger);
				db.emotes.deleteEmote(keywordId);
			});
		} catch(Throwable t) {
			bot.getLogger().error("Exception caught during emotes check.", t);
		}
	}

}

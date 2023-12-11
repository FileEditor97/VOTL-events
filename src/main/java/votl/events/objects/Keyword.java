package votl.events.objects;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

public class Keyword {
	
	private final String trigger;
	private final Boolean exact;
	private final EmojiUnion emoji;

	public Keyword(String trigger, Boolean exact, String emoji) {
		this.trigger = trigger;
		this.exact = exact;
		this.emoji = Emoji.fromFormatted(emoji);
	}

	public Keyword(String trigger, Boolean exact, EmojiUnion emoji) {
		this.trigger = trigger;
		this.exact = exact;
		this.emoji = emoji;
	}

	public String getTrigger() {
		return trigger;
	}

	public Boolean isExact() {
		return exact;
	}

	public EmojiUnion getEmoji() {
		return emoji;
	}

}

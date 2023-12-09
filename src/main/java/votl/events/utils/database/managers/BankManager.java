package votl.events.utils.database.managers;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import votl.events.utils.database.ConnectionUtil;
import votl.events.utils.database.SQLiteBase;

public class BankManager extends SQLiteBase {
	
	private final String table = "bank";
	
	public BankManager(ConnectionUtil cu) {
		super(cu);
	}

	public void createBank(long guildId) {
		execute("INSERT INTO %s(guildId) VALUES(%d) ON CONFLICT(guildId) DO NOTHING;");
	}

	public void deleteBank(long guildId) {
		execute("DELETE FROM %s WHERE (guildId=%d);".formatted(table, guildId));
	}

	public void setLogChannel(long guildId, long logChannelId) {
		execute("UPDATE %s SET logChannel=%d WHERE (guildId=%d);".formatted(table, logChannelId, guildId));
	}

	@Nullable
	public Long getLogChannel(long guildId) {
		return selectOne("SELECT logChannel FROM %s WHERE (guildId=%d);".formatted(table, guildId), "logChannel", Integer.class).longValue();
	}

	public void changeAmount(long guildId, int amount) {
		execute("UPDATE %s SET amount=amount+%d WHERE (guildId=%d);".formatted(table, amount, guildId));
	}

	public Integer getAmount(long guildId) {
		Integer data = selectOne("SELECT amount FROM %s WHERE (guildId=%d);".formatted(table, guildId), "amount", Integer.class);
		if (data == null) return 0;
		return data;
	}

	// Transfer
	public void setTransferCut(long guildId, int percent) {
		execute("UPDATE %s SET transferCut=%d WHERE (guildId=%d);".formatted(table, percent, guildId));
	}

	@Nullable
	public Integer getTransferCut(long guildId) {
		return selectOne("SELECT transferCut FROM %s WHERE (guildId=%d);".formatted(table, guildId), "transferCut", Integer.class);
	}

	public void setMaxTransferAmount(long guildId, int maxAmount) {
		execute("UPDATE %s SET maxTransfer=%d WHERE (guildId=%d);".formatted(table, maxAmount, guildId));
	}

	@Nullable
	public Integer getMaxTransferAmount(long guildId) {
		return selectOne("SELECT maxTransfer FROM %s WHERE (guildId=%d);".formatted(table, guildId), "maxTransfer", Integer.class);
	}

	// Exchange
	public void setExchangeRate(long guildId, Float rate) {
		execute("UPDATE %s SET exchangeRate=%.1f WHERE (guildId=%d);".formatted(table, rate, guildId));
	}

	@Nullable
	public Float getExchangeRate(long guildId) {
		Float data = selectOne("SELECT exchangeRate FROM %s WHERE (guildId=%d);".formatted(table, guildId), "exchangeRate", Float.class);
		if (data == null) return 0f;
		return data;
	}

	public void setExchangeAmountMin(long guildId, int minAmount) {
		execute("UPDATE %s SET minExchange=%d WHERE (guildId=%d);".formatted(table, minAmount, guildId));
	}

	public void setExchangeAmountMax(long guildId, int maxAmount) {
		execute("UPDATE %s SET maxExchange=%d WHERE (guildId=%d);".formatted(table, maxAmount, guildId));
	}

	public Pair<Integer, Integer> getExchangeAmountRange(long guildId) {
		Map<String, Object> data = selectOne("SELECT minExchange, maxExchange FROM %s WHERE (guildId=%d);", List.of("minExchange", "maxExchange"));
		return Pair.of((Integer) data.get("minExchange"), (Integer) data.get("maxExchange"));
	}
	
}

package votl.events;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import votl.events.base.command.CommandClient;
import votl.events.base.command.CommandClientBuilder;
import votl.events.base.waiter.EventWaiter;
import votl.events.commands.manage.BankCmd;
import votl.events.commands.manage.EmotesCmd;
import votl.events.commands.other.HelpCmd;
import votl.events.commands.other.PingCmd;
import votl.events.commands.owner.ShutdownCmd;
import votl.events.commands.shop.ItemsCmd;
import votl.events.commands.shop.ShopCmd;
import votl.events.commands.tokens.AddCmd;
import votl.events.commands.tokens.LeaderboardCmd;
import votl.events.commands.tokens.RemoveCmd;
import votl.events.commands.tokens.TokensCmd;
import votl.events.commands.tokens.TransferCmd;
import votl.events.listener.AutoCompleteListener;
import votl.events.listener.CommandListener;
import votl.events.listener.GuildListener;
import votl.events.listener.MessageListener;
import votl.events.objects.constants.Constants;
import votl.events.objects.constants.Links;
import votl.events.services.CountingThreadFactory;
import votl.events.services.ScheduledCheck;
import votl.events.utils.CheckUtil;
import votl.events.utils.WebhookAppender;
import votl.events.utils.database.DBUtil;
import votl.events.utils.file.FileManager;
import votl.events.utils.file.lang.LangUtil;
import votl.events.utils.message.EmbedUtil;
import votl.events.utils.message.LocaleUtil;
import votl.events.utils.message.MessageUtil;

public class App {

	private final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

	private static App instance;

	public final String VERSION = Optional.ofNullable(App.class.getPackage().getImplementationVersion()).map(ver -> "v"+ver).orElse("DEVELOPMENT");

	public final JDA JDA;
	public final EventWaiter WAITER;
	private final CommandClient commandClient;

	private final FileManager fileManager = new FileManager();

	private final AutoCompleteListener acListener;
	private final GuildListener guildListener;
	private final MessageListener messageListener;
	private final CommandListener commandListener;

	private final ScheduledExecutorService scheduledExecutor;
	private final ScheduledCheck scheduledCheck;

	private final DBUtil dbUtil;
	private final LangUtil langUtil;
	private final CheckUtil checkUtil;
	private final LocaleUtil localeUtil;
	private final MessageUtil messageUtil;
	private final EmbedUtil embedUtil;

	public App() {
		try {
			fileManager.addFile("config", "/config.json", Constants.DATA_PATH + "config.json")
				.addFile("database", "/server.db", Constants.DATA_PATH + "server.db")
				.addLang("en-GB")
				.addLang("ru");
		} catch (Exception ex) {
			logger.error("Error while interacting with File Manager", ex);
			System.exit(0);
		}

		// Define utils
		dbUtil      = new DBUtil(fileManager);
		langUtil    = new LangUtil(this);
		checkUtil   = new CheckUtil(this);
		localeUtil	= new LocaleUtil(this, langUtil, "en-GB", DiscordLocale.ENGLISH_UK);
		messageUtil = new MessageUtil(this);
		embedUtil	= new EmbedUtil(localeUtil);

		WAITER      	= new EventWaiter();
		guildListener   = new GuildListener(this);
		messageListener = new MessageListener(this);
		commandListener = new CommandListener();

		scheduledExecutor	= new ScheduledThreadPoolExecutor(3, new CountingThreadFactory("VOTL", "Scheduler", false));
		scheduledCheck		= new ScheduledCheck(this);

		scheduledExecutor.scheduleAtFixedRate(() -> scheduledCheck.rareChecks(), 2, 12, TimeUnit.HOURS);

		// Define a command client
		commandClient = new CommandClientBuilder()
			.setOwnerId(Constants.DEVELOPER_ID)
			.setServerInvite(Links.DISCORD)
			.setStatus(OnlineStatus.ONLINE)
			.setActivity(Activity.customStatus(">>>  /help  <<<"))
			.setScheduleExecutor(scheduledExecutor)
			.setListener(commandListener)
			.setDevGuildIds(fileManager.getStringList("config", "dev-servers").toArray(new String[0]))
			.addSlashCommands(
				// Owner
				new ShutdownCmd(this),
				// Other
				new PingCmd(this),
				new HelpCmd(this),
				// Tokens
				new TokensCmd(this),
				new AddCmd(this),
				new RemoveCmd(this),
				new LeaderboardCmd(this),
				new TransferCmd(this),
				// Manage
				new BankCmd(this),
				new EmotesCmd(this),
				// Shop
				new ItemsCmd(this),
				new ShopCmd(this)
			)
			.build();

		// Build
		MemberCachePolicy policy = MemberCachePolicy.OWNER;

		acListener		= new AutoCompleteListener(commandClient, dbUtil);

		JDABuilder jdaBuilder = JDABuilder.createLight(fileManager.getString("config", "bot-token"))
			.setEnabledIntents(
				GatewayIntent.GUILD_MEMBERS,			// required for updating member profiles and ChunkingFilter
				GatewayIntent.GUILD_MESSAGES,			// checks for verified
				GatewayIntent.MESSAGE_CONTENT			// search for keyword
			)
			.setMemberCachePolicy(policy)
			.setChunkingFilter(ChunkingFilter.ALL)		// chunk all guilds
			.enableCache(
				CacheFlag.MEMBER_OVERRIDES,		// channel permission overrides
				CacheFlag.ROLE_TAGS				// role search
			)
			.addEventListeners(commandClient, WAITER, acListener, guildListener, messageListener);
		
		JDA jda = null;

		Integer retries = 4; // how many times will it try to build
		Integer cooldown = 8; // in seconds; cooldown amount, will doubles after each retry
		while (true) {
			try {
				jda = jdaBuilder.build();
				break;
			} catch (InvalidTokenException ex) {
				logger.error("Login failed due to Token", ex);
				System.exit(0);
			} catch (ErrorResponseException ex) { // Tries to reconnect to discord x times with some delay, else exits
				if (retries > 0) {
					retries--;
					logger.info("Retrying connecting in "+cooldown+" seconds... "+retries+" more attempts");
					try {
						Thread.sleep(cooldown*1000);
					} catch (InterruptedException e) {
						logger.error("Thread sleep interupted", e);
					}
					cooldown*=2;
				} else {
					logger.error("No network connection or couldn't connect to DNS", ex);
					System.exit(0);
				}
			}
		}

		this.JDA = jda;
	}
	
	public CommandClient getClient() {
		return commandClient;
	}

	public Logger getLogger() {
		return logger;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public DBUtil getDBUtil() {
		return dbUtil;
	}

	public CheckUtil getCheckUtil() {
		return checkUtil;
	}

	public LocaleUtil getLocaleUtil() {
		return localeUtil;
	}

	public MessageUtil getMessageUtil() {
		return messageUtil;
	}

	public EmbedUtil getEmbedUtil() {
		return embedUtil;
	}

	public ScheduledExecutorService getExecutorService() {
		return scheduledExecutor;
	}

	protected void setupKeywords() {
		messageListener.setupKeywords();
	}

	public void addEmojiKeyword(final Long guildId, final String trigger, final EmojiUnion emoji) {
		messageListener.addKeyword(guildId, trigger, emoji);
	}

	public Boolean removeEmojiKeyword(final Long guildId, final String trigger) {
		return messageListener.removeKeyword(guildId, trigger);
	}

	public Boolean removeEmojiKeyword(final String trigger) {
		return messageListener.removeKeyword(trigger);
	}

	public static void main(String[] args) {
		Message.suppressContentIntentWarning();

		instance = new App();
		instance.createWebhookAppender();
		instance.logger.info("Success start");

		instance.getExecutorService().schedule(() -> instance.setupKeywords(), 10, TimeUnit.SECONDS);
	}

	private void createWebhookAppender() {
		String url = fileManager.getNullableString("config", "webhook");
		if (url == null) return;
		
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setPattern("%d{dd.MM.yyyy HH:mm:ss} [%thread] [%logger{0}] %msg%n");
		ple.setContext(lc);
		ple.start();
		WebhookAppender webhookAppender = new WebhookAppender();
		webhookAppender.setUrl(url);
		webhookAppender.setEncoder(ple);
		webhookAppender.setContext(lc);
		webhookAppender.start();

		Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logbackLogger.addAppender(webhookAppender);
		logbackLogger.setAdditive(false);
	}

}

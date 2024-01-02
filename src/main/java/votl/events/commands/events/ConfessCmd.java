package votl.events.commands.events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import votl.events.App;
import votl.events.base.command.CooldownScope;
import votl.events.base.command.SlashCommandEvent;
import votl.events.base.waiter.EventWaiter;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ConfessCmd extends CommandBase {

	private EventWaiter waiter;
	private final DiscordLocale globalLocale = DiscordLocale.RUSSIAN;
	
	public ConfessCmd(App bot, EventWaiter waiter) {
		super(bot);
		this.name = "confess";
		this.path = "bot.events.confess";
		this.category = CmdCategory.EVENTS;
		this.guildOnly = false;
		this.cooldownScope = CooldownScope.USER;
		this.cooldown = 15;
		this.waiter = waiter;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (!event.isFromType(ChannelType.PRIVATE)) {
			createReply(event, lu.getLocalized(globalLocale, path+".only_pm"));
			return;
		}
		event.deferReply().queue();

		Map<Integer, String> allOptions = bot.getDBUtil().confess.getAllOptions();
		if (allOptions.isEmpty()) {
			editError(event, path+".no_options");
			return;
		}

		Long userId = event.getUser().getIdLong();
		List<SelectOption> options = new ArrayList<SelectOption>();
		allOptions.entrySet().stream().filter(e -> !bot.getDBUtil().confess.hasConfessed(userId, e.getKey()))
			.forEach(e -> options.add( SelectOption.of(e.getValue(), String.valueOf(e.getKey())) ));

		if (options.isEmpty()) {
			editError(event, path+".no_options");
			return;
		}

		StringSelectMenu menu = StringSelectMenu.create("confess")
			.addOptions(options)
			.setPlaceholder(lu.getLocalized(globalLocale, path+".text"))
			.build();
		MessageEmbed embed = new EmbedBuilder().setColor(Constants.COLOR_DEFAULT)
			.setDescription(lu.getLocalized(globalLocale, path+".embed"))
			.build();
		
		event.getHook().editOriginalEmbeds(embed).setActionRow(menu).queue(msgOriginal -> {
			waiter.waitForEvent(
				StringSelectInteractionEvent.class,
				e -> e.getMessageId().equals(msgOriginal.getId()),
				selectEvent -> {
					Integer optionId = Integer.valueOf(selectEvent.getSelectedOptions().get(0).getValue());
					Long guildId = bot.getDBUtil().confess.getGuildId(optionId);
					if (guildId == null) {
						editError(event, path+".not_found", "Selected option not found. ID: %s".formatted(optionId));
						return;
					}
					// Check user is guild's member
					selectEvent.getJDA().getGuildById(guildId).retrieveMember(selectEvent.getUser()).queue(member -> {
						// Reply
						msgOriginal.editMessageEmbeds(new EmbedBuilder(embed).setDescription(lu.getLocalized(globalLocale, path+".enter")).build()).setComponents().queue();
						// Wait for reply
						waiter.waitForEvent(
							MessageReceivedEvent.class,
							e -> e.getChannel().getId().equals(selectEvent.getChannelId()),
							replyEvent -> {
								// Prepare embed
								String text = replyEvent.getMessage().getContentRaw();
								EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Constants.COLOR_WARNING)
									.setDescription(text.length() > 4096 ? text.subSequence(0, 4095) : text);
								// Send confirm
								Button confirm = Button.success("confess", lu.getLocalized(globalLocale, path+".confirm"));
								replyEvent.getChannel().sendMessageEmbeds(embedBuilder.setTitle(lu.getLocalized(globalLocale, path+".title")).build()).addActionRow(confirm).queue(msgConfirm -> {
									waiter.waitForEvent(
										ButtonInteractionEvent.class,
										e -> e.getMessageId().equals(msgConfirm.getId()),
										buttonEvent -> {
											// Get channel
											Long channelId = bot.getDBUtil().confess.getChannelId(optionId);
											if (channelId == null) {
												msgConfirm.editMessageEmbeds(bot.getEmbedUtil().getError(event, path+".unknown_channel", "Option's channel is null"));
												return;
											}
											TextChannel channel = bot.JDA.getTextChannelById(channelId);
											if (channel == null) {
												msgConfirm.editMessageEmbeds(bot.getEmbedUtil().getError(event, path+".unknown_channel", "Option's channel not found by bot"));
												return;
											}
											// Save confession info
											Instant time = Instant.now();
											bot.getDBUtil().confess.confessed(userId, optionId, time);
											// Send message
											msgConfirm.editMessage(Constants.SUCCESS).setEmbeds().setComponents().queue();
											try {
												channel.sendMessageEmbeds(new EmbedBuilder()
													.setColor(Constants.COLOR_DEFAULT)
													.setTitle(bot.getDBUtil().confess.getName(optionId))
													.setDescription(text.length() > 4096 ? text.subSequence(0, 4095) : text)
													.setTimestamp(time)
													.setFooter("/confess")
													.build()
												).queue();
											} catch (InsufficientPermissionException ex) {
												// Ignore this shit
											}
										},
										30,
										TimeUnit.SECONDS,
										() -> {
											msgConfirm.editMessageComponents(ActionRow.of(
												confirm.withLabel(lu.getLocalized(globalLocale, "errors.timed_out")).asDisabled()
											)).queue();
										}
									);
								});
							},
							5,
							TimeUnit.MINUTES,
							() -> {
								msgOriginal.reply(lu.getLocalized(globalLocale, path+".timed_out")).queue();
							}
						);
					}, failure -> {
						editError(event, path+".not_member", "Guild: %s".formatted(selectEvent.getJDA().getGuildById(guildId).getName()));
					});
				},
				30,
				TimeUnit.SECONDS,
				() -> {
					msgOriginal.editMessageComponents(ActionRow.of(
						StringSelectMenu.create("timed_out").addOption("null", "null").setPlaceholder(lu.getLocalized(globalLocale, "errors.timed_out")).setDisabled(true).build()
					)).queue();
				}
			);
		});
	}

}

package votl.events.commands.events;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import votl.events.App;
import votl.events.base.command.CooldownScope;
import votl.events.base.command.SlashCommandEvent;
import votl.events.base.waiter.EventWaiter;
import votl.events.commands.CommandBase;
import votl.events.objects.constants.CmdCategory;
import votl.events.objects.constants.Constants;

public class ValentineCmd extends CommandBase {

	private EventWaiter waiter;
	private final DiscordLocale globalLocale = DiscordLocale.RUSSIAN;
	private final int MAX_VALENTINES = 6;
	
	public ValentineCmd(App bot, EventWaiter waiter) {
		super(bot);
		this.name = "valentine";
		this.path = "bot.events.valentine";
		this.options = List.of(
			new OptionData(OptionType.USER, "user", lu.getText(path+".user.help"), true)
		);
		this.category = CmdCategory.EVENTS;
		this.cooldownScope = CooldownScope.USER;
		this.cooldown = 60;
		this.waiter = waiter;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.deferReply(true).queue();
		
		Member target = event.optMember("user");
		if (target == null || target.equals(event.getMember()) || target.getUser().isBot()) {
			editError(event, path+".no_member");
			return;
		}

		if (bot.getDBUtil().valentines.countValentines(event.getUser().getIdLong()) >= MAX_VALENTINES) {
			editError(event, path+".limited");
			return;
		}

		event.getUser().openPrivateChannel().queue(pc -> {
			// Send message in DM
			pc.sendMessageEmbeds(new EmbedBuilder().setColor(Constants.COLOR_DEFAULT).setDescription(lu.getLocalized(globalLocale, path+".enter").formatted(target.getAsMention())).build()).queue(done -> {
				event.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Constants.COLOR_SUCCESS).setDescription(lu.getLocalized(globalLocale, path+".pm_done")).build()).queue();
				waiter.waitForEvent(
					MessageReceivedEvent.class,
					e -> e.getChannel().getId().equals(pc.getId()) && !e.getAuthor().isBot(),
					replyEvent -> {
						// Prepare embed
						String text = replyEvent.getMessage().getContentRaw();
						EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Constants.COLOR_WARNING)
							.setTitle(lu.getLocalized(globalLocale, path+".confirm_title").formatted(target.getEffectiveName()))
							.setDescription(text.length() > 2000 ? text.subSequence(0, 2000) : text);
						replyEvent.getMessage().getAttachments().stream().findFirst().ifPresent(att -> embedBuilder.setImage(att.getUrl()));

						// Send confirm
						Button confirm = Button.success("valentine", lu.getLocalized(globalLocale, path+".confirm_button"));
						replyEvent.getChannel().sendMessage(lu.getLocalized(globalLocale, path+".confirm_value")).addEmbeds(embedBuilder.build()).addActionRow(confirm).queue(msgConfirm -> {
							waiter.waitForEvent(
								ButtonInteractionEvent.class,
								e -> e.getMessageId().equals(msgConfirm.getId()),
								buttonEvent -> {
									Button allow = Button.success("allow:"+target.getId(), "Allow");
									Button delete = Button.danger("reject", "Reject");

									// Send for approval
									MessageChannel channel = bot.JDA.getTextChannelById(1205461668516335616L);
									if (channel != null) {
										channel.sendMessage("Valentine sent\nSender: <@%s>\nReceiver: <@%s>\nGuild: `%s`"
											.formatted(event.getUser().getId(), target.getId(), event.getGuild().getName())
										).addEmbeds(embedBuilder
											.setTitle(null)
											.build()
										).addActionRow(
											allow, delete
										).queue();
									}
									// log in bd
									bot.getDBUtil().valentines.addValentine(event.getUser().getIdLong(), Instant.now());
									// reply
									msgConfirm.editMessageEmbeds(new EmbedBuilder()
										.setColor(Constants.COLOR_SUCCESS)
										.setDescription(lu.getLocalized(globalLocale, path+".sent"))
										.build()
									).setContent(null).setComponents().queue();
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
						done.reply(lu.getLocalized(globalLocale, path+".timed_out")).queue();
					}
				);
			},
			failure -> {
				event.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Constants.COLOR_FAILURE).setDescription(lu.getLocalized(globalLocale, path+".pm_failed")).build()).queue();
			});
		});
	}

}

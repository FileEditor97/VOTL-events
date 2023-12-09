package votl.events.listener;

import java.util.List;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import votl.events.base.command.CommandClient;
import votl.events.base.command.SlashCommand;
import votl.events.utils.database.DBUtil;

public class AutoCompleteListener extends ListenerAdapter {

	private final List<SlashCommand> cmds;
	private final DBUtil dbUtil;

	public AutoCompleteListener(CommandClient cc, DBUtil dbUtil) {
		this.cmds = cc.getSlashCommands();
		this.dbUtil = dbUtil;
	}
		
	@Override
	public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
		String cmdName = event.getFullCommandName();
		String focusedOption = event.getFocusedOption().getName();
		if (cmdName.equals("help") && focusedOption.equals("command")) {
			String value = event.getFocusedOption().getValue().toLowerCase().split(" ")[0];
			List<Choice> choices = cmds.stream()
				.filter(cmd -> cmd.getName().contains(value))
				.map(cmd -> new Choice(cmd.getName(), cmd.getName()))
				.toList();
			if (choices.size() > 25) {
				choices.subList(25, choices.size()).clear();
			}
			event.replyChoices(choices).queue();
		}
		else if(focusedOption.equals("item_id")) {
			List<Choice> choices = dbUtil.items.getItemsShort(event.getGuild().getIdLong()).stream()
				.map(item -> new Choice(item.getRight(), item.getLeft()))
				.toList();
			if (choices.size() > 25) {
				choices.subList(25, choices.size()).clear();
			}
			event.replyChoices(choices).queue();
		}
		else if(focusedOption.equals("item")) {
			String value = event.getFocusedOption().getValue().toLowerCase().split(" ")[0];
			List<Choice> choices = dbUtil.items.getItemsShort(event.getGuild().getIdLong()).stream()
				.filter(item -> item.getRight().contains(value))
				.map(item -> new Choice(item.getRight(), item.getLeft()))
				.toList();
			if (choices.size() > 25) {
				choices.subList(25, choices.size()).clear();
			}
			event.replyChoices(choices).queue();
		}
	}

}

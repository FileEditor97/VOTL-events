package votl.events.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import votl.events.App;
import votl.events.objects.constants.Constants;
import votl.events.utils.exceptions.CheckException;

public class CheckUtil {
	
	private final App bot;

	public CheckUtil(App bot) {
		this.bot = bot;
	}

	public boolean isDeveloper(UserSnowflake user) {
		return Constants.DEVELOPER_ID == user.getIdLong();
	}

	public boolean isAdministrator(Member member) {
		return member.hasPermission(Permission.ADMINISTRATOR);
	}

	public Boolean hasAccess(Boolean adminRequired, Member member) {
		return adminRequired ? isAdministrator(member) : true;
	}

	public CheckUtil hasAccess(IReplyCallback replyCallback, Boolean adminRequired, Member member) throws CheckException {
		if (!hasAccess(adminRequired, member))
			throw new CheckException(bot.getEmbedUtil().getError(replyCallback, "errors.interaction.no_access", "Required access level: "+"Admin"));
		return this;
	}

	public CheckUtil hasPermissions(IReplyCallback replyCallback, Guild guild, Member member, Permission[] permissions) throws CheckException {
		return hasPermissions(replyCallback, guild, member, false, null, permissions);
	}

	public CheckUtil hasPermissions(IReplyCallback replyCallback, Guild guild, Member member, boolean isSelf, Permission[] permissions) throws CheckException {
		return hasPermissions(replyCallback, guild, member, isSelf, null, permissions);
	}

	public CheckUtil hasPermissions(IReplyCallback replyCallback, Guild guild, Member member, boolean isSelf, GuildChannel channel, Permission[] permissions) throws CheckException {
		if (permissions == null || permissions.length == 0)
			return this;
		if (guild == null || (!isSelf && member == null))
			return this;

		MessageCreateData msg = null;
		if (isSelf) {
			Member self = guild.getSelfMember();
			if (channel == null) {
				for (Permission perm : permissions) {
					if (!self.hasPermission(perm)) {
						msg = bot.getEmbedUtil().createPermError(replyCallback, perm, true);
						break;
					}
				}
			} else {
				for (Permission perm : permissions) {
					if (!self.hasPermission(channel, perm)) {
						msg = bot.getEmbedUtil().createPermError(replyCallback, channel, perm, true);
						break;
					}
				}
			}
		} else {
			if (channel == null) {
				for (Permission perm : permissions) {
					if (!member.hasPermission(perm)) {
						msg = bot.getEmbedUtil().createPermError(replyCallback, perm, false);
						break;
					}
				}
			} else {
				for (Permission perm : permissions) {
					if (!member.hasPermission(channel, perm)) {
						msg = bot.getEmbedUtil().createPermError(replyCallback, channel, perm, false);
						break;
					}
				}
			}
		}
		if (msg != null) {
			throw new CheckException(msg);
		}
		return this;
	}

}

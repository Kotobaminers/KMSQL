package com.github.orgs.kotobaminers.kmsql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KMSQLCommand implements CommandExecutor {

	enum Commands {
		REGISTER(Arrays.asList());

		private List<String> alias;
		private Commands(List<String> alias) {
			List<String> list = new ArrayList<String>();
			list.add(this.name());
			alias.forEach(a -> list.add(a.toUpperCase()));
			this.alias = list;
		}

		private void execute(CommandSender sender, String[] args) {
			switch(this) {
			case REGISTER:
				if (sender instanceof Player) {
					if (0 < args.length) {
						try {
							SQLConnector.refreshData();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return;
					}
					new SQLConnector().printRegistrationLink((Player) sender);
				}
				break;
			default:
				break;
			}
		}
	}

	public KMSQLCommand(KMSQL plugin) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Optional<Commands> optional = Stream.of(Commands.values())
			.filter(com -> com.alias.contains(label.toUpperCase()))
			.findFirst();
		optional.ifPresent(com -> com.execute(sender, args));
		return optional.isPresent();
	}

}

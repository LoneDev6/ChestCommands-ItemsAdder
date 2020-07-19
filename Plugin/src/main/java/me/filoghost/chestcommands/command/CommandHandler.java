/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.chestcommands.command;

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.Permissions;
import me.filoghost.chestcommands.command.framework.CommandFramework;
import me.filoghost.chestcommands.command.framework.CommandValidate;
import me.filoghost.chestcommands.menu.InternalIconMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.util.Utils;
import me.filoghost.chestcommands.util.logging.ErrorCollector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandHandler extends CommandFramework {
	
	private final MenuManager menuManager;
	
	public CommandHandler(MenuManager menuManager, String label) {
		super(label);
		this.menuManager = menuManager;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			// This info is accessible to anyone. Please don't remove it, remember that Chest Commands is developed for free
			sender.sendMessage(ChestCommands.CHAT_PREFIX);
			sender.sendMessage(ChatColor.GREEN + "Version: " + ChatColor.GRAY + ChestCommands.getInstance().getDescription().getVersion());
			sender.sendMessage(ChatColor.GREEN + "Developer: " + ChatColor.GRAY + "filoghost");
			sender.sendMessage(ChatColor.GREEN + "Commands: " + ChatColor.GRAY + "/" + label + " help");
			return;
		}


		if (args[0].equalsIgnoreCase("help")) {
			checkCommandPermission(sender, "help");
			sender.sendMessage(ChestCommands.CHAT_PREFIX + "Commands:");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " reload" + ChatColor.GRAY + " - Reloads the plugin.");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " errors" + ChatColor.GRAY + " - Displays the last load errors on the console.");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " list" + ChatColor.GRAY + " - Lists the loaded menus.");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " open <menu> [player]" + ChatColor.GRAY + " - Opens a menu for a player.");
			return;
		}


		if (args[0].equalsIgnoreCase("errors")) {
			checkCommandPermission(sender, "errors");
			ErrorCollector errorCollector = ChestCommands.getLastLoadErrors();

			if (errorCollector.hasErrors()) {
				errorCollector.logToConsole();
				sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Last time the plugin loaded, " + errorCollector.getErrorsCount() + " error(s) were found.");
				if (!(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Errors were printed on the console.");
				}
			} else {
				sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.GREEN + "Last plugin load was successful, no errors logged.");
			}
			return;
		}


		if (args[0].equalsIgnoreCase("reload")) {
			checkCommandPermission(sender, "reload");

			ChestCommands.closeAllMenus();

			ErrorCollector errorCollector = ChestCommands.getInstance().load();

			if (!errorCollector.hasErrors()) {
				sender.sendMessage(ChestCommands.CHAT_PREFIX + "Plugin reloaded.");
			} else {
				errorCollector.logToConsole();
				sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Plugin reloaded with " + errorCollector.getErrorsCount() + " error(s).");
				if (!(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Please check the console.");
				}
			}
			return;
		}


		if (args[0].equalsIgnoreCase("open")) {
			checkCommandPermission(sender, "open");
			CommandValidate.minLength(args, 2, "Usage: /" + label + " open <menu> [player]");

			Player target;

			if (sender instanceof Player) {
				if (args.length > 2) {
					checkCommandPermission(sender, "open.others");
					target = Bukkit.getPlayerExact(args[2]);
				} else {
					target = (Player) sender;
				}
			} else {
				CommandValidate.minLength(args, 3, "You must specify a player from the console.");
				target = Bukkit.getPlayerExact(args[2]);
			}

			CommandValidate.notNull(target, "That player is not online.");

			String menuName = Utils.addYamlExtension(args[1]);
			InternalIconMenu menu = menuManager.getMenuByFileName(menuName);
			CommandValidate.notNull(menu, "The menu \"" + menuName + "\" was not found.");

			if (!sender.hasPermission(menu.getOpenPermission())) {
				menu.sendNoOpenPermissionMessage(sender);
				return;
			}

			if (sender.getName().equalsIgnoreCase(target.getName())) {
				if (!ChestCommands.getLang().open_menu.isEmpty()) {
					sender.sendMessage(ChestCommands.getLang().open_menu.replace("{menu}", menuName));
				}
			} else {
				if (!ChestCommands.getLang().open_menu_others.isEmpty()) {
					sender.sendMessage(ChestCommands.getLang().open_menu_others.replace("{menu}", menuName).replace("{player}", target.getName()));
				}
			}

			menu.open(target);
			return;
		}


		if (args[0].equalsIgnoreCase("list")) {
			checkCommandPermission(sender, "list");
			sender.sendMessage(ChestCommands.CHAT_PREFIX + "Loaded menus:");
			for (String file : menuManager.getMenuFileNames()) {
				sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + file);
			}

			return;
		}

		sender.sendMessage(ChatColor.RED + "Unknown sub-command \"" + args[0] + "\".");
	}

	private void checkCommandPermission(CommandSender sender, String commandPermission) {
		CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_PREFIX + commandPermission), "You don't have permission.");
	}

}

package net.jplays.wildpayments;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WildPayments extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register the /pay command
        this.getCommand("pay").setExecutor(new PayCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public class PayCommandExecutor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage("This command can only be executed by a player.");
                    }
                }.runTaskAsynchronously(WildPayments.this);
                return true;
            }

            Player player = (Player) sender;

            if (args.length != 2) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("\u00a7cIncorrect usage, correct usage: /pay [player] [amount]");
                    }
                }.runTaskAsynchronously(WildPayments.this);
                return true;
            }

            String targetPlayerName = args[0];
            String rawAmount = args[1];

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        long amount = parseAmount(rawAmount);
                        String commandToExecute = String.format("epay %s %d", targetPlayerName, amount);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.performCommand(commandToExecute);
                            }
                        }.runTask(WildPayments.this);
                    } catch (NumberFormatException e) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage("\u00a7cInvalid amount format.");
                            }
                        }.runTaskAsynchronously(WildPayments.this);
                    }
                }
            }.runTaskAsynchronously(WildPayments.this);

            return true;
        }

        private long parseAmount(String rawNumber) throws NumberFormatException {
            rawNumber = rawNumber.replace(",", "");
            long multiplier = 1;

            if (rawNumber.toLowerCase().endsWith("k")) {
                multiplier = 1_000;
                rawNumber = rawNumber.substring(0, rawNumber.length() - 1);
            } else if (rawNumber.toLowerCase().endsWith("m")) {
                multiplier = 1_000_000;
                rawNumber = rawNumber.substring(0, rawNumber.length() - 1);
            } else if (rawNumber.toLowerCase().endsWith("b")) {
                multiplier = 1_000_000_000;
                rawNumber = rawNumber.substring(0, rawNumber.length() - 1);
            }

            if (rawNumber.contains(".")) {
                double value = Double.parseDouble(rawNumber);
                return (long) (value * multiplier);
            } else {
                long value = Long.parseLong(rawNumber);
                return value * multiplier;
            }
        }
    }
}

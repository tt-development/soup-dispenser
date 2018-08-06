package logan.soupdispenser

import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/* This plugin allows you to store soup in a configurable "Soup dispenser" block.
You store soup by right-clicking the custom dispenser, and dispense
soup by shift+right-clicking the custom dispenser. Once a block is set up as
a soup dispenser, it will be marked with particles.
 */
class SoupDispenserPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: SoupDispenserPlugin

        /* Configuration properties */
        lateinit var dispenserBlock: Material
        var maxSoup = 0
    }

    init {
        instance = this
        dispenserBlock = Material.getMaterial(config.getInt("dispenser-block"))
        maxSoup = config.getInt("max-soup")
    }

    override fun onEnable() {

        /* Register listeners */
        server.pluginManager.registerEvents(Listeners(), this)

        /* Initialize other classes */
        SoupDispenserParticleHandler()

        logger.info("$name enabled.")
    }

    override fun onDisable() {
        logger.info("$name disabled.")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (label.equals("soupdispenser", true) && args.isNotEmpty()) {
            sender.sendMessage("${ChatColor.YELLOW}Soup Dispenser Info:")
            sender.sendMessage("Version: ${description.version}")
            sender.sendMessage("Authors: ${description.authors[0]}")
        }

        return true
    }

}

class Listeners : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.apply {

            if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock.type == SoupDispenserPlugin.dispenserBlock) {

                /* Mark this block as a dispensable block if it contains "soup dispenser" */
                if (item.type == Material.SIGN) {

                }

                /* They're taking out soup */
                if (player.isSneaking) {
                    SoupDispenserBlockRegistry.get(clickedBlock)?.apply {
                        when (amount) {
                            in 1..SoupDispenserPlugin.maxSoup -> {
                                player.inventory.addItem(ItemStack(Material.MUSHROOM_SOUP))
                                amount--
                            }
                            else -> {
                                player.sendMessage("${ChatColor.RED}No more soup.")
                            }
                        }

                    }
                }

                /* They're putting in soup */
                if (!player.isSneaking && item.type == Material.MUSHROOM_SOUP) {
                    SoupDispenserBlockRegistry.get(clickedBlock)?.apply {
                        if (amount < SoupDispenserPlugin.maxSoup) {
                            amount++
                            /* Don't send a message to the player otherwise it will get spammy */
                        }
                    }
                }


            }

        }
    }
}

class SoupDispenserBlockRegistry {

    companion object {

        val blockSet = mutableSetOf<SoupDispenserBlock>()

        fun add(block: SoupDispenserBlock) {
            blockSet.add(block)
        }

        fun remove(block: SoupDispenserBlock) {
            blockSet.remove(block)
        }

        fun get(block: Block): SoupDispenserBlock? {
            return blockSet.find { it.location == block.location }
        }

    }

}

data class SoupDispenserBlock(val location: Location) {

    var amount = 0


}

class SoupDispenserParticleHandler {

    init {
        SoupDispenserBlockRegistry.blockSet.forEach {
            it.location.world.playEffect(it.location, Effect.COLOURED_DUST, 1)
        }
    }

}



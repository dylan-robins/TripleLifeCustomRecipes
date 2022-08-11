package ovh.robinsnest.triplelife;

import com.sun.tools.javac.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

class LoginListener implements Listener {
    TripleLife _plugin;

    public LoginListener(TripleLife plugin) {
        _plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        _plugin._logger.info(String.format("Discovering recipes %s for player %s", _plugin._recipeNamespaces, player));
        player.discoverRecipes(_plugin._recipeNamespaces);
    }
}

public class TripleLife extends JavaPlugin {
    Logger _logger = getLogger();
    Server _server = getServer();

    Collection<NamespacedKey> _recipeNamespaces;

    private NamespacedKey registerCustomRecipe(Material item, @NotNull Map<Character, Material> ingredients, String... shape) {
        _logger.info(String.format("Creating custom recipe for item %s", item));

        NamespacedKey nsKey = new NamespacedKey(this, item.name());

        // Create a new item stack
        ItemStack is = new ItemStack(item);
        // Create a new recipe for the item stack
        ShapedRecipe recipe = new ShapedRecipe(nsKey, is);

        // Set the recipe shape from the functions varargs
        recipe.shape(shape);

        // Loop over all the ingredients in the map, and register each one
        for (Map.Entry<Character, Material> ingredient : ingredients.entrySet()) {

            char key = ingredient.getKey();
            Material material = ingredient.getValue();

            _logger.info(String.format("Registering ingredient %c -> %s", key, material));
            recipe.setIngredient(key, material);
        }
        _server.addRecipe(recipe);

        return nsKey;
    }

    private NamespacedKey registerTNTRecipe() {
        Map<Character, Material> TNTIngredients = Map.of(
                'p', Material.PAPER,
                's', Material.SAND,
                'g', Material.GUNPOWDER
        );
        return registerCustomRecipe(
                Material.TNT,
                TNTIngredients,
                "psp", "sgs", "psp"
        );
    }

    private NamespacedKey registerNameTagRecipe() {
        Map<Character, Material> ingredients = Map.of(
                's', Material.STRING,
                'p', Material.PAPER
        );
        return registerCustomRecipe(
                Material.NAME_TAG,
                ingredients,
                "s", "p"
        );
    }

    private NamespacedKey registerSaddleRecipe() {
        Map<Character, Material> ingredients = Map.of('l', Material.LEATHER);
        return registerCustomRecipe(
                Material.SADDLE,
                ingredients,
                " l ", "l l"
        );
    }

    private void removeMaterialRecipe(Material material) {
        // Iterate over all recipes and delete the matching ones
        Iterator<Recipe> it = _server.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == material) {
                it.remove();
            }
        }
    }

    @Override
    public void onEnable() {
        _logger.info("Initializing TripleLife custom recipes!");
        _recipeNamespaces = new ArrayList<NamespacedKey>();
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);

        removeMaterialRecipe(Material.ENCHANTING_TABLE);

        _recipeNamespaces.add(registerTNTRecipe());
        _recipeNamespaces.add(registerNameTagRecipe());
        _recipeNamespaces.add(registerSaddleRecipe());
    }
}

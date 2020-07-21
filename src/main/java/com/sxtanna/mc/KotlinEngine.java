package com.sxtanna.mc;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.logging.Level;

public final class KotlinEngine
{

	private final Plugin plugin;

	private final String       cached;
	private final ScriptEngine engine;
	private final KotlinScript script;


	public KotlinEngine(final Plugin plugin, final ScriptEngine engine, final KotlinScript script)
	{
		this.plugin = plugin;
		this.engine = engine;
		this.script = script;

		this.cached = KotlinScriptConstruct.compile(script);
	}


	public String eval(final OfflinePlayer player, final String[] parameters)
	{
		final String script = PlaceholderAPI.setPlaceholders(player, cached);

		engine.put("player", player);
		engine.put("plugin", plugin);
		engine.put("params", parameters);

		try
		{
			final Object result = engine.eval(script);

			return result == null ? "" : PlaceholderAPI.setPlaceholders(player, result.toString());
		}
		catch (final ScriptException ex)
		{
			plugin.getLogger().log(Level.WARNING, "failed to evaluate kotlinscript", ex);
		}

		return "KotlinScript Error!";
	}

}

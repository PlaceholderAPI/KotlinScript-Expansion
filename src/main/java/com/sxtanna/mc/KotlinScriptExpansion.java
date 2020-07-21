package com.sxtanna.mc;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.kotlin.cli.common.environment.UtilKt;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public final class KotlinScriptExpansion extends PlaceholderExpansion
{

	private final Map<String, KotlinScript>          scripts = new HashMap<>();
	private final LoadingCache<String, KotlinEngine> engines = CacheBuilder.newBuilder().build(new CacheLoader<String, KotlinEngine>()
	{
		@Override
		public KotlinEngine load(final String key) throws Exception
		{
			final KotlinScript script = scripts.get(key);
			if (script == null)
			{
				throw new IllegalArgumentException("Could not find script with identifier: " + key);
			}

			return new KotlinEngine(getPlaceholderAPI(), new KotlinJsr223JvmLocalScriptEngineFactory().getScriptEngine(), script);
		}
	});


	@Override
	public String getIdentifier()
	{
		return "kotlin";
	}

	@Override
	public String getAuthor()
	{
		return "Sxtanna";
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}

	@Override
	public String onRequest(final OfflinePlayer player, final String params)
	{
		if (player == null || scripts.size() == 0)
		{
			return "";
		}

		final String[] parameters = params.split("_");

		try
		{
			final KotlinEngine engine = engines.get(parameters[0]);

			return engine.eval(player, Arrays.copyOfRange(parameters, 1, parameters.length));
		}
		catch (final ExecutionException ex)
		{
			return "";
		}
	}


	@Override
	public boolean register()
	{
		final String serverJarPath;
		final String expansionPath;
		try
		{
			serverJarPath = Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			expansionPath = KotlinScriptExpansion.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		}
		catch (final URISyntaxException ex)
		{
			getPlaceholderAPI().getLogger().log(Level.SEVERE, "Could not resolve jar paths", ex);
			return false;
		}

		UtilKt.setIdeaIoUseFallback();

		try
		{
			final ClassLoader loader = getPlaceholderAPI().getClass().getClassLoader();

			final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);

			method.invoke(loader, new File(expansionPath).toURI().toURL());
		}
		catch (final Exception ex)
		{
			getPlaceholderAPI().getLogger().log(Level.SEVERE, "failed to put the expansion jar on the classpath", ex);
			return false;
		}

		System.setProperty("kotlin.script.classpath", serverJarPath + ";" + expansionPath);

		final File folder = new File(getPlaceholderAPI().getDataFolder(), "kotlinscripts");
		if (!folder.exists() && !folder.mkdirs())
		{
			getPlaceholderAPI().getLogger().warning("failed to create kotlinscripts directory.");
			return false;
		}


		final File[] scriptFiles = folder.listFiles(file -> file.getName().endsWith(".kts"));
		if (scriptFiles == null)
		{
			getPlaceholderAPI().getLogger().warning("kotlinscripts directory is a file.");
			return false;
		}

		for (final File scriptFile : scriptFiles)
		{
			final String source;

			try
			{
				//noinspection UnstableApiUsage
				source = Files.toString(scriptFile, StandardCharsets.UTF_8);
			}
			catch (final IOException ex)
			{
				getPlaceholderAPI().getLogger().log(Level.WARNING, "failed to read kotlinscript file " + scriptFile, ex);
				continue;
			}


			final String identifier = scriptFile.getName().toLowerCase().replace(".kts", "");

			final KotlinScript script = KotlinScriptConstruct.resolve(source);
			scripts.put(identifier, script);

			getPlaceholderAPI().getLogger().info("loaded kotlinscript " + identifier);
		}

		return super.register();
	}

}

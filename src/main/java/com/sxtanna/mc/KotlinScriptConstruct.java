package com.sxtanna.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this entire class is to allow for JS like hoisting of imports, aliases, and classes in kotlin scripts
 */
public final class KotlinScriptConstruct
{

	private static final Pattern IMPORTS_EXPRESSION = Pattern.compile("import (((\\w+)(\\.?))+(\\*)?)");
	private static final Pattern ALIASES_EXPRESSION = Pattern.compile("typealias \\w+ = (((\\w+)(\\.?))+(\\*)?)");
	private static final Pattern CLASSES_EXPRESSION = Pattern.compile("((private|protected|public|internal)(( )+))?((enum|sealed|data)(( )+))?((class|interface)(( )+))\\w+(( )+)?(\\n)*?(\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)|\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*\\})*\\})*\\})(\\n)*(\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*\\})*\\})*\\})?");

	private static final String[] DEFAULT_IMPORTS = {
			"// kotlin packages",
			"import kotlin.reflect.*",
			"import kotlin.reflect.jvm.*",
			"import kotlin.reflect.full.*",
			"// bukkit package",
			"import org.bukkit.*",
			"import org.bukkit.block.*",
			"import org.bukkit.entity.*",
			"import org.bukkit.event.*",
			"import org.bukkit.inventory.*",
			"import org.bukkit.inventory.meta.*",
			"import org.bukkit.scheduler.*",
			"import org.bukkit.scoreboard.*",
			"import org.bukkit.util.*",
			"import org.bukkit.plugin.*",
			"// bukkit events",
			"import org.bukkit.event.block.*",
			"import org.bukkit.event.command.*",
			"import org.bukkit.event.enchantment.*",
			"import org.bukkit.event.entity.*",
			"import org.bukkit.event.hanging.*",
			"import org.bukkit.event.inventory.*",
			"import org.bukkit.event.player.*",
			"import org.bukkit.event.raid.*",
			"import org.bukkit.event.server.*",
			"import org.bukkit.event.vehicle.*",
			"import org.bukkit.event.weather.*",
			"import org.bukkit.event.world.*",
			"// spigot events",
			"import org.spigotmc.event.entity.*",
			"import org.spigotmc.event.player.*",
			};

	private KotlinScriptConstruct()
	{}


	public static KotlinScript resolve(final String source)
	{
		String script = source;

		final List<String> imports = matchResults(IMPORTS_EXPRESSION, source);
		final List<String> aliases = matchResults(ALIASES_EXPRESSION, source);
		final List<String> classes = matchResults(CLASSES_EXPRESSION, source);

		for (final String match : imports)
		{
			script = script.replace(match, "");
		}
		for (final String match : aliases)
		{
			script = script.replace(match, "");
		}
		for (final String match : classes)
		{
			script = script.replace(match, "");
		}

		script = script.trim();

		return new KotlinScript(source, script, imports, aliases, classes);
	}

	public static String compile(final KotlinScript script)
	{
		final StringBuilder builder = new StringBuilder();

		builder.append(String.join("\n", DEFAULT_IMPORTS)).append('\n');

		builder.append("// custom imports").append('\n');
		builder.append(script.getImports().isEmpty() ? "// none"  : String.join("\n", script.getImports())).append('\n');

		builder.append("// custom aliases").append('\n');
		builder.append(script.getAliases().isEmpty() ? "// none"  : String.join("\n", script.getAliases())).append('\n');

		builder.append("// custom classes").append('\n');
		builder.append(script.getClasses().isEmpty() ? "// none"  : String.join("\n", script.getClasses())).append('\n');


		builder.append("val params = bindings[\"params\"] as Array<String>").append('\n');
		builder.append("val player = bindings[\"player\"] as Player").append('\n');
		builder.append("val plugin = bindings[\"plugin\"] as Plugin").append('\n');

		builder.append(script.getScript()).append('\n');

		return builder.toString();
	}


	private static List<String> matchResults(final Pattern pattern, final String text)
	{
		final List<String> results = new ArrayList<>();

		final Matcher matcher = pattern.matcher(text);

		while (matcher.find())
		{
			results.add(matcher.group());
		}

		return results;
	}

}

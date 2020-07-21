package com.sxtanna.mc;

import java.util.List;

public final class KotlinScript
{

	private final String source;
	private final String script;

	private final List<String> imports;
	private final List<String> aliases;
	private final List<String> classes;


	public KotlinScript(final String source, final String script, final List<String> imports, final List<String> aliases, final List<String> classes)
	{
		this.source  = source;
		this.script  = script;
		this.imports = imports;
		this.aliases = aliases;
		this.classes = classes;
	}


	public String getSource()
	{
		return source;
	}

	public String getScript()
	{
		return script;
	}

	public List<String> getImports()
	{
		return imports;
	}

	public List<String> getAliases()
	{
		return aliases;
	}

	public List<String> getClasses()
	{
		return classes;
	}

}

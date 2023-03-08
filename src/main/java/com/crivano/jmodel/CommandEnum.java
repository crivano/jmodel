package com.crivano.jmodel;

public enum CommandEnum {
	FIELD("field", "@field", true, true, "[", "/]"),
	VALUE("value", "@value", true, true, "[", "/]"),
	PRINT("print", "@print", true, true, "[", "/]"),
	GROUP_BEGIN("group", "@group", false, true, "[", "]"),
	GROUP_END("/group", "/@group", false, false, "[", "]"),
	IF_BEGIN("if", "@if", true, true, "[", "]"),
	IF_END("/if", "/@if", false, false, "[", "]"),
	FOR_BEGIN("for", "@for", true, true, "[", "]"),
	FOR_END("/for", "/@for", false, false, "[", "]"),
	DESCRIPTION_BEGIN("description", "@description", false, true, "[", "]"),
	DESCRIPTION_END("/description", "/@description", false, false, "[", "]"),
	HOOK_BEGIN("hook", "@hook", true, true, "[", "]"),
	HOOK_END("/hook", "/@hook", false, false, "[", "]"),
	SET("set", "@set", true, true, "[", "/]");

	String command;
	String ftlCommand;
	boolean requireParams;
	boolean acceptParams;
	String ftlPrefix;
	String ftlSuffix;

	static final String PREFIX_FIELD = FIELD.getStart();
	static final String PREFIX_VALUE = VALUE.getStart();
	static final String PREFIX_SET = SET.getStart();
	static final String PREFIX_GROUP_BEGIN = GROUP_BEGIN.getStart();
	static final String PREFIX_GROUP_END = GROUP_END.getStart();

	static String INTERVIEW_COMMANDS[] = new String[] { FIELD.getStart(), GROUP_BEGIN.getStart(), GROUP_END.getStart(),
			IF_BEGIN.getStart(), IF_END.getStart(), FOR_BEGIN.getStart(), FOR_END.getStart() };
	static String HEAD_COMMANDS[] = new String[] { CommandEnum.PREFIX_SET };

	CommandEnum(String command, String ftlCommand, boolean requireParams, boolean acceptParams, String ftlPrefix,
			String ftlSuffix) {
		this.command = command;
		this.ftlCommand = ftlCommand;
		this.requireParams = requireParams;
		this.acceptParams = acceptParams;
		this.ftlPrefix = ftlPrefix;
		this.ftlSuffix = ftlSuffix;
	}

	public String getStart() {
		return ftlPrefix + ftlCommand + (requireParams ? " " : "") + (acceptParams ? "" : ftlSuffix);
	}

	public boolean match(String s) {
		if (s == null)
			return false;
		return s.startsWith(getStart());
	}

	public static CommandEnum getCommandFromName(String name) {
		for (CommandEnum c : values())
			if (c.command.equals(name))
				return c;
		return null;
	}

}

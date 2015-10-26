package org.unrecoverable.tools.e3648;

public interface Connection {

	public void sendCommand(final String iCommand) throws CommandException;

	public String sendRequest(final String iCommand) throws CommandException;
}

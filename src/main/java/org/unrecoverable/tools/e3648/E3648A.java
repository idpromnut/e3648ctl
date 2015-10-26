package org.unrecoverable.tools.e3648;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E3648A implements Instrument {
	private static final Logger LOGGER = LoggerFactory.getLogger(E3648A.class);

	private Connection connection;
	private PowerSupply output1;
	private PowerSupply output2;

	public E3648A(final Connection iConnection) {
		connection = iConnection;
		output1 = new AgilentPowerSupply(connection, 1);
		output2 = new AgilentPowerSupply(connection, 2);
	}

	@Override
	public void remote() {
		try {
			connection.sendCommand("SYST:REM");
			LOGGER.debug("enable remote instrument control");
		} catch (CommandException e) {
			LOGGER.error("could not enable remote instrument control", e);
		}
	}

	@Override
	public void local() {
		try {
			connection.sendCommand("SYST:LOC");
			LOGGER.debug("enable local instrument control");
		} catch (CommandException e) {
			LOGGER.error("could not enable local instrument control", e);
		}
	}

	@Override
	public void reset() {
		try {
			connection.sendCommand("*RST");
			LOGGER.debug("reset instrument");
		} catch (CommandException e) {
			LOGGER.error("could not reset instrument", e);
		}
	}

	public void readErrors() {
		try {
			LOGGER.debug("read errors: {}", connection.sendRequest("SYST:ERR?"));
		} catch (CommandException e) {
			LOGGER.error("could not clear errors from instrument", e);
		}
	}

	@Override
	public void clearErrors() {
		try {
			connection.sendCommand("*CLS");
			LOGGER.debug("clear errors");
		} catch (CommandException e) {
			LOGGER.error("could not clear errors from instrument", e);
		}
	}

	public void displayMessage(String iMessage) {
		try {
			connection.sendCommand("DISP:TEXT '" + iMessage + "'");
			LOGGER.debug("set instrument display: {}", iMessage);
		} catch (CommandException e) {
			LOGGER.error("could not update instrument display with: {}", iMessage, e);
		}
	}

	public void clearDisplay() {
		try {
			connection.sendCommand("DISP:MODE VI");
			LOGGER.debug("clear instrument display");
		} catch (CommandException e) {
			LOGGER.error("could not clear instrument display", e);
		}
	}

	public List<PowerSupply> getOutputs() {
		List<PowerSupply> lPowerSupplies = new ArrayList<>();
		lPowerSupplies.add(output1);
		lPowerSupplies.add(output2);
		return lPowerSupplies;
	}
}

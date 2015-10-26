package org.unrecoverable.tools.e3648;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgilentPowerSupply implements PowerSupply {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgilentPowerSupply.class);

	private Connection connection;
	private int outputAddress;

	public AgilentPowerSupply(final Connection iConnection, int iOutputAddress) {

		if (iOutputAddress < 1 || iOutputAddress > 2) throw new IllegalArgumentException("invalid output address; must be eith 1 or 2");
		connection = iConnection;
		outputAddress = iOutputAddress;
	}

	@Override
	public void setOutput(boolean iState) {
		final String lState = iState ? "ON" : "OFF";
		try {
			connection.sendCommand("OUTP " + lState);
			LOGGER.debug("setOutput(): {}", lState);
		} catch (CommandException e) {
			LOGGER.error("setOutput() failed: {}", lState, e);
		}
	}

	@Override
	public boolean getOutput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVoltage(double iVoltage) {
		try {
			connection.sendCommand("VOLT " + iVoltage);
			LOGGER.debug("setVoltage(): {}", iVoltage);
		} catch (CommandException e) {
			LOGGER.error("setVoltage() failed: {}", iVoltage, e);
		}
	}

	@Override
	public void setCurrent(double iCurrent) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getVoltage() {
		double lVoltage = 0.0;
		try {
			String lResponse = connection.sendRequest("VOLT?");
			lVoltage = Double.parseDouble(lResponse);
			LOGGER.debug("getVoltage(): {}", lVoltage);
		} catch (CommandException e) {
			LOGGER.error("getVoltage() failed", e);
		}

		return lVoltage;
	}

	@Override
	public double getCurrent() {
		double lCurrent = 0.0;
		try {
			String lResponse = connection.sendRequest("CURR?");
			lCurrent = Double.parseDouble(lResponse);
			LOGGER.debug("getCurrent(): {}", lCurrent);
		} catch (CommandException e) {
			LOGGER.error("getCurrent() failed", e);
		}

		return lCurrent;
	}

	@Override
	public double measureVoltage() {
		double lVoltage = 0.0;
		try {
			String lResponse = connection.sendRequest("MEAS:VOLT?");
			lVoltage = Double.parseDouble(lResponse);
			LOGGER.debug("measureVoltage(): {}", lVoltage);
		} catch (CommandException e) {
			LOGGER.error("measureVoltage() failed", e);
		}

		return lVoltage;
	}

	@Override
	public double measureCurrent() {
		double lCurrent = 0.0;
		try {
			String lResponse = connection.sendRequest("MEAS:CURR?");
			lCurrent = Double.parseDouble(lResponse);
			LOGGER.debug("measureCurrent(): {}", lCurrent);
		} catch (CommandException e) {
			LOGGER.error("measureCurrent() failed", e);
		}

		return lCurrent;
	}


	protected void setCurrentOutput() throws CommandException {
		connection.sendCommand("INST:NSEL " + outputAddress);

		// verify that the correct output is selected
		String lSelectedOutput = connection.sendRequest("INST:NSEL?");
		if (!("+" + outputAddress).equalsIgnoreCase(lSelectedOutput)) {
			throw new CommandException("output address could not be selected");
		}
	}
}

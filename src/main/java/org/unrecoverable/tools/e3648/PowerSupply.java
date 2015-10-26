package org.unrecoverable.tools.e3648;

public interface PowerSupply {

	/**
	 * Turns the output of this power supply on or off.
	 * @param iState true to turn output on, false to turn it off.
	 */
	public void setOutput(final boolean iState);

	/**
	 * Returns the state of the output for this power supply (on/off)
	 * @return true of output is on, false if it is off.
	 */
	public boolean getOutput();

	/**
	 * Sets the voltage for this power supply.
	 * @param iVoltage set voltage in volts.
	 */
	public void setVoltage(final double iVoltage);

	/**
	 * Sets the current for this power supply.
	 * @param iCurrent the set current in amps.
	 */
	public void setCurrent(final double iCurrent);

	/**
	 * Returns the set voltage for this power supply.
	 * @return the set voltage in volts.
	 */
	public double getVoltage();

	/**
	 * Returns the set current for this power supply.
	 * @return set current in amps.
	 */
	public double getCurrent();

	/**
	 * Returns the measured voltage on the output.
	 * @return number of volts present on the output.
	 */
	public double measureVoltage();

	/**
	 * Returns the measured current being sourced from the output.
	 * @return number of amps being sourced from the output.
	 */
	public double measureCurrent();
}

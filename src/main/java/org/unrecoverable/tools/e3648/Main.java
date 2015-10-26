package org.unrecoverable.tools.e3648;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPort;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		// update path with correct library
		addLibraryPath("./lib/" + System.getProperty("os.arch"));

		AgilentRS232Connection lConnection = new AgilentRS232Connection();
		lConnection.connect("COM4", 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		E3648A lPowerSupply = new E3648A(lConnection);

		double lVoltage = 1.5;
		final double lIncrement = 0.01;
		double lCurrentOffset = 0.0;
		double lCurrentMeasurement;
		final NumberFormat lFormatter = NumberFormat.getNumberInstance();
		lFormatter.setMinimumFractionDigits(3);

		File lMeasurementOutputFile = new File("output.csv");
		Writer lMeasurementOutput = new FileWriter(lMeasurementOutputFile);

		lPowerSupply.reset();
		lPowerSupply.clearErrors();
		lPowerSupply.remote();
		PowerSupply lOutput = lPowerSupply.getOutputs().get(0);
		lOutput.setVoltage(lVoltage);
		lOutput.setOutput(true);
//		lCurrentOffset = -1 * lOutput.measureCurrent();
		lMeasurementOutput.write("Input Voltage (V)");
		lMeasurementOutput.write(",");
		lMeasurementOutput.write("Input Current (I)");
		lMeasurementOutput.write("\n");
		while(lVoltage > 0.6) {
			lVoltage = lVoltage - lIncrement;
			lOutput.setVoltage(lVoltage);
			lCurrentMeasurement = lOutput.measureCurrent() + lCurrentOffset;
			LOGGER.info("Measurement: {} V,{} A", lFormatter.format(lVoltage), lFormatter.format(lCurrentMeasurement));
			lMeasurementOutput.write(lFormatter.format(lVoltage));
			lMeasurementOutput.write(",");
			lMeasurementOutput.write(lFormatter.format(lCurrentMeasurement));
			lMeasurementOutput.write("\n");
			Thread.sleep(100);
		}

		IOUtils.closeQuietly(lMeasurementOutput);
		lOutput.setOutput(false);
		lPowerSupply.local();
		lConnection.disconnect();
		System.exit(0);
	}

	/**
	* Adds the specified path to the java library path
	*
	* @param pathToAdd the path to add
	* @throws Exception
	*/
	public static void addLibraryPath(String pathToAdd) throws Exception{
	    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
	    usrPathsField.setAccessible(true);

	    //get array of paths
	    final String[] paths = (String[])usrPathsField.get(null);

	    //check if the path to add is already present
	    for(String path : paths) {
	        if(path.equals(pathToAdd)) {
	            return;
	        }
	    }

	    //add the new path
	    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
	    newPaths[newPaths.length-1] = pathToAdd;
	    usrPathsField.set(null, newPaths);
	}
}

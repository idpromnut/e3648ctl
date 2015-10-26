package org.unrecoverable.tools.e3648;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class AgilentRS232Connection implements Connection {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgilentRS232Connection.class);

	private SerialPort serialPort;
	private OutputStream output;
	private InputStream input;

	private BlockingQueue<String> readMessages = new LinkedBlockingQueue<>();

	@Override
	public String sendRequest(String iCommand) throws CommandException {
		try {
			readMessages.clear();
			sendCommand(iCommand);
			return readMessages.poll(1000, TimeUnit.MILLISECONDS);
		}
		catch(InterruptedException e) {
			LOGGER.error("send request failed: {}", iCommand, e);
		}

		return null;
	}

	@Override
	public void sendCommand(String iCommand) throws CommandException {
		try {
			while(!serialPort.isDSR()) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
				}
			}
			output.write((iCommand + "\r\n").getBytes());
			output.flush();
			while(serialPort.isDSR()) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
				}
			}
		}
		catch(IOException e) {
			LOGGER.error("send command failed: {}", iCommand, e);
		}
	}

	public void connect(final String iPortName, final int iBaudRate, final int iDataBits, final int iStopBits, final int iParity) throws IOException {
		try {
			CommPortIdentifier lPortIdentifier = CommPortIdentifier.getPortIdentifier(iPortName);
			CommPort lCommPort;
			SerialPort lSerialPort;

			if (lPortIdentifier.isCurrentlyOwned()) {
				throw new IOException("port is currently in use");
			} else {
				lCommPort = lPortIdentifier.open(this.getClass().getName(), 2000);

				if (lCommPort instanceof SerialPort) {
					lSerialPort = (SerialPort) lCommPort;
					lSerialPort.setSerialPortParams(iBaudRate, iDataBits,
							iStopBits, iParity);
					lSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
					serialPort = lSerialPort;
					output = serialPort.getOutputStream();
					input = serialPort.getInputStream();
					serialPort.addEventListener(new SerialPortReader());
					serialPort.notifyOnDataAvailable(true);
					serialPort.notifyOnCTS(true);
					serialPort.notifyOnDSR(true);
					serialPort.setDTR(true);
					serialPort.setRTS(true);
					LOGGER.info("connection on {} open and ready ({},{},{},{})", iPortName, iBaudRate, iDataBits, iStopBits, iParity);
				} else {
					throw new IOException("new config points to a non-serial port and only serial ports are supported");
				}
			}
		} catch (PortInUseException e) {
			throw new IOException("port " + iPortName + " is already in use");
		} catch (NoSuchPortException e) {
			throw new IOException("no such port " + iPortName);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("can not open port " + iPortName, e);
		} catch (TooManyListenersException e) {
			throw new IOException("can attach read listener to port " + iPortName, e);
		}
	}

	public void disconnect() {
		if (serialPort != null) {
			serialPort.setDTR(false);
			serialPort.setRTS(false);
			serialPort.close();
			LOGGER.debug("disconnect from {}", serialPort.getName());
		}
	}

	private String printableSerialPortEventType(int iSerialEventType) {

		switch (iSerialEventType) {
			case SerialPortEvent.BI: return "BI";
			case SerialPortEvent.CD: return "CD";
			case SerialPortEvent.CTS: return "CTS";
			case SerialPortEvent.DATA_AVAILABLE: return "DATA";
			case SerialPortEvent.DSR: return "DSR";
			case SerialPortEvent.FE: return "FE";
			case SerialPortEvent.OE: return "OE";
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: return "OUTPUT_BUF";
			case SerialPortEvent.PE: return "PE";
			case SerialPortEvent.RI: return "RI";
			default: return "UNKNOWN";
		}
	}

	class SerialPortReader implements SerialPortEventListener {
		private StringBuilder readBuffer = new StringBuilder();

		@Override
		public void serialEvent(SerialPortEvent iEvent) {
			if (SerialPortEvent.DATA_AVAILABLE == iEvent.getEventType()) {
				try {
					int data;
					while((input.available() > 0) && ((data = input.read()) > -1)) {
						readBuffer.append((char)data);
						if ("\r\n".equals(readBuffer.substring(readBuffer.length() - 2))) {
							readMessages.offer(readBuffer.toString().trim());
							LOGGER.trace("received message: {}", readBuffer.toString());
							readBuffer.delete(0, readBuffer.length());
						}
						else {
							LOGGER.trace("did not receive EOL; current buffer is: {}", readBuffer.toString());
						}
					}
				} catch (IOException e) {
					LOGGER.error("error while reading input", e);
				}
			}
			else if (SerialPortEvent.DSR == iEvent.getEventType()) {
				LOGGER.trace("received non-data event: {}, {}->{}",
				printableSerialPortEventType(iEvent.getEventType()),
				iEvent.getOldValue(),
				iEvent.getNewValue());
			}
		}
	}
}

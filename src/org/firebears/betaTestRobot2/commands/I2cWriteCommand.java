package org.firebears.betaTestRobot2.commands;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Write the given byte array out to an I2C address.
 * Typically, this writes a sequence of bytes to an Arduino.
 */
public class I2cWriteCommand extends Command {
	
	private final I2C i2c;
	private final ByteBuffer dataToSend;
	private final int sendSize;
	private final ByteBuffer dataReceived;
	private final int receiveSize;

	public I2cWriteCommand(I2C i2c, byte[] bytes) {
		this.i2c = i2c;
		sendSize = bytes.length;
		dataToSend = ByteBuffer.allocateDirect(sendSize);
		for (int i=0; i<sendSize; i++) {
			dataToSend.put(i, bytes[i]);
		}
		receiveSize = 0;
		dataReceived = ByteBuffer.allocateDirect(0);
	}
	
	@Override
	protected void execute() {
		i2c.transaction(dataToSend, sendSize, dataReceived, receiveSize);
	}
	
	@Override
	protected boolean isFinished() {
		return true;
	}
}

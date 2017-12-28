package org.firebears.util;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;

/**
 * Wrapper class around {@link TalonSRX} that properly implements {@link SpeedController} 
 * and {@link Sendable}.
 * @author Keith Rieck
 */
public class CANTalon implements SpeedController, Sendable {

	private final int timeoutMs = 100;
	private final TalonSRX talonSRX;
	private final int deviceNumber;
	private ControlMode controlMode;
	private double currentSpeed = 0.0;
	private String name = null;
	private String subsystem = null;
	private int encoderMultiplier = 1;
	private int pidIdx = 0;

	public CANTalon(int deviceNumber) {
		talonSRX = new TalonSRX(deviceNumber);
		this.deviceNumber = deviceNumber;
		this.controlMode = ControlMode.PercentOutput;
	}

	public void changeControlMode(ControlMode talonControlMode) {
		this.controlMode = talonControlMode;
	}
	
	@Deprecated
	public void clearStickyFaults() {
		talonSRX.clearStickyFaults(timeoutMs);
	}
	
	public void configEncoderCodesPerRev(int ticks) {
		this.encoderMultiplier = ticks;
	}
	
	@Deprecated
	public void configNominalOutputVoltage(double forwardVoltage, double reverseVoltage) {
		// ????  This doesn't seem to be available
	}
	
	@Deprecated
	public void configPeakOutputVoltage(double forwardVoltage, double reverseVoltage) {
		// ????  This doesn't seem to be available
	}

	@Override
	public void disable() {
		talonSRX.neutralOutput();
	}

	public void enable() {
		talonSRX.set(controlMode, currentSpeed);
	}

	public void enableBrakeMode(boolean brakeEnabled) {
		talonSRX.setNeutralMode(brakeEnabled ? NeutralMode.Brake : NeutralMode.Coast);
	}

	@Override
	public double get() {
		return currentSpeed;
	}

	@Override
	public boolean getInverted() {
		return talonSRX.getInverted();
	}

	public String getName() {
		return name;
	}

	public double getOutputCurrent() {
		return talonSRX.getOutputCurrent();
	}

	/**
	 * @return Position of selected sensor (in Raw Sensor Units).
	 */
	public int getSelectedSensorPosition() {
		return talonSRX.getSelectedSensorPosition(pidIdx);
	}

	/**
	 * @return Velocity of selected sensor (in Raw Sensor Units per 100 ms).
	 */
	public int getSelectedSensorVelocity() {
		return talonSRX.getSelectedSensorVelocity(pidIdx);
	}
	
	public String getSmartDashboardType() {
		return "Speed Controller";
	}

	public String getSubsystem() {
		return subsystem;
	}
	
	public void initSendable(SendableBuilder builder) {
	    builder.setSmartDashboardType("Speed Controller");
	    builder.setSafeState(() -> disable());
	    builder.addDoubleProperty("Value", () -> get(), (value) -> set(value));
	  }

	@Override
	public void pidWrite(double speed) {
		currentSpeed = speed;
		talonSRX.set(controlMode, currentSpeed);
	}

	public void reverseSensor(boolean isReversed) {
		talonSRX.setSensorPhase(isReversed);
	}

	@Override
	public void set(double speed) {
		currentSpeed = speed;
		talonSRX.set(controlMode, currentSpeed * encoderMultiplier);
	}

	public void setFeedbackDevice(FeedbackDevice feedbackDevice) {
		talonSRX.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
	}

	@Override
	public void setInverted(boolean isInverted) {
		talonSRX.setInverted(isInverted);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setPID(double pidP, double pidI, double pidD, double pidF, int pidIZone, double pidRampRate,
			int slotIdx) {
		talonSRX.config_kP(slotIdx, pidP, timeoutMs);
		talonSRX.config_kI(slotIdx, pidI, timeoutMs);
		talonSRX.config_kD(slotIdx, pidD, timeoutMs);
		talonSRX.config_kF(slotIdx, pidF, timeoutMs);
		talonSRX.config_IntegralZone(slotIdx, pidIZone, timeoutMs);
		talonSRX.configClosedloopRamp(pidRampRate, timeoutMs);
		talonSRX.selectProfileSlot(slotIdx, pidIdx);
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}

	@Override
	public void stopMotor() {
		talonSRX.neutralOutput();
	}

	@Override
	public String toString() {
		return "CANTalon(" + deviceNumber 
				+ (subsystem != null ? "," + subsystem : "")
				+ (name != null ? "," + name : "") + ")";
	}

}

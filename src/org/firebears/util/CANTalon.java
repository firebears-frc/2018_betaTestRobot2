package org.firebears.util;

import java.util.function.Consumer;

import com.ctre.phoenix.MotorControl.ControlMode;
import com.ctre.phoenix.MotorControl.FeedbackDevice;
import com.ctre.phoenix.MotorControl.NeutralMode;
import com.ctre.phoenix.MotorControl.CAN.TalonSRX;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;

public class CANTalon implements SpeedController, Sendable {

	private final int timeoutMs = 100;
	private final TalonSRX talonSRX;
	private final int deviceNumber;
	private NetworkTable networkTable;
	private ControlMode controlMode;
	private com.ctre.phoenix.MotorControl.FeedbackDevice feedbackDevice;
	private NetworkTableEntry m_valueEntry;
	private int m_valueListener;
	private double currentSpeed = 0.0;
	private String name = null;
	private String subsystem = null;

	public CANTalon(int deviceNumber) {
		talonSRX = new TalonSRX(deviceNumber);
		this.deviceNumber = deviceNumber;
		this.controlMode = ControlMode.PercentOutput;
	}

	public void changeControlMode(ControlMode talonControlMode) {
		this.controlMode = null;
		for (ControlMode cm : ControlMode.values()) {
			if (talonControlMode.value == cm.value) {
				this.controlMode = cm;
				break;
			}
		}
	}
	
	@Deprecated
	public void clearStickyFaults() {
		talonSRX.clearStickyFaults(timeoutMs);
	}
	
	public void configEncoderCodesPerRev(int ticks) {
		// ????
	}
	
	public void configNominalOutputVoltage(double forwardVoltage, double reverseVoltage) {
		// ????
	}
	
	public void configPeakOutputVoltage(double forwardVoltage, double reverseVoltage) {
		// ????
	}

	@Override
	public void disable() {
		talonSRX.neutralOutput();
	}

	public void enable() {
		// ????
	}

	public void enableBrakeMode(boolean brakeEnabled) {
		talonSRX.setNeutralMode(brakeEnabled ? NeutralMode.Brake : NeutralMode.Coast);
	}

	@Override
	public double get() {
		return currentSpeed;
	}

	public int getEncPosition() {
		return talonSRX.getSelectedSensorPosition();
	}

	public int getEncVelocity() {
		return talonSRX.getSelectedSensorVelocity();
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
		talonSRX.set(ControlMode.PercentOutput, currentSpeed);
	}

	public void reverseSensor(boolean b) {
		// ????
	}

	@Override
	public void set(double speed) {
		currentSpeed = speed;
		talonSRX.set(ControlMode.PercentOutput, currentSpeed);
	}

	public void setFeedbackDevice(FeedbackDevice feedbackDevice) {
		talonSRX.configSelectedFeedbackSensor(feedbackDevice, timeoutMs);
	}

	@Override
	public void setInverted(boolean isInverted) {
		talonSRX.setInverted(isInverted);
	}

	public void setName(String s) {
		name = s;
	}

	public void setPID(double pidP, double pidI, double pidD, double pidF, int pidIZone, double pidRampRate,
			int pidProfileSlot) {
		talonSRX.config_kP(pidProfileSlot, pidP, timeoutMs);
		talonSRX.config_kI(pidProfileSlot, pidI, timeoutMs);
		talonSRX.config_kD(pidProfileSlot, pidD, timeoutMs);
		talonSRX.config_kF(pidProfileSlot, pidF, timeoutMs);
		talonSRX.config_IntegralZone(pidProfileSlot, pidIZone, timeoutMs);
		talonSRX.configClosedloopRamp(pidRampRate, timeoutMs);
		talonSRX.selectProfileSlot(pidProfileSlot);
	}

	public void setSubsystem(String s) {
		subsystem = s;
	}

	@Override
	public void stopMotor() {
		talonSRX.neutralOutput();
	}

	@Override
	public String toString() {
		return "CANTalon(" + deviceNumber + ")";
	}

}

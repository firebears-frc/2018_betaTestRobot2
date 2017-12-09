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
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

public class CANTalon implements SpeedController, LiveWindowSendable {

	private final int timeoutMs = 100;
	private final TalonSRX talonSRX;
	private final int deviceNumber;
	private NetworkTable networkTable;
	private ControlMode controlMode;
	private com.ctre.phoenix.MotorControl.FeedbackDevice feedbackDevice;
	private NetworkTableEntry m_valueEntry;
	private int m_valueListener;
	private double currentSpeed = 0.0;

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

	public double getOutputCurrent() {
		return talonSRX.getOutputCurrent();
	}

	@Override
	public String getSmartDashboardType() {
		return "Speed Controller";
	}

	@Override
	public void initTable(NetworkTable subtable) {
		this.networkTable = subtable;
		if (subtable != null) {
			m_valueEntry = subtable.getEntry("Value");
			updateTable();
		} else {
			m_valueEntry = null;
		}
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

	@Override
	public void startLiveWindowMode() {
		stopMotor();
		final Consumer<EntryNotification> listener = new Consumer<EntryNotification>() {
			public void accept(EntryNotification event) {
				set(event.value.getDouble());
			}
		};
		m_valueListener = m_valueEntry.addListener(listener,
				EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
	}

	@Override
	public void stopLiveWindowMode() {
		stopMotor();
		m_valueEntry.removeListener(m_valueListener);
		m_valueListener = 0;
	}

	@Override
	public void stopMotor() {
		talonSRX.neutralOutput();
	}

	@Override
	public String toString() {
		return "CANTalon(" + deviceNumber + ")";
	}

	@Override
	public void updateTable() {
		if (m_valueEntry != null) {
			m_valueEntry.setDouble(get());
		}
	}
}

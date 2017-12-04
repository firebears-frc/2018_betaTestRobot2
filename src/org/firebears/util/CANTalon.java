package org.firebears.util;

import java.util.function.Consumer;

import com.ctre.phoenix.MotorControl.ControlMode;
import com.ctre.phoenix.MotorControl.NeutralMode;
import com.ctre.phoenix.MotorControl.CAN.TalonSRX;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

/**
 * Wrapper class around the new TalonSRX class. This class adds SpeedController
 * and LiveWindowSendable interfaces.
 */
public class CANTalon implements SpeedController, LiveWindowSendable {

	public enum FeedbackDevice {
		QuadEncoder(0, com.ctre.phoenix.MotorControl.FeedbackDevice.QuadEncoder), AnalogPot(2,
				com.ctre.phoenix.MotorControl.FeedbackDevice.Analog), AnalogEncoder(3, null), EncRising(4,
						com.ctre.phoenix.MotorControl.FeedbackDevice.Tachometer), EncFalling(5,
								null), CtreMagEncoder_Relative(6, null), CtreMagEncoder_Absolute(7, null), PulseWidth(8,
										com.ctre.phoenix.MotorControl.FeedbackDevice.PulseWidthEncodedPosition);
		public final int value;
		public final com.ctre.phoenix.MotorControl.FeedbackDevice feedbackDevice;

		FeedbackDevice(int value, com.ctre.phoenix.MotorControl.FeedbackDevice fd) {
			this.value = value;
			this.feedbackDevice = fd;
		}
	}

	public enum TalonControlMode {
		PercentVbus(0, ControlMode.PercentOutput), Position(1, ControlMode.Position), Speed(2,
				ControlMode.Velocity), Current(3, ControlMode.Current), Voltage(4, null), Follower(5,
						ControlMode.Follower), MotionProfile(6, ControlMode.MotionProfile), MotionMagic(7,
								ControlMode.MotionMagic), Disabled(15, ControlMode.MotionMagicArc);
		public final int value;
		public final ControlMode controlMode;

		TalonControlMode(int value, ControlMode cm) {
			this.value = value;
			this.controlMode = cm;
		}
	}

	private final int timeoutMs = 100;
	private final TalonSRX talonSRX;
	private final SpeedController speedController;
	private final int deviceNumber;
	private NetworkTable networkTable;
	private ControlMode controlMode;
	private com.ctre.phoenix.MotorControl.FeedbackDevice feedbackDevice;
	private NetworkTableEntry m_valueEntry;
	private int m_valueListener;

	public CANTalon(int deviceNumber) {
		this.talonSRX = new TalonSRX(deviceNumber);
		this.speedController = this.talonSRX.getWPILIB_SpeedController();
		this.deviceNumber = deviceNumber;
		this.controlMode = ControlMode.PercentOutput;
	}

	public void changeControlMode(TalonControlMode talonControlMode) {
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
		speedController.disable();
	}

	public void enable() {
		// ????
	}

	public void enableBrakeMode(boolean brakeEnabled) {
		talonSRX.setNeutralMode(brakeEnabled ? NeutralMode.Brake : NeutralMode.Coast);
	}

	@Override
	public double get() {
		return speedController.get();
	}

	public int getEncPosition() {
		return talonSRX.getSelectedSensorPosition();
	}

	public int getEncVelocity() {
		return talonSRX.getSelectedSensorVelocity();
	}

	@Override
	public boolean getInverted() {
		return speedController.getInverted();
	}
	
	public double getOutputCurrent()  {
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
	public void pidWrite(double output) {
		speedController.pidWrite(output);
	}

	public void reverseSensor(boolean reverse) {
		// ????
	}

	@Override
	public void set(double speed) {
		talonSRX.set(controlMode, speed);
	}

	public void setFeedbackDevice(FeedbackDevice fd) {
		feedbackDevice = fd.feedbackDevice;
	}

	@Override
	public void setInverted(boolean isInverted) {
		speedController.setInverted(isInverted);
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
		set(0); // Stop for safety
		final Consumer<EntryNotification> listener = new Consumer<EntryNotification>() {
			@Override
			public void accept(EntryNotification event) {
				set(event.value.getDouble());
			}
		};
		m_valueListener = m_valueEntry.addListener(listener,
				EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
	}

	@Override
	public void stopLiveWindowMode() {
		set(0); // Stop for safety
		m_valueEntry.removeListener(m_valueListener);
		m_valueListener = 0;
	}

	@Override
	public void stopMotor() {
		speedController.stopMotor();
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

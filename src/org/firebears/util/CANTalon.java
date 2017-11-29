package org.firebears.util;

import java.util.function.Consumer;

import com.ctre.phoenix.MotorControl.CAN.TalonSRX;
import com.ctre.phoenix.MotorControl.CAN.TalonSRXJNI;
import com.ctre.phoenix.MotorControl.SmartMotorController.TalonControlMode;

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
		QuadEncoder(0), AnalogPot(2), AnalogEncoder(3), EncRising(4), EncFalling(5), CtreMagEncoder_Relative(
				6), CtreMagEncoder_Absolute(7), PulseWidth(8);
		public int value;

		FeedbackDevice(int value) {
			this.value = value;
		}
	}

	public enum TalonControlMode {
		PercentVbus(0), Position(1), Speed(2), Current(3), Voltage(4), Follower(5), MotionProfile(6), MotionMagic(
				7), Disabled(15);
		public final int value;

		TalonControlMode(int value) {
			this.value = value;
		}
	}

	private final TalonSRX talonSRX;
	private final int deviceNumber;

	private NetworkTable networkTable;

	private NetworkTableEntry m_valueEntry;

	private int m_valueListener;

	public CANTalon(int deviceNumber) {
		talonSRX = new TalonSRX(deviceNumber);
		talonSRX.setControlMode(TalonControlMode.PercentVbus.value);
		this.deviceNumber = deviceNumber;
		m_handle = TalonSRXJNI.new_TalonSRX(deviceNumber);
	}

	public void changeControlMode(TalonControlMode controlMode) {
		talonSRX.changeControlMode(
				com.ctre.phoenix.MotorControl.SmartMotorController.TalonControlMode.valueOf(controlMode.value));
		m_controlMode = controlMode;
		if (controlMode == TalonControlMode.Disabled) {
			m_controlEnabled = false;
		}
	}

	public void clearStickyFaults() {
		talonSRX.clearStickyFaults();
	}

	public void configEncoderCodesPerRev(int codesPerRev) {
		talonSRX.configEncoderCodesPerRev(codesPerRev);
	}

	public void configNominalOutputVoltage(double forwardVoltage, double reverseVoltage) {
		talonSRX.configNominalOutputVoltage(forwardVoltage, reverseVoltage);
	}

	public void configPeakOutputVoltage(double forwardVoltage, double reverseVoltage) {
		talonSRX.configPeakOutputVoltage(forwardVoltage, reverseVoltage);
	}

	@Override
	public void disable() {
		talonSRX.disable();
	}

	public void enable() {
		talonSRX.enable();
	}

	public void enableBrakeMode(boolean brake) {
		talonSRX.enableBrakeMode(brake);
	}

	@Override
	public double get() {
		return talonSRX.get();
	}

	public int getEncPosition() {
		  return talonSRX.getEncPosition();
	  }

	public int getEncVelocity() {
		  return talonSRX.getEncVelocity();
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

	public double getSpeed() {
		return talonSRX.getSpeed();
	}

	public double getTemperature() {
		return talonSRX.getTemperature();
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
		this.set(output);
	}

	public void reverseSensor(boolean flip) {
		talonSRX.reverseSensor(flip);
	}

	public void enableControl() {
		talonSRX.enableControl();
	}
	
	long m_handle = 0;
	double m_setPoint;
	private TalonControlMode m_controlMode;
	boolean m_controlEnabled = true;
	boolean m_isInverted = false;
	boolean m_stopped = false;
	
	@Override
	public void set(double outputValue) {
		//talonSRX.set(outputValue);
		long m_handle = TalonSRXJNI.new_TalonSRX(deviceNumber);
	    if (m_stopped) {
	      enableControl();
	      m_stopped = false;
	    }
	    if (m_controlEnabled) {
	      m_setPoint = outputValue; /* cache set point for getSetpoint() */
	      switch (m_controlMode) {
	        case PercentVbus:
	          TalonSRXJNI.Set(m_handle, m_isInverted ? -outputValue : outputValue);
	          break;
	        case Follower:
	          TalonSRXJNI.SetDemand(m_handle, (int) outputValue);
	          break;
	        case Voltage:
	          // Voltage is an 8.8 fixed point number.
	          int volts = (int) ((m_isInverted ? -outputValue : outputValue) * 256);
	          TalonSRXJNI.SetDemand(m_handle, volts);
	          break;
	        case Speed:
	          TalonSRXJNI.SetDemand(m_handle, (int)(m_isInverted ? -outputValue : outputValue));
	          break;
	        case Position:
	          TalonSRXJNI.SetDemand(m_handle, (int)outputValue);
	          break;
	        case Current:
	          double milliamperes = (m_isInverted ? -outputValue : outputValue) * 1000.0; /* mA*/
	          TalonSRXJNI.SetDemand(m_handle, (int) milliamperes);
	          break;
	        case MotionProfile:
	          TalonSRXJNI.SetDemand(m_handle, (int) outputValue);
	          break;
	        case MotionMagic:
	          TalonSRXJNI.SetDemand(m_handle, (int)outputValue);
	          break;
	        default:
	          break;
	      }
	      TalonSRXJNI.SetModeSelect(m_handle, m_controlMode.value);
	    }
	}

	public void setFeedbackDevice(FeedbackDevice device) {
		talonSRX.setFeedbackDevice(
				com.ctre.phoenix.MotorControl.SmartMotorController.FeedbackDevice.valueOf(device.value));
	}

	@Override
	public void setInverted(boolean isInverted) {
		talonSRX.setInverted(isInverted);
		m_isInverted = isInverted;
	}

	public void setPID(double p, double i, double d, double f, int izone, double closeLoopRampRate, int profile) {
		talonSRX.setPID(p, i, d, f, izone, closeLoopRampRate, profile);
	}

	public void setSpeed(double speed) {
		this.set(speed);
	}

	@Override
	public void startLiveWindowMode() {
		setSpeed(0); // Stop for safety
		final Consumer<EntryNotification> listener = new Consumer<EntryNotification>()  {
			public void accept(EntryNotification event) {
				setSpeed(event.value.getDouble());
			}
		};
		m_valueListener = m_valueEntry.addListener(listener,
				EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
	}

	@Override
	public void stopLiveWindowMode() {
		setSpeed(0); // Stop for safety
		m_valueEntry.removeListener(m_valueListener);
		m_valueListener = 0;
	}
	
	  @Override
	public void stopMotor() {
		talonSRX.stopMotor();
	    m_stopped = true;
	}
	  
	  @Override
	public void updateTable() {
		if (m_valueEntry != null) {
			m_valueEntry.setDouble(getSpeed());
		}
	}
	  
	  @Override
	  public String toString() {
		  return "CANTalon(" + deviceNumber + ")";
	  }
}


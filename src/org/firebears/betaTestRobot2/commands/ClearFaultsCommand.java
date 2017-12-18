package org.firebears.betaTestRobot2.commands;

import java.util.Arrays;
import java.util.List;

import org.firebears.util.CANTalon;

import com.ctre.phoenix.MotorControl.CAN.TalonSRX;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Clear sticky faults from a list of components.
 */
public class ClearFaultsCommand extends Command {

	private final PowerDistributionPanel pdp;
	private final List<?> componentList;

	public ClearFaultsCommand(PowerDistributionPanel pdp, Object... comps) {
		this.pdp = pdp;
		this.componentList = Arrays.asList(comps);
	}

	protected void initialize() {
	}

	protected void execute() {
		pdp.clearStickyFaults();
		(new Compressor()).clearAllPCMStickyFaults();
		for (Object component : componentList) {
			if (component instanceof CANTalon) {
				((CANTalon) component).clearStickyFaults();
			} else if (component instanceof TalonSRX) {
				((TalonSRX) component).clearStickyFaults(10);
			}
		}
	}

	protected boolean isFinished() {
		return true;
	}

}

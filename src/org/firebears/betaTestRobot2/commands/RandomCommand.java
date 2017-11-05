package org.firebears.betaTestRobot2.commands;

import java.util.Random;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Randomly change values on the Labview dashboard.
 */
public class RandomCommand extends Command {

	Random random = new Random();
	NetworkTable networkTable;
	
	public RandomCommand() {
		random = new Random();
		networkTable = NetworkTableInstance.getDefault().getTable("SmartDashboard/DB");
	}
	
	@Override
	protected void initialize() {
	}

	@Override
	protected void execute() {
	}

	@Override
	protected boolean isFinished() {
		return true;
	}

	@Override
	protected void end() {
		resetBasicDashboardTab();
	}

	private void resetBasicDashboardTab() {
		for (int i=0; i<5; i++) {
			String stringKey = "String " + i;
			networkTable.getEntry(stringKey).setString(randomString());
		}
		for (int i=0; i<4; i++) {
			String sliderKey = "Slider " + i;
			String stringKey = "String " + (i+5);
			Number sliderValue = networkTable.getEntry(sliderKey).getNumber(0);
			networkTable.getEntry(stringKey).setString(sliderValue.toString());
		}
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<4; i++) {
			String buttonKey = "Button " + i;
			Boolean buttonValue = networkTable.getEntry(buttonKey).getBoolean(false);
			sb.append(buttonValue ? "T " : "F ");
		}
		networkTable.getEntry("String 9").setString(sb.toString());
	}

	@Override
	protected void interrupted() {
		end();
	}
	
	private String randomString() {
		return  list0[random.nextInt(list0.length)] + " " + list1[random.nextInt(list1.length)] + " " + list2[random.nextInt(list2.length)];
	}
	
	String[] list0 = {"wet", "warm", "bouncy", "sparky", "limber", "quick", "arborial", "swampy", "powerful", "fiery"};
	String[] list1 = {"red", "happy", "yellow", "crazy", "gracious", "light", "mellow", "fast", "loud", "bright"};
	String[] list2 = {"dog", "robot", "wire", "computer", "banner", "jacket", "hammer", "car", "sandwich", "tree"};
}

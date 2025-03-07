package io.openems.edge.batteryinverter.sinexcel.statemachine;

import io.openems.edge.batteryinverter.api.OffGridBatteryInverter.TargetGridMode;
import io.openems.edge.batteryinverter.sinexcel.BatteryInverterSinexcelImpl;
import io.openems.edge.batteryinverter.sinexcel.Config;
import io.openems.edge.common.statemachine.AbstractContext;

public class Context extends AbstractContext<BatteryInverterSinexcelImpl> {

	protected final Config config;
	protected final TargetGridMode targetGridMode;
	protected final int setActivePower;
	protected final int setReactivePower;
	protected final int setEnergyDispatchingMode;
	protected final int setZero;
	protected final int setActivePowerL1;
	protected final int setActivePowerL2;
	protected final int setActivePowerL3;

	public Context(BatteryInverterSinexcelImpl parent, Config config, TargetGridMode targetGridMode, int setActivePower,
			int setReactivePower) {
		super(parent);
		this.config = config;
		this.targetGridMode = targetGridMode;
		this.setActivePower = setActivePower;
		this.setReactivePower = setReactivePower;
		this.setEnergyDispatchingMode = 4; // 0 = 3phase mode, 4 = individual phase mode
		this.setZero = 0;
		this.setActivePowerL1 = setActivePower/3;
		this.setActivePowerL2 = setActivePower/3;
		this.setActivePowerL3 = setActivePower/3;
//		if (setActivePower > 0) {
//			// if buying/charging must set L1 to a positive number
//			this.setActivePowerL1 = 500;
//		}
//		else {
//			this.setActivePowerL1 = 0;
//		}
//		this.setActivePowerL2 = setActivePower;
//		this.setActivePowerL3 = 0;
	}

}
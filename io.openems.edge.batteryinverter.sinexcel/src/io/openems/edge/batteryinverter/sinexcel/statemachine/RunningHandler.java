package io.openems.edge.batteryinverter.sinexcel.statemachine;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.batteryinverter.sinexcel.BatteryInverterSinexcel;
import io.openems.edge.batteryinverter.sinexcel.statemachine.StateMachine.State;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.statemachine.StateHandler;

public class RunningHandler extends StateHandler<State, Context> {

	@Override
	public State runAndGetNextState(Context context) throws OpenemsNamedException {
		final var inverter = context.getParent();

		if (inverter.hasFaults() || inverter.getInverterState().get() == Boolean.FALSE) {
			return State.UNDEFINED;
		}

		// Mark as started
		inverter._setStartStop(StartStop.START);
		
		// Apply Active and Reactive Power Set-Points
		this.applyPower(context);

		return State.RUNNING;
	}

	/**
	 * Applies the Active and Reactive Power Set-Points.
	 *
	 * @param context the {@link Context}
	 * @throws OpenemsNamedException on error
	 */
	private void applyPower(Context context) throws OpenemsNamedException {
		final var inverter = context.getParent();

		IntegerWriteChannel setActivePower = inverter.channel(BatteryInverterSinexcel.ChannelId.SET_ACTIVE_POWER);
		setActivePower.setNextWriteValue(context.setActivePower);

		IntegerWriteChannel setReactivePower = inverter.channel(BatteryInverterSinexcel.ChannelId.SET_REACTIVE_POWER);
		setReactivePower.setNextWriteValue(context.setReactivePower);

		// timl
		IntegerWriteChannel setActivePowerL1 = inverter.channel(BatteryInverterSinexcel.ChannelId.SET_ACTIVE_POWER_L1);
		setActivePowerL1.setNextWriteValue(context.setActivePowerL1);
		
		IntegerWriteChannel setActivePowerL2 = inverter.channel(BatteryInverterSinexcel.ChannelId.SET_ACTIVE_POWER_L2);
		setActivePowerL2.setNextWriteValue(context.setActivePowerL2);

		IntegerWriteChannel setActivePowerL3 = inverter.channel(BatteryInverterSinexcel.ChannelId.SET_ACTIVE_POWER_L3);
		setActivePowerL3.setNextWriteValue(context.setActivePowerL3);

	
	}
	
}

package io.openems.edge.controller.ess.cycle.statemachine;

import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.controller.ess.cycle.statemachine.StateMachine.State;

public class UndefinedHandler extends StateHandler<State, Context> {

	@Override
	public State runAndGetNextState(Context context) {
		final var ess = context.ess;
		final var config = context.config;

		if (!context.isEssSocDefined()) {
			return State.UNDEFINED;
		}

		if (!context.isStartTimeInitialized()) {
			return State.UNDEFINED;
		}

		return switch (config.cycleOrder()) {
		case START_WITH_CHARGE -> State.START_CHARGE;
		case START_WITH_DISCHARGE -> State.START_DISCHARGE;
		case AUTO -> {
			int soc = ess.getSoc().get();
			if (soc < 50) {
				yield State.START_DISCHARGE;
			}
			yield State.START_CHARGE;
		}
		};
	}

	@Override
	protected void onExit(Context context) {
		context.updateLastStateChangeTime();
	}
}

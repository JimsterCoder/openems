package io.openems.edge.controller.ess.limitusablecapacity;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.io.openems.edge.controller.ess.limitusablecapacity", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class LimitUsableCapacityControllerImpl extends AbstractOpenemsComponent
		implements LimitUsableCapacityController, Controller, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(LimitUsableCapacityControllerImpl.class);

	@Reference
	protected ComponentManager componentManager;

	protected Config config;
	private State state = State.UNDEFINED;

	public LimitUsableCapacityControllerImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				LimitUsableCapacityController.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.config = config;

		if (this.config.stopDischargeSoc() < this.config.forceChargeSoc()) {
			throw new OpenemsException("Stop Charge Soc " + config.stopDischargeSoc()
					+ " should be greater than the force charge Soc " + config.forceChargeSoc());
		}

		// TODO add all the other combinations for error

	}

	@Override
	public void run() throws OpenemsNamedException {
		ManagedSymmetricEss ess = this.componentManager.getComponent(this.config.ess_id());

		
		// Set to normal state and return if SoC is not available
		Value<Integer> socOpt = ess.getSoc();
		if (!socOpt.isDefined()) {
			this.state = State.NO_LIMIT;
			return;
		}
		int soc = socOpt.get();

		// initialize allowedCharge
		Integer allowedCharge = null;
		// initialize allowedDischarge
		Integer allowedDischarge = null;

		boolean stateChanged;
		do {
			stateChanged = false;
			switch (this.state) {
			case UNDEFINED:
			case NO_LIMIT:
				/*
				 * no-limit State
				 */
				// no constraints in normal operation mode
				allowedDischarge = null;
				allowedCharge = null;

				if (soc <= this.config.stopDischargeSoc()) {
					stateChanged = this.changeState(State.STOP_DISCHARGE);
					break;
				}
				if (soc >= this.config.stopChargeSoc()) {
					stateChanged = this.changeState(State.STOP_CHARGE);
					break;
				}
				break;
			case STOP_DISCHARGE:
				/*
				 * Stop Discharge State
				 */
				allowedDischarge = 0;

				if (soc <= this.config.forceChargeSoc()) {
					stateChanged = this.changeState(State.FORCE_CHARGE);
					break;
				}
				if (soc > this.config.allowDischargeSoc()) {
					stateChanged = this.changeState(State.NO_LIMIT);
					break;
				}
				break;
			case FORCE_CHARGE:
				/*
				 * Force chargin state
				 */
				allowedDischarge = (ess.getMaxApparentPower().getOrError() * 20) / 100;

				if (soc >= this.config.stopDischargeSoc()) {
					stateChanged = this.changeState(State.STOP_DISCHARGE);
					break;
				}
				break;
			case STOP_CHARGE:
				/*
				 * Stop charge state
				 */
				allowedCharge = 0;

				if (soc <= this.config.allowChargeSoc()) {
					stateChanged = this.changeState(State.NO_LIMIT);
					break;
				}
				break;

			}

		} while (stateChanged);

		if (allowedCharge != null) {
			ess._setAllowedChargePower(allowedCharge);
		}
		if (allowedDischarge != null) {
			ess._setAllowedDischargePower(allowedDischarge);
		}

		this.channel(LimitUsableCapacityController.ChannelId.STATE_MACHINE).setNextValue(this.state);
		this.channel(LimitUsableCapacityController.ChannelId.ALLOWED_CHARGE_POWER).setNextValue(allowedCharge);
		this.channel(LimitUsableCapacityController.ChannelId.ALLOWED_DISCHARGE_POWER).setNextValue(allowedDischarge);
		log.info("Currently in " + this.state + //
				" and with Allowed charge power : " + allowedCharge + " W" + //
				" and with Allowed discharge power : " + allowedDischarge + " W"); //

	}

	/**
	 * A flag to maintain change in the state.
	 * 
	 * @param nextState the target state
	 * @return Flag that the state is changed or not
	 */
	private boolean changeState(State nextState) {
		if (this.state != nextState) {
			this.state = nextState;
			return true;
		} else {
			return false;
		}
	}
}

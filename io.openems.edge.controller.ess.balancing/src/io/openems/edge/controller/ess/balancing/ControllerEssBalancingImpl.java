package io.openems.edge.controller.ess.balancing;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.meter.api.ElectricityMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.Symmetric.Balancing", // This name has to be kept for compatibility reasons
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class ControllerEssBalancingImpl extends AbstractOpenemsComponent implements Controller, OpenemsComponent {
	
	private final Logger log = LoggerFactory.getLogger(ControllerEssBalancingImpl.class);

	@Reference
	private ConfigurationAdmin cm;

	@Reference
	private ManagedSymmetricEss ess;

	@Reference
	private ElectricityMeter meter;

	private Config config;
	
	private double costTotal;
	private int[] meterW = new int[3];
	private int inverter_power_IN; 
	private int inverter_power_OUT; 
	private int control;
	private int deltapower;
	private int newfirmwarefudge = -300;
	private boolean newfirmware = true;

	public ControllerEssBalancingImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ControllerEssBalancing.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "ess", config.ess_id())) {
			return;
		}
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "meter", config.meter_id())) {
			return;
		}
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		if(config.mode() == Mode.MANUAL_OFF) {
			return;
		}
		/*
		 * Check that we are On-Grid (and warn on undefined Grid-Mode)
		 */
		var gridMode = this.ess.getGridMode();
		if (gridMode.isUndefined()) {
			this.logWarn(this.log, "Grid-Mode is [UNDEFINED]");
		}
		switch (gridMode) {
		case ON_GRID:
		case UNDEFINED:
			break;
		case OFF_GRID:
			return;
		}
		double buy = 0.0; // $/kWh
		double sell = 0.0;	

		// get meter readings
		meterW[0] = this.meter.getActivePowerL1().getOrError();
		meterW[1] = this.meter.getActivePowerL2().getOrError();
		meterW[2] = this.meter.getActivePowerL3().getOrError();
		
		inverter_power_IN = this.ess.getActivePower().getOrError();
		inverter_power_OUT = inverter_power_IN;
		
		// ************************************************************************************
		if (this.config.targetGridSetpoint() == 27 ||
				this.config.targetGridSetpoint() == 32 ||
				this.config.targetGridSetpoint() == 54 ||
				this.config.targetGridSetpoint() == 270 ||
				this.config.targetGridSetpoint() == 320 ||
				this.config.targetGridSetpoint() == 540) {
			
			if (this.config.targetGridSetpoint() == 27) {
				buy = 0.270365; // $/kWh
				sell = 0.1438;
				newfirmware = false;
			}
			else if (this.config.targetGridSetpoint() == 32) {
				buy = 0.32453; // $/kWh
				sell = 0.1438;				
				newfirmware = false;
			}
			else if (this.config.targetGridSetpoint() == 54) {
				buy = 0.540845; // $/kWh
				sell = 0.1438;				
				newfirmware = false;
			}
			else if (this.config.targetGridSetpoint() == 270) {
				buy = 0.270365; // $/kWh
				sell = 0.1438;
				newfirmware = true;
			}
			else if (this.config.targetGridSetpoint() == 320) {
				buy = 0.32453; // $/kWh
				sell = 0.1438;				
				newfirmware = true;
			}
			else if (this.config.targetGridSetpoint() == 540) {
				buy = 0.540845; // $/kWh
				sell = 0.1438;				
				newfirmware = true;
			}
			double[] cost = new double[3];

			// calculate cost on each phase
			double consumedPower = 0;
			costTotal = 0; // global variable used in debug function
			for (int i = 0; i < 3; i++) {
				if (meterW[i] > 0) {
		          cost[i] = meterW[i] * buy / 1000.0;
				} else {	
					cost[i] = meterW[i] * sell / 1000.0;
				}
				// total cost
				costTotal += cost[i];
        // calculate power actually consumed by the site
        // the meter reading plus power supplied by the inverter
				consumedPower += meterW[i] + inverter_power_OUT / 3.0;
			}
		
		  // only act if cost is outside a set value $/kWh
		  double costdesired = -0.010;
		  double costrange = 0.005;
		  double maxcost = costdesired + costrange;
		  double mincost = costdesired - costrange;
		  
		  control = 0;
		  deltapower = 300;
		  if ((costTotal - costdesired) > 0.20) {
			  // make a larger change
			  deltapower = 500;
			  control = 1;
		  }
		  else if ((costTotal - costdesired) < -0.10) {
			  // make a larger change
			  deltapower = 300;
			  control = 2;
		  }
		  else if ((costTotal - costdesired) < -0.20) {
			  // make a larger change
			  deltapower = 1000;
			  control = 3;
		  }
		  
		
		  if (consumedPower < 0) {
			  // if we are not consuming power, we don't need the battery
			  // (power is coming from solar)
			  inverter_power_OUT = 0;	
			  control = 4;
		  }
		  else if (costTotal > maxcost) {
		      // increase inverter power
		      inverter_power_OUT += deltapower;
			  control = 5;
		
		  }
		  else if (costTotal < mincost) {
			  // decrease inverter power
			  // this is an ODD problem here as the inverter can only be 0 or 400 and nothing in between
			  // (if set to 100, it goes to 400), so if it's on 400 it will never go to 0
			  // when it is told to set to 300, it stays at 400.
//			  if (((costTotal < mincost) && (inverter_power == 400)) || ((costTotal < mincost) && ((inverter_power * 0.4) > consumedPower))) {
			  if ((costTotal < mincost) && (inverter_power_OUT == 400)) {
				  inverter_power_OUT = 0;
				  control = 6;
			  }
			  else if (inverter_power_OUT > 100) {
				  // decrease inverter power
				  // triple delta when decreasing
		          inverter_power_OUT -= deltapower;
		          control = 7;
		          if (newfirmware) inverter_power_OUT += newfirmwarefudge;
		          if ((inverter_power_OUT < 400) && (costTotal > -0.05)) {
		        	  inverter_power_OUT = 400;
		        	  control = 8;
		          }
		      }
		  }
		  
		  // double check we aren't buying
	      if (inverter_power_OUT < 0) {
	        inverter_power_OUT = 0;
	        control = 9;
	      }
	
	      // control the maximum power
	      if (inverter_power_OUT > 6500) {
	    	  inverter_power_OUT = 6500;
	    	  control = 10;
	      }
	      		      
		  this.ess.setActivePowerEquals(inverter_power_OUT);
		  this.ess.setReactivePowerEquals(0);
		}
		// ************************************************************************************
		
		/*
		 * Calculates required charge/discharge power
		 */
		else {
			var calculatedPower = calculateRequiredPower(//
					this.ess.getActivePower().getOrError(), //
					this.meter.getActivePower().getOrError(), //
					this.config.targetGridSetpoint(), //
					50);
			/*
			 * set result
			 */
			// this.ess.setActivePowerEqualsWithPid(calculatedPower);
			this.ess.setActivePowerEquals(calculatedPower);
			this.ess.setReactivePowerEquals(0);
			
		}
		
	}

	/**
	 * Calculates required charge/discharge power.
	 *
	 * @param essPower           the charge/discharge power of the
	 *                           {@link ManagedSymmetricEss}
	 * @param gridPower          the buy-from-grid/sell-to grid power
	 * @param targetGridSetpoint the configured targetGridSetpoint
	 * @return the required power
	 */
	protected static int calculateRequiredPower(int essPower, int gridPower, int targetGridSetpoint, int fudgeSellMore) {
		int newsetting = gridPower + essPower +fudgeSellMore - targetGridSetpoint;
		return newsetting;
	}
	@Override
	public String debugLog() {
		if (this.isEnabled()) {
//			return String.format("Cost:%.3f $", costTotal);
			return String.format("Cost:$%.3f L1 %d  L2 %d  L3 %d  in %d  out %d  x %d  d %d", costTotal, meterW[0], meterW[1], meterW[2], inverter_power_IN, inverter_power_OUT, control, deltapower );
		}
		else {
			return null;
		}
	}
}

package io.openems.edge.controller.ess.timeofusetariff;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Controller Ess Time-Of-Use Tariff", //
		description = "Optimize behaviour of an ESS in combination with a Time-Of-Use (ToU) Tariff.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlEssTimeOfUseTariff0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Ess-ID", description = "ID of Ess device.")
	String ess_id() default "ess0";

	@AttributeDefinition(name = "Mode", description = "Set the type of mode.")
	Mode mode() default Mode.AUTOMATIC;

	@AttributeDefinition(name = "Max charge power [W]", description = "Maximum Chargeable power from the grid")
	int maxPower() default 5000;

	@AttributeDefinition(name = "Control-Mode", description = "Set the control-mode.")
	ControlMode controlMode() default ControlMode.DELAY_DISCHARGE;

	@AttributeDefinition(name = "Ess target filter", description = "This is auto-generated by 'Ess-ID'.")
	String ess_target() default "(enabled=true)";

	String webconsole_configurationFactory_nameHint() default "Controller Ess Time-Of-Use Tariff [{id}]";

}
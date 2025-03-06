package io.openems.edge.batteryinverter.sinexcel.enums;

import io.openems.common.types.OptionsEnum;

public enum EnergyDispatchingMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	THREE_PHASE_CONTROL(0, "Three phase control"), //
	INDIVIDUAL_PHASE_CONTROL(4, "Individual phase control");//

	private final int value;
	private final String name;

	private EnergyDispatchingMode(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}
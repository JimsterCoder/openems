package io.openems.edge.batteryinverter.sinexcel.enums;

import io.openems.common.types.OptionsEnum;

public enum ActivePowerControlMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	CONTANT_ACTIVE_POWER(0, "Constant Active Power"), //
	VOLT_WATT_ENABLED(1, "Volt watt enabled"), //
	CONSTANT_PF(0, "Constant power factor"), //
	WATT_PF_ENABLED(0, "Watt power factor enabled");//

	private final int value;
	private final String name;

	private ActivePowerControlMode(int value, String name) {
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

//public enum EnergyDispatchMode implements OptionsEnum {
//	UNDEFINED(-1, "Undefined"), //
//	THREE_PHASE_CONTROL(0, "Three phase control"), //
//	INDIVIDUAL_PHASE_CONTROL(4, "Individual phase control");//
//
//	private final int value;
//	private final String name;
//
//	private EnergyDispatchMode(int value, String name) {
//		this.value = value;
//		this.name = name;
//	}
//
//	@Override
//	public int getValue() {
//		return this.value;
//	}
//
//	@Override
//	public String getName() {
//		return this.name;
//	}
//
//	@Override
//	public OptionsEnum getUndefined() {
//		return UNDEFINED;
//	}
//}
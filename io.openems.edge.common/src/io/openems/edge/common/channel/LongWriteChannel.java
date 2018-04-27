package io.openems.edge.common.channel;

import java.util.Optional;
import java.util.function.Consumer;

import io.openems.edge.common.channel.doc.ChannelId;
import io.openems.edge.common.component.OpenemsComponent;

public class LongWriteChannel extends LongReadChannel implements WriteChannel<Long> {

	public LongWriteChannel(OpenemsComponent component, ChannelId channelId) {
		super(component, channelId);
	}

	private Optional<Long> nextWriteValueOpt = Optional.empty();

	/**
	 * Internal method. Do not call directly.
	 * 
	 * @param value
	 */
	@Deprecated
	@Override
	public void _setNextWriteValue(Long value) {
		this.nextWriteValueOpt = Optional.ofNullable(value);
	}

	/**
	 * Internal method. Do not call directly.
	 * 
	 * @param value
	 */
	@Deprecated
	@Override
	public Optional<Long> _getNextWriteValue() {
		return this.nextWriteValueOpt;
	}

	/*
	 * onSetNextWrite
	 */
	private Consumer<Long> onSetNextWriteCallback = null;

	@Override
	public Consumer<Long> getOnSetNextWrite() {
		return this.onSetNextWriteCallback;
	}

	@Override
	public void _onSetNextWrite(Consumer<Long> callback) {
		this.onSetNextWriteCallback = callback;
	}
}

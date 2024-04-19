package org.magcruise.gaming.ui.api.message;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@SuppressWarnings("serial")
public class NotifyGameRecord extends SimpleGameProgress {

	private String context;

	public NotifyGameRecord(int roundnum, String context) {
		super(roundnum);
		this.context = context;
	}

	public String getContext() {
		return context;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("roundnum", getRoundnum()).build();
	}

}

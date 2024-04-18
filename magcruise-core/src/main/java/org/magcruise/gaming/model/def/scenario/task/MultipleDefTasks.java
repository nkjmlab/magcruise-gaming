package org.magcruise.gaming.model.def.scenario.task;

import java.util.List;
import org.magcruise.gaming.model.def.common.MultipleElements;

@SuppressWarnings("serial")
public class MultipleDefTasks extends MultipleElements<DefTask> implements DefTask {

	public MultipleDefTasks(List<DefTask> result) {
		super(result);
	}

}

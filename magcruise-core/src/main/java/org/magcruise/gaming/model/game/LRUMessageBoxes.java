package org.magcruise.gaming.model.game;

import java.util.Collections;
import org.apache.commons.collections4.map.LRUMap;

public class LRUMessageBoxes<K, T> extends MessageBoxes<K, T> {

	public LRUMessageBoxes(int maxSizeOfBoxes) {
		messageBoxes = Collections.synchronizedMap(new LRUMap<>(maxSizeOfBoxes));
	}

}

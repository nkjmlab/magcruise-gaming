/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.magcruise.gaming.ui.api.message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.magcruise.gaming.model.game.message.SimpleMessage;

@SuppressWarnings("serial")
public class RequestToUI extends SimpleMessage implements Comparable<RequestToUI> {
	private volatile String type;
	private volatile long id;

	public RequestToUI() {
		this(generateId());
	}

	public RequestToUI(long id) {
		this.type = getClass().getSimpleName();
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private static final Queue<Long> ids = new ConcurrentLinkedQueue<>();

	public synchronized static long generateId() {
		while (true) {
			long id = System.currentTimeMillis();
			if (!ids.contains(id)) {
				ids.offer(id);
				if (ids.size() > 1000) {
					ids.poll();
				}
				return id;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public int compareTo(RequestToUI o) {
		return (int) (getId() - o.getId());
	}

}

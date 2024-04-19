package org.magcruise.gaming.model.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class History implements SConstructive {
	protected static Logger log = LogManager.getLogger();

	private Map<Integer, RoundHistory> history = new ConcurrentHashMap<>();

	public History() {
	}

	public History(RoundHistory round) {
		this();
		history.put(0, round);
	}

	public History(RoundHistory... rounds) {
		this();
		for (int i = 0; i < rounds.length; i++) {
			history.put(i, rounds[i]);
		}

	}

	@Override
	public SConstructor<? extends History> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "    " : "")
						+ (SConstructor.toConstructor(style, getClass(),
								history.values().toArray())));
	}

	public RoundHistory getRoundHistory(int roundnum) {
		return history.get(roundnum);
	}

	public void putRoundHistory(int roundnum, RoundHistory round) {
		history.put(roundnum, round);
	}

	public Serializable getValue(Symbol key, int roundnum) {
		return history.get(roundnum).get(key);
	}

	public Serializable getValueBefore(Symbol key, int roundnum) {
		return history.get(history.size() - roundnum).get(key);
	}

	public Serializable getLastValue(Symbol key) {
		return history.get(history.size() - 1).get(key);
	}

	public String tabulate() {
		return tabulate(0, history.size(), history.get(0).keySet().toArray(new Symbol[0]));
	}

	public void removeRoundHistory(int roundnum) {
		history.remove(roundnum);
	}

	public String tabulate(int from, int to, Symbol... keys) {
		if (history.size() == 0) {
			return "";
		}

		Serializable[] targets = keys;
		List<Pair> pairs = new ArrayList<>();

		for (Serializable target : targets) {
			pairs.add(new Pair(target, target));
		}
		return tabulate(from, to, pairs.toArray(new Pair[0]));
	}

	public String tabulate(int from, int to, Pair... keys) {
		Pair[] targets = keys;
		if (history.size() == 0) {
			return "";
		}

		String str = "<div class='table-responsive-sm'>";
		str += "<table class='table table-bordered table-striped table-sm'>";
		str += "<tr><th>rd.</th>";
		for (Pair pair : targets) {
			str += "<th>" + pair.getCar() + "</th>";
		}
		str += "</tr>";

		for (int i = from; i < to; i++) {
			RoundHistory r = history.get(i);
			str += "<tr><td>" + i + "</td>";
			for (Pair pair : targets) {
				Symbol key = (Symbol) pair.getCdr();
				str += "<td>" + r.get(key) + "</td>";
			}
			str += "</tr>";
		}
		str += "</table></div>";
		return str;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 *
	 * @param roundnum
	 * @return
	 */
	private History subHistoryOfEndOfRound(int roundnum) {
		int toIndex = history.size() <= roundnum + 1 ? history.size() : roundnum + 1;

		History result = new History();
		history.values().forEach(r -> {
			if (r.getRoundnum() < toIndex) {
				result.putRoundHistory(r.getRoundnum(), r);
			}
		});

		return result;
	}

	public int size() {
		return history.size();
	}

	public void revertToEndOfRound(int finishedRoundnum) {
		this.history = subHistoryOfEndOfRound(finishedRoundnum).history;
	}

}

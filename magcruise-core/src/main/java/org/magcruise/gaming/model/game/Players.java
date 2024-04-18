package org.magcruise.gaming.model.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.lists.LList;
import gnu.lists.Pair;

@SuppressWarnings("serial")
public class Players<T extends Player> implements SConstructive, Iterable<T> {

	private Map<ActorName, T> players = new LinkedHashMap<>();

	/**
	 * ゲームシナリオ(Sceheme)からアクセスしやすいように用意している．ゲームシナリオ以外からはアクセスしないこと．
	 */

	public LList all;

	public Players(List<T> players) {
		for (T p : players) {
			this.players.put(p.name, p);
		}
		this.all = this.asLList();
	}

	public Players(@SuppressWarnings("unchecked") T... players) {
		this(Arrays.asList(players));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SConstructor<? extends Players> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), players.values().toArray());
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() : "")
				+ SConstructive.super.getExpression(style);
	}

	public LList asLList() {
		return Pair.makeList(new ArrayList<T>(players.values()));
	}

	public Player get(int index) {
		return new ArrayList<>(players.values()).get(index);
	}

	public Player get(ActorName name) {
		return players.get(name);
	}

	public <S extends Player> S getAs(Class<S> clazz, ActorName name) {
		Player p = get(name);
		if (clazz.isAssignableFrom(p.getClass())) {
			return clazz.cast(p);
		}
		throw new IllegalArgumentException(
				"Player who is instance of " + clazz.getName() + " is not found.");
	}

	public <S extends Player> S get(Class<S> clazz) {
		for (Player p : values()) {
			if (clazz.isAssignableFrom(p.getClass())) {
				return clazz.cast(p);
			}
		}
		throw new IllegalArgumentException(
				"Player who is instance of " + clazz.getName() + " is not found.");
	}

	@Override
	public String toString() {
		return this.players.toString();
	}

	public int size() {
		return this.players.keySet().size();
	}

	public Collection<T> values() {
		return players.values();
	}

	@Override
	public Iterator<T> iterator() {
		return players.values().iterator();
	}

	public <S extends Player> Players<S> getPlayers(Class<S> clazz) {
		List<S> result = new ArrayList<>();
		for (Player m : values()) {
			if (clazz.isAssignableFrom(m.getClass())) {
				result.add(clazz.cast(m));
			}
		}
		return new Players<>(result);
	}

}

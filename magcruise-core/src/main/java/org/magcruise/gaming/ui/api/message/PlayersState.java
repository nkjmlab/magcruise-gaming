package org.magcruise.gaming.ui.api.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class PlayersState extends RequestToUI {

	private int round = 0;

	private Map<String, Map<Integer, Map<Boolean, Integer>>> requestCounts = new HashMap<>();

	private List<RequestToAssignOperator> players = new ArrayList<>();

	public PlayersState() {
		super();
	}

	public void addAssignment(RequestToAssignOperator assinment) {
		this.players.add(assinment);
		String playerName = assinment.getPlayerName();
		requestCounts.putIfAbsent(playerName, new HashMap<>());

	}

	public void addRequestToInput(RequestToInput request) {

		String playerName = request.getPlayerId();
		requestCounts.putIfAbsent(playerName, new HashMap<>());

		Map<Integer, Map<Boolean, Integer>> roundsInfo = requestCounts.get(request.getPlayerId());
		int roundnum = request.getRoundnum();

		roundsInfo.putIfAbsent(roundnum, new HashMap<>());

		Map<Boolean, Integer> roundInfo = roundsInfo.get(roundnum);
		Boolean finished = request.getFinished();
		roundInfo.putIfAbsent(finished, 0);
		roundInfo.put(finished, roundInfo.get(finished) + 1);

		if (roundnum > round) {
			this.round = roundnum;
		}
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public Map<String, Map<Integer, Map<Boolean, Integer>>> getRequestCounts() {
		return requestCounts;
	}

	public void setRequestCounts(Map<String, Map<Integer, Map<Boolean, Integer>>> requestCounts) {
		this.requestCounts = requestCounts;
	}

	public List<RequestToAssignOperator> getPlayers() {
		return players;
	}

	public void setPlayers(List<RequestToAssignOperator> players) {
		this.players = players;
	}

}

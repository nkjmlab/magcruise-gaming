function joinInGame(url, sessionId, playerName) {
	new JsonRpcClient(new JsonRpcRequest(url, "joinInGame", [sessionId, playerName])).rpc();
}
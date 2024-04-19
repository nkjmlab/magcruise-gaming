package org.magcruise.gaming.examples.util;

/**
 * {@link PlayerMode#AGENTS}は，全員がエージェントのゲームを実行する． {@link PlayerMode#SINGLE_PLAYER} ,
 * {@link PlayerMode#TWO_PLAYERS}, {@link PlayerMode#THREE_PLAYERS}は順に，人間のプレーヤが1人，2人，3人のゲームを実行する．
 *
 */
public enum PlayerMode {
  AGENTS, THREE_PLAYERS, TWO_PLAYERS, SINGLE_PLAYER,
}

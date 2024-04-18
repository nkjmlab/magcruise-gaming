package org.magcruise.broker.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.MessageBox;
import org.magcruise.gaming.model.game.MessageBoxes;
import org.magcruise.gaming.ui.api.message.RequestToUI;

public class PlayerMessageBoxes {

  protected Map<ProcessId, MessageBoxes<String, RequestToUI>> playerMessageBoxesOfProcesses;

  public PlayerMessageBoxes(int maxSizeOfProcess) {
    playerMessageBoxesOfProcesses = Collections.synchronizedMap(new LRUMap<>(maxSizeOfProcess));
  }

  public synchronized <T extends RequestToUI> List<T> get(
      Class<T> clazz, ProcessId processId, String playerId) {
    return getMessageBox(processId, playerId).values(clazz);
  }

  public synchronized void offer(ProcessId processId, String playerName, RequestToUI message) {
    getMessageBox(processId, playerName).offer(message);
  }

  private synchronized MessageBox<RequestToUI> getMessageBox(ProcessId processId, String playerId) {
    playerMessageBoxesOfProcesses.putIfAbsent(processId, new MessageBoxes<>());
    MessageBoxes<String, RequestToUI> messageQueueOfProcess =
        playerMessageBoxesOfProcesses.get(processId);
    return messageQueueOfProcess.getMessageBox(playerId);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}

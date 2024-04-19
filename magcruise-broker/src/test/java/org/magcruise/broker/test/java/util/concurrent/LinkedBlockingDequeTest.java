package org.magcruise.broker.test.java.util.concurrent;

import static org.assertj.core.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.Test;

public class LinkedBlockingDequeTest {
  @Test
  public void test_order() throws Exception {
    LinkedBlockingDeque<Integer> deq = new LinkedBlockingDeque<Integer>();
    deq.offer(0);
    deq.offer(1);
    deq.offer(2);
    assertThat(0).isEqualTo((int) deq.poll());
    assertThat(1).isEqualTo((int) deq.poll());
    assertThat(2).isEqualTo((int) deq.poll());
  }

  @Test
  public void test_drain_order() throws Exception {
    LinkedBlockingDeque<Integer> deq = new LinkedBlockingDeque<>();
    deq.offer(0);
    deq.offer(1);
    deq.offer(2);
    List<Integer> list = new ArrayList<>();
    deq.drainTo(list);
    assertThat(0).isEqualTo((int) list.get(0));
    assertThat(1).isEqualTo((int) list.get(1));
    assertThat(2).isEqualTo((int) list.get(2));
  }

  @Test
  public void test_concurrent_add_drain() throws Exception {
    final int elementSize = 1000000;
    final LinkedBlockingDeque<Integer> deq = new LinkedBlockingDeque<>();
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < elementSize; i++) {
                  deq.offer(i);
                }
              }
            })
        .start();
    int i = 0;
    while (i < elementSize) {
      if (deq.size() == 0) {
        assertThat(i).isEqualTo((int) deq.take());
        i++;
      }
      List<Integer> list = new ArrayList<Integer>();
      deq.drainTo(list);
      for (int v : list) {
        assertThat(i).isEqualTo(v);
        i++;
      }
    }
  }
}

package org.magcruise.gaming.examples;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;

public class ValidateUtils {

  public static void validate(Object[] expected, Object[] actual, int fromRound, int toRound) {
    Object[] expectedSub = Arrays.asList(expected).subList(fromRound, toRound).toArray();
    Object[] actualSub = Arrays.asList(actual).subList(fromRound, toRound).toArray();
    assertThat(actualSub).containsExactly(expectedSub);
  }
}

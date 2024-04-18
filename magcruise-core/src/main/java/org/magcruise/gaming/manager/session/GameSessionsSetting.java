package org.magcruise.gaming.manager.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.executor.aws.AwsServersSetting;

public class GameSessionsSetting {

  private List<GameSessionSeed> seeds = new ArrayList<>();

  private String ownerId = "";

  private String hostName;
  private String webUi;

  private AwsServersSetting awsServersSetting;

  public List<GameSessionSeed> getSeeds() {
    return seeds;
  }

  public void setSeeds(GameSessionSeed[] seeds) {
    this.seeds.addAll(Arrays.asList(seeds));
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getWebUi() {
    return webUi;
  }

  public void setWebUi(String webUi) {
    this.webUi = webUi;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public AwsServersSetting getAwsServersSetting() {
    return awsServersSetting;
  }

  public void setAwsServersSetting(AwsServersSetting awsSettings) {
    this.awsServersSetting = awsSettings;
  }

}

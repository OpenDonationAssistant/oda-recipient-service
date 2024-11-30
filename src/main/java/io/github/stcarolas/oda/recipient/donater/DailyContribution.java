package io.github.stcarolas.oda.recipient.donater;

import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

@Serdeable
public class DailyContribution {

  private Map<String, Amount> contributions;

  public DailyContribution(){
    this.contributions = new HashMap<>();
  }

  public DailyContribution(Map<String, Amount> contributions){
    this.contributions = contributions;
  }

  public Map<String, Amount> getContributions() {
    return contributions;
  }

  public void setContributions(Map<String, Amount> contributions) {
    this.contributions = contributions;
  }
}

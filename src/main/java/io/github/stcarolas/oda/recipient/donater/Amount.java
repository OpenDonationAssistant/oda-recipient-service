package io.github.stcarolas.oda.recipient.donater;

import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.model.DataType;

@Serdeable
@TypeDef(type = DataType.STRING, converter = AmountConverter.class)
public class Amount {

  private Integer minor;
  private Integer major;
  private String currency;

  public Amount(Integer major, Integer minor, String currency) {
    this.minor = minor;
    this.major = major;
    this.currency = currency;
  }

  public Integer getMinor() {
    return minor;
  }

  public Integer getMajor() {
    return major;
  }

  public String getCurrency() {
    return currency;
  }
}

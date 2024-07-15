/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TestClass {

  private String stringValue;
  private boolean aBoolean;

  private List<String> listOfStrings;

  private BigDecimal bigDecimal;


  private Map<Integer, String> int2StringMap;

  public Map<Integer, String> getInt2StringMap() {
    return int2StringMap;
  }

  public void setInt2StringMap(Map<Integer, String> int2StringMap) {
    this.int2StringMap = int2StringMap;
  }


  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public boolean isaBoolean() {
    return aBoolean;
  }

  public void setaBoolean(boolean aBoolean) {
    this.aBoolean = aBoolean;
  }

  public List<String> getListOfStrings() {
    return listOfStrings;
  }

  public void setListOfStrings(List<String> listOfStrings) {
    this.listOfStrings = listOfStrings;
  }

  public BigDecimal getBigDecimal() {
    return bigDecimal;
  }

  public void setBigDecimal(BigDecimal bigDecimal) {
    this.bigDecimal = bigDecimal;
  }
}

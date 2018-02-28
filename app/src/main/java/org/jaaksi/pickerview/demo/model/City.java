package org.jaaksi.pickerview.demo.model;

import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：
 */

public class City implements OptionDataSet {

  public int id;
  public String name;
  public List<County> counties;

  @Override public CharSequence getCharSequence() {
    return name;
  }

  @Override public List<County> getSubs() {
    return counties;
  }

  @Override public String getValue() {
    return String.valueOf(id);
  }
}

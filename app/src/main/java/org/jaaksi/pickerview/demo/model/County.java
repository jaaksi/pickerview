package org.jaaksi.pickerview.demo.model;

import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;

/**
 * Created by fuchaoyang on 2018/2/11.<br/>
 * description：
 */

public class County implements OptionDataSet {
  public int id;
  public String name;

  @Override public CharSequence getCharSequence() {
    return name;
  }

  @Override public List<OptionDataSet> getSubs() {
    return null;
  }

  @Override public String getValue() {
    return String.valueOf(id);
  }
}

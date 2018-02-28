package org.jaaksi.pickerview.demo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by fuchaoyang on 2018/2/10.<br/>
 * description：
 */

public class BaseActivity extends AppCompatActivity {
  protected Activity mContext;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
  }

  protected void openFragment(Fragment fragment) {
    getFragmentManager().beginTransaction()
      .add(android.R.id.content, fragment)
      .addToBackStack(null)
      .commit();
  }
}

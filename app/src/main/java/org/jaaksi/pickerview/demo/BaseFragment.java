package org.jaaksi.pickerview.demo;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 创建时间：2018年02月12日13:56 <br>
 * 作者：fuchaoyang <br>
 * 描述：
 */

public abstract class BaseFragment extends Fragment {
  protected Activity mActivity;

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
    mActivity = getActivity();
    View view = inflater.inflate(getLayoutId(), container, false);
    view.setBackgroundColor(Color.WHITE);
    view.setClickable(true);
    initView(view);
    return view;
  }

  protected abstract int getLayoutId();

  protected abstract void initView(View view);

  protected void openFragment(Fragment fragment) {
    getFragmentManager().beginTransaction()
      .add(android.R.id.content, fragment)
      .addToBackStack(null)
      .commit();
  }
}

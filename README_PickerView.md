# PickerView
本库中使用的PickerView基于 [ScrollPickerView](https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/ScrollPickerView.java)，
做了部分优化和调整，如下：
 * 改为adapter填充数据，不再直接持有数据源。提供两种常用adapter：NumericWheelAdapter和ArrayWheelAdapter
 * 增加属性itemSize，支持高度为wrap_content时，根据itemCount和visibleItemCount计算总高度，便于动态改变visibleItemCount
 * 提供Formatter接口。外界可以对显示的文案处理。比如把2018变成2018年，8变成 08月。MixedTimePicker中用处更大
 * 中心item装饰器由绘制drawable改为由接口CenterDecoration控制，提供默认实现。用户可以自定义，更强大，使用更方便
 * 修改数据源size < visibleItemCount时强制不进行循环
 * 扩展PickerView数据不仅仅支持String，支持任意数据。提供PickerDataSet数据实体接口，方便设置数据
 * 修改默认选中第0个item
 * 修复了部分bug
> *需要注意的是：该控件并不支持设置label。个人觉得label束缚较多，影响内容绘制。如果item内容长度差异大，将会更加难以控制。因此该控件并不对label进行支持，而采用更好的Formatter取代。*

## BasePickerView
PickerView基类([ScrollPickerView](https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/ScrollPickerView.java))

### Attributes
|attribute |format |method | description|
---|---|---|---
| mAdapter | WheelAdapter | setAdapter | 设置数据适配器
| mListener | OnSelectedListener | setOnSelectedListener | 设置滑动选中监听
| mVisibleItemCount | int | setVisibleItemCount | 设置可见的item count
| mItemSize | int | setItemSize | 设置item高/宽度<br>(仅对应高/宽设置为wrap_content时有效)
| mIsCirculation | boolean | setIsCirculation | 设置是否循环绘制<br>(当visibleCount>数据个数时有效)
| mIsHorizontal | boolean | setVertical/setHorizontal | 设置是否纵向/横向，默认为纵向
| mIsInertiaScroll | boolean | setInertiaScroll | 设置快速滑动时是否惯性滚动一段距离
| mDisallowInterceptTouch | boolean | setDisallowInterceptTouch | 是否允许父元素拦截事件
| mDisallowTouch | boolean | setDisallowTouch | 设置是否允许手动触摸滚动
| mCenterPosition | int | setDisallowTouch | 设置中间item的位置(即选中的那一行)
| mCanTap | boolean | setCanTap | 设置 单击切换选项或触发点击监听器
|  |  | autoScrollFast | 自动滚动(必须设置为可循环滚动)
| mFormatter | Formatter | setFormatter | 设置内容Formatter
| mCenterDecoration | CenterDecoration | setCenterDecoration | 设置中心指示器
| mDrawIndicatorNoData | boolean | setDrawIndicatorNoData | 设置没有数据时是否绘制指示器

## Formatter
内容Formatter：用于格式化内容。如在MixedTimePicker中可以实现任意格式的时间格式
```java
  public interface Formatter {
    CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence);
  }
```
具体的Picker中会包含自己的Formatter。


## CenterDecoration
中心装饰器：用于绘制选中的position的装饰器。
```java
  public interface CenterDecoration {
    void drawIndicator(BasePickerView pickerView, Canvas canvas, int left, int top, int right,
      int bottom);
  }
```
>*本库设计时尽可能将功能实现抽象为接口方便扩展以及解耦。*

## DefaultCenterDecoration
装饰器CenterDecoration的默认实现。样式为上下两条线，中间可设置drawable
### API
|api|description|
---|---
| setLineColor | 设置lineColor
| setLineWidth | 设置lineWith 单位dp
| setDrawable | 设置CenterDecoration drawable，<br>参数可以是color或drawable
| setMargin | 设置装饰线的margin 单位px
### Simple Example
```java
    // 设置CenterDecoration
    DefaultCenterDecoration decoration = new DefaultCenterDecoration(mContext);
    decoration.setLineColor(Color.RED)
      //.setDrawable(Color.parseColor("#999999"))
      .setLineWidth(1)
      .setMargin(Util.dip2px(mContext, 10), Util.dip2px(mContext, -3), Util.dip2px(mContext, 10),
        Util.dip2px(mContext, -3));
    pickerView.setCenterDecoration(decoration);
```

## PickerView
继承自BasePickerView，常用的绘制文字的PickerView(([StringPickerView](https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/StringScrollPicker.java)))

### API
|api|description|
---|---
| setColor | 设置center out 文字 color
| setTextSize | 设置item文字大小，单位dp
| setAlignment | 设置对其方式
### Simple Example
```java
    mPickerView = view.findViewById(R.id.pickerview);
    mPickerView.setAdapter(new NumericWheelAdapter(1, 10));
    // 覆盖xml中的水平方向
    mPickerView.setHorizontal(false);
    mPickerView.setTextSize(15, 22);
    mPickerView.setIsCirculation(true);
    //mPickerView.setAlignment(Layout.Alignment.ALIGN_CENTER);
    mPickerView.setCanTap(false);
    mPickerView.setDisallowInterceptTouch(false);
    // 覆盖xml设置的7
    mPickerView.setVisibleItemCount(5);
    mPickerView.setItemSize(50);
    // 格式化内容
    mPickerView.setFormatter(new BasePickerView.Formatter() {
      @Override public CharSequence format(BasePickerView pickerView, int position,
        CharSequence charSequence) {
        return charSequence + "万年";
      }
    });
    int margin = Util.dip2px(mActivity, 2);
    DefaultCenterDecoration centerDecoration =
      new DefaultCenterDecoration(getActivity()).setLineColor(Color.GREEN)
        .setMargin(0, -margin, 0, -margin)
        .setLineWidth(1)
        .setDrawable(Color.RED);
    mPickerView.setCenterDecoration(centerDecoration);
    //mPickerView.setSelectedPosition(1);
```
# One very very user-friendly Picker library
一个非常好用的Android PickerView库，内部提供3种常用类型的Picker。支持扩展自定义Picker。
* TimePicker：时间选择器，包含日期
* MixedTimePicker:：聚合的时间选择器
* OptionPicker：联动选择器

## Screenshot
![](https://github.com/jaaksi/pickerview/blob/master/docs/TimePicker.gif)
![](https://github.com/jaaksi/pickerview/blob/master/docs/MixedTimePicker.gif)
![](https://github.com/jaaksi/pickerview/blob/master/docs/custom.png)

## APK
[Demo App](https://github.com/jaaksi/pickerview/blob/master/docs/app-debug.apk)下载连接

### [PickerView README](https://github.com/jaaksi/pickerview/blob/master/README_PickerView.md)

## Picker
通过组装PickerView实现常用的Picker选择器。上面已经列举提供的3中常用的Picker。

## BasePicker
Picker基类：封装了TopBar，PickerView容器，create and add PickerView方法，Picker弹窗等方法。
三种Picker都继承自BasePicker，你也可以继承它扩展自己的Picker。

### API
|api|description|
---|---
| setPickerBackgroundColor | 设置picker背景
| setPadding | 设置PickerView父容器padding 单位:px
| setTag | 给Picker 设置tag，用于区分不同的picker等。用法同View setTag
| getRootLayout | 获取PickerView的父容器，创建DefaultTopBar时必须指定
| setOnPickerChooseListener | 设置picker取消，确定按钮监听。可用于拦截选中操作
| setTopBar | 设置自定义TopBar
| setInterceptor | 设置拦截器
| createPickerView | 创建PickerView
| getPickerViews | 获取Picker中所有的pickerview集合
| addPicker | 将创建的PickerView 添加到上面集合中，createPickerView内部已调用该方法
| findPickerViewByTag | 通过tag找到对应的PickerView
| isScrolling | 是否滚动未停止。滚动未停止的时候，不响应Picker的取消，确定按键
| getPickerDialog | 获取Picker弹窗。可以在new之后设置dialog属性
| show | 显示picker弹窗

> 对比github上最受欢迎的同类库 [Android-PickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/TimePickerView.java)
本库将TopBar等通用相关逻辑封装在基类中，并提供代码中创建PickerView方法，不需要再依赖xml。用户自定义Picker时，继承BasePicker，只需要处理自己的逻辑即可，简单便捷。
而对Android-PickerView来说，实现自定义Picker，依然需要处理TopBar等逻辑。造成大量重复代码。

### TopBar
TopBar:TopBar通过抽象接口ITopBar来管理，实现Picker与TopBar的解耦。提供默认实现DefaultTopBar。可实现接口定制自己的TopBar。
```java
   public interface ITopBar {
     /**
      * @return topbar view
      */
     View getTopBarView();

     /**
      * @return 取消按钮view
      */
     View getBtnCancel();

     /**
      * @return 确定按钮view
      */
     View getBtnConfirm();

     /**
      * @return title view
      */
     TextView getTitleView();
   }
```

### DefaultTopBar API
|api|description|
---|---
| setDividerColor | 设置topbar bottom line color
| setDividerHeight | 设置bottom divider line height
| getDivider | 获取TopBar bottom line
| getTitleView | 获取TopBar title view

### Interceptor
拦截器：用于在pickerview创建时拦截，设置pickerview的属性。
>Picker内部并不提供对PickerView的设置方法，而是通过Interceptor实现。这种设计用来实现Picker和PickerView的属性设置完美解耦。
```java
   private void init(){
    mTimePicker.setInterceptor(new BasePicker.Interceptor() {
      @Override public void intercept(PickerView pickerView) {
        pickerView.setVisibleItemCount(5);
        // 将年月设置为循环的
        int type = (int) pickerView.getTag();
        if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
          pickerView.setIsCirculation(true);
        }
      }
    })
   }
```
这一点对比 [Android-PickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/TimePickerView.java), 每个Picker都需要声明对PickerView的设置方法，与PickerView严重耦合。需要开发者copy大量重复代码，且无法区分每一个PickerView设置不同的属性。

## TimePicker
常用的时间选择器，支持 年、月、日、时、分
  * 时间类型type的设计：自由组合、随心所欲(当然应该是有意义的)
  ```
    TYPE_YEAR | TYPE_MONTH | TYPE_DAY | TYPE_HOUR | TYPE_MINUTE
  ```
对比 Android-PickerView [TimePickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/TimePickerView.java)
  ```java
    /**
    * Android-PickerView中的设置type方法：参数设置麻烦且不易理解
    * 长度必须为6的数组，表示年月日时分秒 的显示与否，不设置则默认全部显示
    */
    setType(boolean[] type)
    
    // 本项目设置type方法：简单易懂，组合方便
    setType(TYPE_DATE | TYPE_HOUR)
  ```
  * 完美支持时间区间设置以及选中联动
  * 支持Format，如显示今年，明年

### API
|api|description|
---|---
| type | 时间类型，需要在Builder构造方法中指定，不能改变
| OnTimeSelectListener | 选中时间回调，需要在Builder构造方法中指定，不能改变
| setRangDate | 设置起止时间
| setSelectedDate | 设置选中时间戳
| setInterceptor | 设置拦截器
| setFormatter | 设置Formatter，内部提供默认的Formatter
| create | 通过Builder构建 TimePicker
| |以上是TimePicker.Builder的，下面是TimePicker的
| setFormatter | 同上
| setSelectedDate | 同上
| getType | 获取type
| hasType | 判断是否包含某种type

### Formatter
TimePicker Formatter：用于根据type和num格式化时间文案
  ```java
    public interface Formatter {
      /**
       * 根据type和num格式化时间
       *
       * @param picker picker
       * @param type 并不是模式，而是当前item所属的type，如年，时
       * @param position position
       * @param num position item显示的数字
       */
      CharSequence format(TimePicker picker, int type, int position, int num);
    }
  ``` 
内部提供默认的 Formatter实现DefaultFormatter。用户可以设置自定义Formatter或继承DefaultFormatter进行扩展。

> *TimePicker初始化，如果未设置时间区间，会使用默认区间。三种Picker都采用Builder模式初始化。且用户自定义的Picker也应该采用这种模式进行初始化。*

### Simple Example
```java
    mTimePicker = new TimePicker.Builder(mActivity, type, this)
      // 设置时间区间
      .setRangDate(1526361240000L, 1893563460000L)
      // 设置选中时间
      //.setSelectedDate()
      // 设置pickerview样式
      .setInterceptor(new BasePicker.Interceptor() {
        @Override public void intercept(PickerView pickerView) {
          pickerView.setVisibleItemCount(5);
          // 将年月设置为循环的
          int type = (int) pickerView.getTag();
          if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
            pickerView.setIsCirculation(true);
          }
        }
      })
      // 设置 Formatter
      .setFormatter(new TimePicker.DefaultFormatter() {
        // 自定义Formatter显示去年，今年，明年
        @Override public CharSequence format(TimePicker picker, int type, int position, int num) {
          if (type == TimePicker.TYPE_YEAR) {
            int offset = num - mCurrYear;
            if (offset == -1) return "去年";
            if (offset == 0) return "今年";
            if (offset == 1) return "明年";
            return num + "年";
          }

          return super.format(picker, type, position, num);
        }
      }).create();

    //mTimePicker.setSelectedDate(1549349843000L);
    mTimePicker.show();
```

## MixedTimePicker
常用的聚合时间选择器。日期（年、月、日）聚合，时间（小时、分钟）聚合。
  * 混合模式：github上的TimePicker库基本都不提供该种类型的Picker
  * 支持自定义日期格式，时间格式
  * 支持设置时间间隔
  * 支持设置区间以及选中联动
  * 支持设置纯日期，纯时间模式，采用type同TimePicker

### API
|api|description|
---|---
| type | 类型，需要在Builder构造方法中指定，不能改变
| OnTimeSelectListener | 选中时间回调，需要在Builder构造方法中指定，不能改变
| setRangDate | 设置起止时间
| setSelectedDate | 设置选中时间戳
| setTimeMinuteOffset | 设置时间间隔分钟数(60%offset==0才有效)，以0为起始边界
| setContainsStarDate | 设置mTimeMinuteOffset作用时，是否包含超出的startDate
| setContainsEndDate | 设置mTimeMinuteOffset作用时，是否包含超出的endDate
| setInterceptor | 设置拦截器
| setFormatter | 设置Formatter，内部提供默认的Formatter
| create | 通过Builder构建 MixedTimePicker
| |以上是MixedTimePicker.Builder的，下面是MixedTimePicker的
| setFormatter | 同上
| setSelectedDate | 同上
| getType | 获取type
| hasType | 判断是否包含某种type

### Formatter
MixedTimePicker Formatter：用于自定义日期和时间格式。内部提供默认的 Formatter实现。
```java
  public interface Formatter {
    /**
     * 用户可以自定义日期格式和时间格式
     *
     * @param picker picker
     * @param date 当前状态对应的日期或者时间
     * @param position 当前type所在的position
     */
    CharSequence format(MixedTimePicker picker, int type, Date date, int position);
    }
``` 
> MixedTimePicker 的 Formatter 完美体现了Formatter设计的精妙之处。用户可以根据回调中的type和date自定义日期和时间格式。比如显示今天，或 xx月xx日 星期 x

### Simple Example
```java
    mTimePicker = new MixedTimePicker.Builder(mActivity, MixedTimePicker.TYPE_ALL, this)
      // 设置不包含超出的结束时间<=
      .setContainsEndDate(false)
      // 设置时间间隔为30分钟
      .setTimeMinuteOffset(30)
      .setRangDate(1517771651000L, 1577976666000L)
      .setFormatter(new MixedTimePicker.DefaultFormatter() {
        @Override
        public CharSequence format(MixedTimePicker picker, int type, Date date, int position) {
          if (type == MixedTimePicker.TYPE_DATE) {
            CharSequence text;
            int dayOffset = DateUtil.getDayOffset(date.getTime(), System.currentTimeMillis());
            if (dayOffset == 0) {
              text = "今天";
            } else if (dayOffset == 1) {
              text = "明天";
            } else { // xx月xx日 星期 x
              text = mDateFormat.format(date);
            }
            return text;
          }
          return super.format(picker, type, date, position);
        }
      })
      .create();
    // 2018/2/5 03:14:11 - 2020/1/2 22:51:6
    Dialog pickerDialog = mTimePicker.getPickerDialog();
    pickerDialog.setCanceledOnTouchOutside(true);
    DefaultTopBar topBar = (DefaultTopBar) mTimePicker.getTopBar();
    topBar.getTitleView().setText("请选择时间");
```
> *不同于TimePicker, MixedTimePicker 由于支持纯时间模式（日期取选中时间的日期），不提供默认区间。如果模式中包含日期模式，则会强制要求设置时间区间*

## OptionPicker
  * 支持设置层级
  * 构造数据源及其简单，只需要实现OptionDataSet接口
  * 支持通过对应选中的values设置选中项。内部处理选中项逻辑，避免用户记录下标且麻烦的遍历处理

> 对比 Android-PickerView的 [OptionsPickerView](https://github.com/Bigkoo/Android-PickerView/blob/master/pickerview/src/main/java/com/bigkoo/pickerview/OptionsPickerView.java)

function | Android-PickerViews | 本控件
---|---|---
多级 | 最多支持3级(写死的) | 构造时设置级别(无限制)
构造数据源 | 需要构建每一级的集合，二三级为嵌套 | 一级数据entity实现OptionDataSet接口即可
设置数据源 | 提供三个方法，分别用于一、二、三级的 | 只需要设置一级数据集
联动选中 | 提供三个，只能设置选中的下标。<br/>需要用户自己通过多层遍历定位每一级别选中的下标，然后再设置 | 只需要传入选中的values(可变长数组)，不需要任何计算

Android-PickerView 中的 OptionsPickerView 代码。由于不知道层级，所以每个方法都提供3个用来对应（最多）3级选择。
```java
    // 提供3个选中的方法，分别对应1,2,3级联动的情况
    public void setSelectOptions(int option1)

    public void setSelectOptions(int option1, int option2)

    public void setSelectOptions(int option1, int option2, int option3)

    // 提供3个设置数据源的方法，分别对应1,2,3级联动的情况
    public void setPicker(List<T> optionsItems)

    public void setPicker(List<T> options1Items, List<List<T>> options2Items)

    public void setPicker(List<T> options1Items, List<List<T>> options2Items,
          List<List<List<T>>> options3Items) {
    }
```

本库中的OptionPicker
```java
/**
   * 根据选中的values初始化选中的position并初始化pickerview数据
   *
   * @param options data
   * @param values 选中数据的value{@link OptionDataSet#getValue()}
   */
  public void setDataWithValues(List<? extends OptionDataSet> options, String... values) {
    mOptions = options;
    setSelectedWithValues(values);
  }

  /**
   * 根据选中的values初始化选中的position
   *
   * @param values 选中数据的value{@link OptionDataSet#getValue()}，如果values[0]==null，则进行默认选中，其他为null认为没有该列
   */
  public void setSelectedWithValues(String... values) {
  ...
  }
```
> 如上面对比表格中所列举的，无论是层级，构造数据源和设置数据源，还是设置选中的选项，本库的API都十分简单，方便。

### API
|api|description|
---|---
| mHierarchy | 层级，需要在Builder构造方法中指定，不能改变
| OnOptionSelectListener | 选中回调，需要在Builder构造方法中指定，不能改变
| setInterceptor | 设置拦截器
| setFormatter | 设置Formatter
| create | 通过Builder构建 OptionPicker
| |以上是OptionPicker.Builder的，下面是OptionPicker的
| setFormatter | 同上
| setDataWithValues | 根据选中的values初始化选中的position并初始化pickerview数据。<br/>values参数为可变长数组，可以不设置。
| setDataWithIndexs | 设置数据和选中position。不建议使用，建议使用 setDataWithValues
| setSelectedWithValues | 根据选中的values初始化选中的position
| setSelectedWithIndexs | 设置选中的position。不建议使用，建议使用 setSelectedWithValues
| getOptions | 获取数据集
| getSelectedPosition | 获取选中的下标，数组size=mHierarchy，如果为-1表示该列没有数据
| getSelectedOptions | 获取选中的选项，如果指定index为null则表示该列没有数据

> 需要注意的是：本库中的OptionPicker只用于联动的，不支持多级别且不联动。
基本没有这种需求，如果大家有这种需求，我会在后续迭代中支持。

## Others
> 奇葩设计：部分default属性声明为static而非final

### 全局设置default属性
奇葩也好，亮点也罢。作为一个UI控件，不同的app，不同的UI，不同的产品自然会有不同的样式。
考虑到在一个app中我们会用到很多Picker，而我们又需要定制自己的UI的样式，如果通过动态方法设置样式就太麻烦了。
故做此设计。你可以通过配置这些static变量来快速定制一个满足自己app样式需求的Picker。
当然你也可以通过封装方法来处理PickerView，Picker,装饰器等样式，但这样一样十分麻烦。我相信你自己都会烦。

### 静态默认值
> *所有的这些静态属性值都以 sDefault 开头*

* BasePickerView

field|description|defaultValue
---|---|---
sDefaultVisibleItemCount | 默认可见的item个数 | 5
sDefaultItemSize | 默认itemSize | 50(dp)
sDefaultIsCirculation | 默认是否循环 | false

* PickerView

field|description|defaultValue
---|---|---
sOutTextSize | default out text size | 18(dp)
sCenterTextSize | default center text size | 22(dp)
sCenterColor | default center text color | Color.BLUE
sOutColor | default out text color | Color.GRAY

* BasePicker

field|description|defaultValue
---|---|---
sDefaultPaddingRect | pickerView父容器的 default padding | null(无padding)
sDefaultPickerBackgroundColor | default picker background color | Color.WHITE
sDefaultTopBarCreator | 用于构建自定义defaultTopBar的接口 | null

* DefaultCenterDecoration

field|description|defaultValue
---|---|---
sDefaultLineColor | default line color | Color.BLUE
sDefaultLineWidth | default line width | 1(dp)
sDefaultDrawable | default item background drawable | null
sDefaultMarginRect | default line margin | null(无margin)


> **建议初始化这些属性值放到Application中完成，避免app发生crash而导致失效**

### Simple Example
```java
public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    // 建议在application中初始化picker 默认属性实现全局设置
    initDefaultPicker();
  }

  private void initDefaultPicker() {
    // 利用修改静态默认属性值，快速定制一套满足自己app样式需求的Picker.
    // BasePickerView
    PickerView.sDefaultVisibleItemCount = 3;
    PickerView.sDefaultItemSize = 50;
    PickerView.sDefaultIsCirculation = true;

    // PickerView
    PickerView.sOutTextSize = 18;
    PickerView.sCenterTextSize = 18;
    PickerView.sCenterColor = Color.RED;
    PickerView.sOutColor = Color.GRAY;

    // BasePicker
    int padding = Util.dip2px(this, 20);
    BasePicker.sDefaultPaddingRect = new Rect(padding, padding, padding, padding);
    BasePicker.sDefaultPickerBackgroundColor = Color.WHITE;
    // 自定义 TopBar
    BasePicker.sDefaultTopBarCreator = new BasePicker.IDefaultTopBarCreator() {
      @Override public ITopBar createDefaultTopBar(LinearLayout parent) {
        return new CustomTopBar(parent);
      }
    };

    // DefaultCenterDecoration
    DefaultCenterDecoration.sDefaultLineWidth = 1;
    DefaultCenterDecoration.sDefaultLineColor = Color.RED;
    //DefaultCenterDecoration.sDefaultDrawable = new ColorDrawable(Color.WHITE);
    int leftMargin = Util.dip2px(this, 10);
    int topMargin = Util.dip2px(this, 2);
    DefaultCenterDecoration.sDefaultMarginRect =
      new Rect(leftMargin, -topMargin, leftMargin, -topMargin);
  }
}
```

## Change Log
  > v1.0.0(2018-03-03)
  - release v1.0.0

  > v1.0.1(2018-04-13)
  - 修复MixedTimePicker选中时日期未更改
  - 修复部分手机Picker弹窗无法居底部

## Gradle
```java
    compile 'org.jaaksi:pickerview:1.0.1'
```

## Thanks
*   [ScrollPickerView](https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/ScrollPickerView.java)
*   [Android-PickerView](https://github.com/Bigkoo/Android-PickerView)
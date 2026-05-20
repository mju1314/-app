  git add .
  git commit -m "你的提交说明"
  git push



主要前端 UI 界面和代码位置如下。

  应用骨架

  - 应用导航与整体 Scaffold： ExpenseTrackerApp.kt
  - 路由定义、底部导航项： AppState.kt

  页面级界面

  - 首页 HomeRoute / HomeScreen：
    HomeRoute.kt
  - 明细页 RecordsRoute / RecordsScreen：
    RecordsRoute.kt
  - 记录详情页 RecordDetailRoute / RecordDetailScreen：
    RecordDetailRoute.kt
  - 统计页 StatsRoute / StatsScreen：
    StatsRoute.kt
  - 设置页 SettingsRoute / SettingsScreen：
    SettingsRoute.kt
  - 新增记账页 AddExpenseRoute / AddExpenseScreen：
    AddExpenseRoute.kt
  - 占位页组件：
    PlaceholderScreen.kt

  页面状态与交互逻辑

  - 首页状态 / ViewModel：
    HomeUiState.kt
    HomeViewModel.kt
  - 明细状态 / ViewModel：
    RecordsUiState.kt
    RecordsViewModel.kt
  - 记录详情状态 / ViewModel：
    RecordDetailUiState.kt
    RecordDetailViewModel.kt
  - 统计状态 / ViewModel：
    StatsUiState.kt
    StatsViewModel.kt
  - 设置状态 / ViewModel：
    SettingsUiState.kt
    SettingsViewModel.kt
  - 新增记账状态 / ViewModel：
    AddExpenseUiState.kt
    AddExpenseViewModel.kt

  公共 UI 组件

  - 卡片容器： SectionCard.kt
  - 可编辑日期时间组件： EditableDateTimeField.kt

  主题样式

  - 主题入口： Theme.kt
  - 颜色： Color.kt
  - 字体排版： Type.kt
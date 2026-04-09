# ExpenseTracker

一个面向**个人自用**的 Android 离线记账 App。  
当前版本已经完成核心记账闭环，并支持 CSV 导出、本地备份与本地恢复。

## 当前已完成

### 核心记账链路
- 首页查看今日 / 本月支出
- 新增一笔支出
- 明细列表查看记录
- 统计页查看本月分类汇总与近 7 天趋势
- 记录详情编辑 / 删除

### 设置能力
- 默认货币切换
- CSV 导出
- 本地备份（数据库 + DataStore）
- 本地恢复（恢复后自动重启应用）
- 清空记录

### 数据层
- Room 本地数据库
- DataStore 本地偏好设置
- Hilt 依赖注入
- Jetpack Compose UI

## 已验证
- 主链路联调通过：
  - 新增记录
  - 首页刷新
  - 明细刷新
  - 统计刷新
  - 编辑记录
  - 删除记录
- CSV 导出可用
- 本地备份可用
- 本地恢复可用

## 技术栈
- Kotlin
- Jetpack Compose
- Room
- DataStore
- Hilt
- Navigation Compose

## 本地运行

### 环境要求
- JDK 17
- Android SDK 34
- Gradle 8.6

### 构建
```powershell
.\gradlew.bat assembleDebug
```

### 安装到设备 / 模拟器
```powershell
.\gradlew.bat installDebug
```

## 当前项目定位
这个项目优先目标不是做成复杂的财务系统，而是：

- 离线可用
- 录入快速
- 数据安全
- 适合个人长期使用

## 后续计划
- 分类管理
- 支付方式管理
- 记账时间可编辑
- 明细筛选 / 搜索
- 统计月份切换
- 统计能力增强
- 数据库迁移与基础测试


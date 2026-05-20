# Android 个人记账 App MVP 方案

## 1. 产品目标

### 产品定位

这是一款面向单个用户、自用场景的 Android 记账应用。

核心目标：

- 3 秒内完成一笔支出记录
- 完全离线可用
- 能清楚回看日支出和月支出

### 产品原则

- 录入速度优先于功能丰富度
- 本地优先优先于云同步
- 统计清晰优先于复杂图表
- 稳定使用习惯优先于过细分类

## 2. MVP 范围

### MVP 包含功能

- 新增一笔支出
- 编辑一笔记录
- 删除一笔记录
- 查看今日支出总额
- 查看本月支出总额
- 查看最近记录列表
- 查看本月分类统计
- 分类管理
- 支付方式管理
- 导出 CSV
- 本地备份与恢复

### MVP 不包含功能

- 收入记录
- 预算管理
- 多账户资产管理
- 借还款流程
- 多人协作
- 登录系统
- 云同步
- 小票 OCR 识别
- 第三方账单导入

## 3. 目标使用场景

### 主场景

用户完成一笔消费后，打开 App，输入金额，点击分类，立即保存。

### 高频场景

- 早餐、午餐、晚餐
- 通勤和交通支出
- 购物和日用品支出
- 咖啡、零食、小额娱乐支出

### 低频但重要的场景

- 回看本月总花费
- 查看某个分类花了多少钱
- 修正错误记录
- 导出数据做本地备份

## 4. 信息架构

底部导航 4 个页面即可。

1. 首页
2. 明细
3. 统计
4. 设置

悬浮主按钮：

- 记一笔

## 5. 页面设计

## 5.1 首页

### 页面目标

让用户快速了解当前支出情况，并能立即进入记账流程。

### 页面模块

- 今日支出
- 本月支出
- 最近记录，默认 10 条
- 快速新增按钮
- 可选的快捷模板区域

### 线框示意

```text
+--------------------------------------------------+
| 本月支出：¥1,268                                 |
| 今日支出：¥42                                    |
+--------------------------------------------------+
| 最近记录                                         |
| 04-08  午餐         ¥24     餐饮                 |
| 04-08  咖啡         ¥18     餐饮                 |
| 04-07  地铁         ¥3      交通                 |
| 04-07  零食         ¥12     日用                 |
+--------------------------------------------------+
|                    [+ 记一笔]                    |
+--------------------------------------------------+
```

### 验收标准

- App 默认进入首页
- 用户可以一键进入新增支出页面
- 最近记录从本地数据库读取，1 秒内展示

## 5.2 记一笔页面

### 页面目标

让记账尽可能快，尽可能少思考。

### 字段

- 金额
- 分类
- 时间
- 支付方式
- 备注

### 交互规则

- 默认聚焦金额输入框
- 时间默认当前时间
- 支付方式默认上一次使用的方式
- 备注为可选项
- 保存按钮始终可见
- 金额和分类填写后即可保存

### 线框示意

```text
+--------------------------------------------------+
| 金额                                             |
| ¥ 0.00                                           |
+--------------------------------------------------+
| 分类                                             |
| [餐饮] [交通] [购物] [日用]                      |
| [娱乐] [医疗] [其他]                            |
+--------------------------------------------------+
| 时间            2026-04-08 19:30                |
| 支付方式        支付宝                           |
| 备注            可不填                           |
+--------------------------------------------------+
|                  [保存支出]                      |
+--------------------------------------------------+
```

### 验收标准

- 正常场景下，用户输入金额后，3 次点击内完成记录
- 保存后立即写入本地数据库
- 保存成功后返回上页，或者清空表单继续录入

## 5.3 明细页面

### 页面目标

让用户高效浏览、筛选和管理历史记录。

### 页面模块

- 按天分组的记录列表
- 按日期范围筛选
- 按分类筛选
- 按备注搜索
- 点击记录进入编辑
- 滑动或菜单删除

### 线框示意

```text
+--------------------------------------------------+
| 筛选：[本月] [全部分类]                          |
+--------------------------------------------------+
| 2026-04-08   合计 ¥42                            |
| 午餐               ¥24      餐饮                 |
| 咖啡               ¥18      餐饮                 |
|                                                  |
| 2026-04-07   合计 ¥15                            |
| 地铁               ¥3       交通                 |
| 零食               ¥12      日用                 |
+--------------------------------------------------+
```

### 验收标准

- 按 `spent_at` 倒序排序
- 编辑记录后首页和统计页立即刷新
- 删除记录前有确认提示

## 5.4 统计页面

### 页面目标

提供足够有用的统计，而不是做成复杂的数据分析工具。

### MVP 模块

- 本月总支出
- 分类分布
- 每日支出趋势
- 支出前三的分类

### 线框示意

```text
+--------------------------------------------------+
| 本月总支出：¥1,268                               |
+--------------------------------------------------+
| 分类分布                                         |
| 餐饮            ¥520   41%                       |
| 购物            ¥320   25%                       |
| 交通            ¥180   14%                       |
+--------------------------------------------------+
| 每日趋势                                         |
| 01 02 03 04 05 06 07 08                          |
| ▁ ▂ ▃ ▂ ▅ ▃ ▁ ▂                                  |
+--------------------------------------------------+
```

### 验收标准

- 所有统计仅依赖本地数据库
- 支持切换月份
- 新增、编辑、删除后统计自动刷新

## 5.5 设置页面

### 页面目标

集中处理基础配置和数据安全相关操作。

### 页面模块

- 分类管理
- 支付方式管理
- 导出 CSV
- 本地备份
- 本地恢复
- 默认偏好设置

### 验收标准

- 用户可以新增、编辑、归档分类
- 用户可以新增和编辑支付方式
- 导出的 CSV 可正常打开
- 无需后端服务即可进行备份和恢复

## 6. 核心用户流程

## 6.1 新增支出

1. 打开 App
2. 点击“记一笔”
3. 输入金额
4. 选择分类
5. 点击保存

可选动作：

- 调整时间
- 更换支付方式
- 补充备注

## 6.2 编辑支出

1. 打开明细页
2. 点击某条记录
3. 修改字段
4. 保存修改

## 6.3 导出数据

1. 打开设置页
2. 点击“导出 CSV”
3. 选择保存位置
4. 保存文件

## 7. 推荐默认分类

第一版分类应保持少而稳。

- 餐饮
- 交通
- 购物
- 日用
- 娱乐
- 医疗
- 其他

分类规则：

- MVP 阶段总分类数尽量控制在 10 个以内
- 默认分类建议支持归档，不建议直接删除
- 使用图标加文字，便于快速识别

## 8. 推荐默认支付方式

- 现金
- 支付宝
- 微信支付
- 银行卡
- 其他

## 9. Android 技术栈建议

### 推荐技术栈

- 语言：Kotlin
- UI：Jetpack Compose
- 架构：MVVM
- 本地数据库：Room
- 偏好存储：DataStore
- 依赖注入：Hilt
- 页面导航：Navigation Compose
- 日期时间：`java.time`
- 数据导出：本地生成 CSV 文件

### 项目结构建议

MVP 阶段，一个 `app` 模块就够用。

建议包结构：

```text
com.example.expensetracker
├─ data
│  ├─ db
│  ├─ dao
│  ├─ entity
│  ├─ repository
├─ domain
│  ├─ model
│  ├─ usecase
├─ ui
│  ├─ home
│  ├─ add
│  ├─ records
│  ├─ stats
│  ├─ settings
│  ├─ components
├─ common
│  ├─ utils
│  ├─ extensions
```

## 10. Room 数据库设计

## 10.1 transactions

用于存储每一笔支出记录。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 主键，自增 |
| amount | Decimal 或 Long | 建议按分存储 |
| category_id | Long | 关联 `categories` |
| payment_method_id | Long | 关联 `payment_methods` |
| note | String | 可为空 |
| spent_at | Long | 实际消费时间戳 |
| created_at | Long | 创建时间戳 |
| updated_at | Long | 更新时间戳 |

设计建议：

- 金额统一按“分”存储，避免浮点误差
- `spent_at` 表示实际消费时间
- `created_at` 和 `updated_at` 用于审计和排序补充

## 10.2 categories

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 主键，自增 |
| name | String | 分类名称，唯一 |
| icon | String | 图标标识 |
| sort_order | Int | 排序用 |
| is_default | Boolean | 是否默认分类 |
| is_archived | Boolean | 是否归档 |
| created_at | Long | 创建时间戳 |
| updated_at | Long | 更新时间戳 |

## 10.3 payment_methods

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 主键，自增 |
| name | String | 名称唯一 |
| sort_order | Int | 排序用 |
| is_default | Boolean | 是否默认 |
| is_archived | Boolean | 是否归档 |
| created_at | Long | 创建时间戳 |
| updated_at | Long | 更新时间戳 |

## 10.4 quick_templates

这是一个可选表，更适合作为 MVP 1.5 阶段功能。如果时间紧，可以先不做。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 主键，自增 |
| name | String | 例如“早餐” |
| amount | Long | 按分存储 |
| category_id | Long | 外键 |
| payment_method_id | Long | 外键 |
| note | String | 默认备注，可为空 |
| sort_order | Int | 排序用 |
| created_at | Long | 创建时间戳 |
| updated_at | Long | 更新时间戳 |

## 10.5 app_preferences

如果偏好数据用 DataStore，则不需要建 Room 表。

建议保存的键：

- `last_used_payment_method_id`
- `default_stats_month`
- `enable_quick_templates`
- `export_last_path`

## 11. Entity 设计建议

## 11.1 TransactionEntity

```kotlin
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["payment_method_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("category_id"),
        Index("payment_method_id"),
        Index("spent_at")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount") val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "payment_method_id") val paymentMethodId: Long,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "spent_at") val spentAt: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
```

## 11.2 CategoryEntity

```kotlin
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon") val icon: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
```

## 11.3 PaymentMethodEntity

```kotlin
@Entity(tableName = "payment_methods", indices = [Index(value = ["name"], unique = true)])
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
```

## 12. DAO 设计

MVP 阶段至少需要以下能力：

### TransactionDao

- 插入记录
- 更新记录
- 删除记录
- 查询最近记录
- 按月份查询记录
- 查询按天汇总的数据
- 查询月度分类汇总
- 查询今日总支出
- 查询本月总支出

### CategoryDao

- 查询有效分类
- 新增分类
- 更新分类
- 归档分类

### PaymentMethodDao

- 查询有效支付方式
- 新增支付方式
- 更新支付方式
- 归档支付方式

## 13. 状态管理与页面 ViewModel

每个页面建议独立一个 `ViewModel`。

### HomeViewModel

- 加载今日总支出
- 加载本月总支出
- 加载最近记录

### AddExpenseViewModel

- 管理表单状态
- 校验金额和分类
- 保存记录
- 读取默认支付方式

### RecordsViewModel

- 加载分组后的记录列表
- 应用筛选条件
- 删除记录

### StatsViewModel

- 加载月汇总
- 加载分类统计
- 加载每日趋势

### SettingsViewModel

- 管理分类
- 管理支付方式
- 处理导出和恢复

## 14. 页面导航建议

路由建议：

- `home`
- `add_expense`
- `records`
- `record_detail/{id}`
- `stats`
- `settings`
- `manage_categories`
- `manage_payment_methods`

规则建议：

- `add_expense` 可以用全屏页面，也可以用底部弹层
- `record_detail/{id}` 可以复用新增页面表单，切换为编辑模式

## 15. UI 组件清单

建议尽早抽出的复用组件：

- AmountInput
- CategoryGrid
- PaymentMethodSelector
- RecordListItem
- DailyRecordGroup
- SummaryCard
- EmptyStateView
- ConfirmDeleteDialog

## 16. MVP 开发拆解

## 阶段一：项目基础搭建

目标：

- 创建 Android 项目
- 接入 Compose、Room、Hilt、DataStore
- 搭建包结构和导航骨架

任务：

1. 初始化 Kotlin + Compose 项目
2. 添加 Room、Hilt、Navigation Compose、DataStore 依赖
3. 创建基础主题和 App Scaffold
4. 配置依赖注入和本地数据库
5. 创建底部导航框架

交付结果：

- App 能正常启动
- 底部导航可切换
- 空页面能正常打开

## 阶段二：核心数据层

目标：

- 完成实体、DAO、数据库和仓库层

任务：

1. 创建 Room Entity
2. 创建 DAO 接口
3. 创建 Database 类
4. 创建 Repository
5. 初始化默认分类和默认支付方式

交付结果：

- 本地数据库可读写
- 首次启动能看到默认数据

## 阶段三：记一笔流程

目标：

- 优先完成价值最高的功能

任务：

1. 搭建记一笔页面 UI
2. 实现金额校验
3. 实现分类和支付方式选择
4. 将记录写入数据库
5. 将上次使用的支付方式写入 DataStore

交付结果：

- 用户可以成功新增一笔支出

## 阶段四：首页

目标：

- 让用户在记账后立刻看到反馈

任务：

1. 加载今日总支出
2. 加载本月总支出
3. 加载最近记录
4. 接入悬浮“记一笔”按钮

交付结果：

- 首页能实时反映新记录

## 阶段五：明细管理

目标：

- 让历史记录可回看、可修改、可删除

任务：

1. 实现按天分组的记录列表
2. 实现记录详情和编辑页
3. 实现删除确认流程
4. 实现日期和分类筛选

交付结果：

- 用户可以查看、编辑和删除记录

## 阶段六：统计

目标：

- 提供轻量但有用的月度支出分析

任务：

1. 查询月总支出
2. 查询分类总额
3. 查询每日趋势
4. 搭建统计页 UI

交付结果：

- 用户可以查看基础月度消费分析

## 阶段七：设置与数据安全

目标：

- 让 App 可长期稳定使用

任务：

1. 实现分类管理
2. 实现支付方式管理
3. 实现 CSV 导出
4. 实现本地备份和恢复

交付结果：

- 用户可以维护基础数据并导出个人记录

## 17. MVP 里程碑建议

如果你是业余时间独立开发：

- 第 1 周：基础工程 + 数据库
- 第 2 周：记一笔 + 首页
- 第 3 周：明细编辑/删除
- 第 4 周：统计 + 设置 + 导出

如果时间有限，最小可用版本可以先停在：

- 记一笔
- 首页
- 明细

统计和备份可以作为下一阶段再补。

## 18. MVP 验收清单

在认为 MVP 完成前，至少确认以下项目：

- 用户能在 3 秒内完成一笔正常记账
- App 重启后数据仍然存在
- 首页总额显示正确
- 明细列表排序正确
- 编辑记录后总额和统计会同步更新
- 删除记录后总额和统计会同步更新
- 分类归档不会影响旧记录显示
- 导出的 CSV 可在表格软件中正常打开
- 断网状态下可正常使用

## 19. 后续扩展方向

以下不属于 MVP，但适合作为下一阶段：

- 收入记录
- 预算提醒
- 首页快捷模板
- 桌面小组件
- 定时提醒记账
- 小票 OCR 识别
- 导入支付宝或微信账单
- 加密本地备份
- 多设备云同步

## 20. 最终建议

如果你想最快做出一个真正能长期用的个人产品，建议按这个顺序开发：

1. 记一笔
2. 首页汇总
3. 明细编辑/删除
4. 统计
5. 导出与备份

始终用一个标准来判断产品是否做对：

两周之后，你自己还愿不愿意每天打开它。

如果答案是否定的，先继续简化录入流程，而不是继续加功能。

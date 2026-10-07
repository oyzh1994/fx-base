# fx-gui SVG 图标控件（svg.glyph）

- 覆盖包：`cn.oyzh.fx.gui.svg.glyph` 及其子包 `database` / `page` / `key` / `layout` / `alert` / `file`。
- 全部为 SVG 图标控件：继承 `cn.oyzh.fx.plus.controls.svg.SVGGlyph`（部分继承 `ScalingSVGGlyph` 并重写缩放比）。
- 共性结构：无参构造 `super("<svg 路径>")` 加载图标；`(String size)` 构造 `this()` 后 `setSizeStr(size)`；部分类含被注释掉的 `initNode()`（设置提示文本），属死代码，未展开。
- 已跳过整文件被注释的死代码：`svg/glyph/FileSVGGlyph.java`、`svg/glyph/FolderSVGGlyph.java`（注意 `file/FileSVGGlyph.java`、`file/FolderSVGGlyph.java` 为正常代码，保留）。
- `CancelSVGGlyph` 继承 `CloseSVGGlyph`（复用 close.svg，缩放 0.75）。


## 通用图标（svg.glyph 根目录）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| AboutSVGGlyph | 关于 SVG 图标控件 | `/fx-svg/info-circle.svg` |  |
| AccessControlSVGGlyph | 访问控制 SVG 图标控件 | `/fx-svg/access-control.svg` |  |
| AddDocumentSVGGlyph | 新增文档 SVG 图标控件 | `/fx-svg/add-document.svg` | widthScaling=0.9 |
| AddFillSVGGlyph | 新增（填充） SVG 图标控件 | `/fx-svg/add-fill.svg` |  |
| AddGroupSVGGlyph | 新增分组 SVG 图标控件 | `/fx-svg/addGroup.svg` |  |
| AddHostSVGGlyph | 新增主机 SVG 图标控件 | `/fx-svg/addHost.svg` |  |
| AddSVGGlyph | 新增 SVG 图标控件 | `/fx-svg/add.svg` |  |
| Apply1SVGGlyph | 应用1 SVG 图标控件 | `/fx-svg/apply1.svg` | sizeScaling=0.9,widthScaling=1.1,heightScaling=0.9 |
| ApplySVGGlyph | 应用 SVG 图标控件 | `/fx-svg/apply.svg` |  |
| BatchOptSVGGlyph | 批量操作 SVG 图标控件 | `/fx-svg/mml-batch-command-16.svg` |  |
| BinarySVGGlyph | 二进制 SVG 图标控件 | `/fx-svg/binary.svg` |  |
| BoxSVGGlyph | 盒子 SVG 图标控件 | `/fx-svg/box.svg` |  |
| BucketSVGGlyph | 桶 SVG 图标控件 | `/fx-svg/bucket.svg` |  |
| CalcSVGGlyph | 计算 SVG 图标控件 | `/fx-svg/calc.svg` |  |
| CancelSVGGlyph | 取消 SVG 图标控件 | `(继承父类)` | 继承 CloseSVGGlyph |
| ChangelogSVGGlyph | 更新日志 SVG 图标控件 | `/fx-svg/changelog.svg` |  |
| ChooseSVGGlyph | 选择 SVG 图标控件 | `/fx-svg/choose.svg` |  |
| ClearSVGGlyph | 清空 SVG 图标控件 | `/fx-svg/clear.svg` |  |
| CloseSVGGlyph | 关闭 SVG 图标控件 | `/fx-svg/close.svg` | sizeScaling=0.75 |
| CollapseAllSVGGlyph | 全部折叠 SVG 图标控件 | `/fx-svg/vertical-align-middl.svg` |  |
| CollapseSVGGlyph | 折叠 SVG 图标控件 | `/fx-svg/left-arrow-to-left.svg` |  |
| CollectSVGGlyph | 收藏 SVG 图标控件 | `/fx-svg/star-l.svg` |  |
| CompressSVGGlyph | 压缩 SVG 图标控件 | `/fx-svg/compress.svg` |  |
| ConfigurationSVGGlyph | 配置 SVG 图标控件 | `/fx-svg/configuration.svg` |  |
| ConnectionSVGGlyph | 连接 SVG 图标控件 | `/fx-svg/connections.svg` |  |
| CopySVGGlyph | 复制 SVG 图标控件 | `/fx-svg/copy.svg` |  |
| CutSVGGlyph | 剪切 SVG 图标控件 | `/fx-svg/cut.svg` |  |
| DateSVGGlyph | 日期 SVG 图标控件 | `/fx-svg/date.svg` |  |
| DeleteForceSVGGlyph | 强制删除 SVG 图标控件 | `/fx-svg/delete-force.svg` |  |
| DeleteSVGGlyph | 删除 SVG 图标控件 | `/fx-svg/delete.svg` |  |
| DesignSVGGlyph | 设计 SVG 图标控件 | `/fx-svg/design.svg` |  |
| DesktopSVGGlyph | 桌面 SVG 图标控件 | `/fx-svg/desktop.svg` |  |
| DiscardSVGGlyph | 丢弃 SVG 图标控件 | `/fx-svg/close.svg` | sizeScaling=0.8 |
| DockerSVGGlyph | Docker SVG 图标控件 | `/fx-svg/docker.svg` |  |
| Down1SVGGlyph | 向下1 SVG 图标控件 | `/fx-svg/down1.svg` |  |
| DownloadBoxSVGGlyph | 下载盒子 SVG 图标控件 | `/fx-svg/download-box.svg` |  |
| DownloadSVGGlyph | 下载 SVG 图标控件 | `/fx-svg/download.svg` |  |
| DownSVGGlyph | 向下 SVG 图标控件 | `/fx-svg/down.svg` |  |
| EditDocumentSVGGlyph | 编辑文档 SVG 图标控件 | `/fx-svg/edit_document.svg` |  |
| EditSVGGlyph | 编辑 SVG 图标控件 | `/fx-svg/edit.svg` |  |
| EnlargeSVGGlyph | 放大 SVG 图标控件 | `/fx-svg/enlarge.svg` |  |
| ErrorInfoSVGGlyph | 错误信息 SVG 图标控件 | `/fx-svg/error-info.svg` |  |
| ExampleSVGGlyph | 示例 SVG 图标控件 | `/fx-svg/example.svg` |  |
| ExecuteSVGGlyph | 执行 SVG 图标控件 | `/fx-svg/execute.svg` |  |
| ExpandAllSVGGlyph | 全部展开 SVG 图标控件 | `/fx-svg/colum-height.svg` |  |
| ExpendSVGGlyph | 展开 SVG 图标控件 | `/fx-svg/arrow-to-right.svg` |  |
| ExportSVGGlyph | 导出 SVG 图标控件 | `/fx-svg/export.svg` |  |
| EyeCloseSVGGlyph | 闭眼 SVG 图标控件 | `/fx-svg/eye-close.svg` |  |
| EyeOpenSVGGlyph | 睁眼 SVG 图标控件 | `/fx-svg/eye-open.svg` | heightScaling=0.7 |
| EyeSVGGlyph | 眼睛 SVG 图标控件 | `/fx-svg/eye.svg` | widthScaling=1.2 |
| FilterSVGGlyph | 过滤 SVG 图标控件 | `/fx-svg/filter.svg` |  |
| ForceKillSVGGlyph | 强制终止 SVG 图标控件 | `/fx-svg/force-kill.svg` |  |
| GenerateSVGGlyph | 生成 SVG 图标控件 | `/fx-svg/generate.svg` |  |
| GroupSVGGlyph | 分组 SVG 图标控件 | `/fx-svg/group.svg` |  |
| HexSVGGlyph | 十六进制 SVG 图标控件 | `/fx-svg/HEX.svg` |  |
| HiddenSVGGlyph | 隐藏 SVG 图标控件 | `/fx-svg/hidden.svg` |  |
| HistorySVGGlyph | 历史 SVG 图标控件 | `/fx-svg/history.svg` |  |
| HomeSVGGlyph | 主页 SVG 图标控件 | `/fx-svg/home.svg` |  |
| HostSVGGlyph | 主机 SVG 图标控件 | `/fx-svg/host.svg` |  |
| ImportSVGGlyph | 导入 SVG 图标控件 | `/fx-svg/import.svg` |  |
| InfoSVGGlyph | 信息 SVG 图标控件 | `/fx-svg/info-circle.svg` |  |
| JSONSVGGlyph | JSON SVG 图标控件 | `/fx-svg/json.svg` |  |
| KeywordsSVGGlyph | 关键字 SVG 图标控件 | `/fx-svg/keywords.svg` |  |
| KillSVGGlyph | 终止 SVG 图标控件 | `/fx-svg/kill.svg` |  |
| LoadAllSVGGlyph | 全部加载 SVG 图标控件 | `/fx-svg/reload-time.svg` |  |
| LockSVGGlyph | 锁定 SVG 图标控件 | `/fx-svg/lock.svg` |  |
| LogSVGGlyph | 日志 SVG 图标控件 | `/fx-svg/log.svg` |  |
| MatchCaseSVGGlyph | 区分大小写 SVG 图标控件 | `/fx-svg/match_case.svg` | heightScaling=0.83 |
| MessageSVGGlyph | 消息 SVG 图标控件 | `/fx-svg/message.svg` |  |
| MigrationSVGGlyph | 迁移 SVG 图标控件 | `/fx-svg/migration.svg` |  |
| MinusSVGGlyph | 减号 SVG 图标控件 | `/fx-svg/minus.svg` |  |
| MonitorSVGGlyph | 监视器 SVG 图标控件 | `/fx-svg/monitor.svg` |  |
| MoreSVGGlyph | 更多 SVG 图标控件 | `/fx-svg/more.svg` |  |
| MoveDownSVGGlyph | 下移 SVG 图标控件 | `/fx-svg/direction-down.svg` |  |
| MoveFolderSVGGlyph | 移动文件夹 SVG 图标控件 | `/fx-svg/move-folder.svg` |  |
| MoveSVGGlyph | 移动 SVG 图标控件 | `/fx-svg/move.svg` |  |
| MoveUpSVGGlyph | 上移 SVG 图标控件 | `/fx-svg/direction-up.svg` |  |
| MusicSVGGlyph | 音乐 SVG 图标控件 | `/fx-svg/music.svg` |  |
| NextStepSVGGlyph | 下一步 SVG 图标控件 | `/fx-svg/next-step.svg` |  |
| NextSVGGlyph | 下一个 SVG 图标控件 | `/fx-svg/direction-down.svg` |  |
| OldSVGGlyph | 旧版本 SVG 图标控件 | `/fx-svg/old.svg` |  |
| OpenSVGGlyph | 打开 SVG 图标控件 | `/fx-svg/open.svg` |  |
| PackageSVGGlyph | 打包 SVG 图标控件 | `/fx-svg/package.svg` |  |
| ParamSVGGlyph | 参数 SVG 图标控件 | `/fx-svg/param.svg` |  |
| ParentDirSVGGlyph | 上级目录 SVG 图标控件 | `/fx-svg/parent-dir.svg` |  |
| PasteSVGGlyph | 粘贴 SVG 图标控件 | `/fx-svg/file-paste.svg` | widthScaling=0.875 |
| PauseSVGGlyph | 暂停 SVG 图标控件 | `/fx-svg/pause.svg` |  |
| PermissionSVGGlyph | 权限 SVG 图标控件 | `/fx-svg/permissions.svg` |  |
| PlaySVGGlyph | 播放 SVG 图标控件 | `/fx-svg/play-circle.svg` |  |
| PortSVGGlyph | 端口 SVG 图标控件 | `/fx-svg/port.svg` |  |
| PositioningSVGGlyph | 定位 SVG 图标控件 | `/fx-svg/positioning.svg` | sizeScaling=1.05 |
| PrettySVGGlyph | 格式化 SVG 图标控件 | `/fx-svg/pretty.svg` |  |
| PrevStepSVGGlyph | 上一步 SVG 图标控件 | `/fx-svg/prev-step.svg` |  |
| PrevSVGGlyph | 上一个 SVG 图标控件 | `/fx-svg/direction-up.svg` |  |
| ProcessSVGGlyph | 进程 SVG 图标控件 | `/fx-svg/process.svg` |  |
| QRCodeSVGGlyph | 二维码 SVG 图标控件 | `/fx-svg/qrcode.svg` |  |
| QuerySVGGlyph | 查询 SVG 图标控件 | `/fx-svg/query.svg` |  |
| QuitSVGGlyph | 退出 SVG 图标控件 | `/fx-svg/poweroff.svg` |  |
| RedoSVGGlyph | 重做 SVG 图标控件 | `/fx-svg/data_redo.svg` |  |
| ReduceSVGGlyph | 缩小 SVG 图标控件 | `/fx-svg/reduce.svg` |  |
| RefreshSVGGlyph | 刷新 SVG 图标控件 | `/fx-svg/reload.svg` |  |
| RegexSVGGlyph | 正则表达式 SVG 图标控件 | `/fx-svg/regex.svg` | sizeScaling=0.85 |
| RenameSVGGlyph | 重命名 SVG 图标控件 | `/fx-svg/edit-square.svg` |  |
| RepeatSVGGlyph | 重复 SVG 图标控件 | `/fx-svg/repeated.svg` |  |
| ResetSVGGlyph | 重置 SVG 图标控件 | `/fx-svg/reset.svg` |  |
| ResourceSVGGlyph | 资源 SVG 图标控件 | `/fx-svg/resource.svg` |  |
| RestartSVGGlyph | 重启 SVG 图标控件 | `/fx-svg/restart.svg` |  |
| ReverseSVGGlyph | 反转 SVG 图标控件 | `/fx-svg/reverse.svg` |  |
| RightSideSVGGlyph | 右侧 SVG 图标控件 | `/fx-svg/rightside.svg` |  |
| RunSVGGlyph | 运行 SVG 图标控件 | `/fx-svg/run-solid.svg` |  |
| SaveSVGGlyph | 保存 SVG 图标控件 | `/fx-svg/save.svg` |  |
| ScriptSVGGlyph | 脚本 SVG 图标控件 | `/fx-svg/script.svg` |  |
| SearchSVGGlyph | 搜索 SVG 图标控件 | `/fx-svg/search.svg` |  |
| SelectSVGGlyph | 选择 SVG 图标控件 | `/fx-svg/select.svg` | widthScaling=0.9,heightScaling=0.6 |
| ServerSVGGlyph | 服务器 SVG 图标控件 | `/fx-svg/sever.svg` |  |
| SettingSVGGlyph | 设置 SVG 图标控件 | `/fx-svg/setting.svg` |  |
| SFTPSVGGlyph | SFTP SVG 图标控件 | `/fx-svg/sftp.svg` |  |
| ShowSVGGlyph | 显示 SVG 图标控件 | `/fx-svg/show.svg` |  |
| SnippetSVGGlyph | 代码片段 SVG 图标控件 | `/fx-svg/snippet.svg` |  |
| SortAscSVGGlyph | 升序排序 SVG 图标控件 | `/fx-svg/sort-ascending.svg` |  |
| SortDescSVGGlyph | 降序排序 SVG 图标控件 | `/fx-svg/sort-descending.svg` |  |
| SplitViewSVGGlyph | 分屏 SVG 图标控件 | `/fx-svg/split-view.svg` |  |
| StopSVGGlyph | 停止 SVG 图标控件 | `/fx-svg/stop-circle-line.svg` |  |
| SubmitSVGGlyph | 提交 SVG 图标控件 | `/fx-svg/check.svg` | sizeScaling=0.9,widthScaling=1.1,heightScaling=0.9 |
| TerminalSVGGlyph | 终端 SVG 图标控件 | `/fx-svg/code-library.svg` |  |
| TestSVGGlyph | 测试 SVG 图标控件 | `/fx-svg/link.svg` |  |
| TimeSVGGlyph | 时间 SVG 图标控件 | `/fx-svg/time.svg` |  |
| ToolsSVGGlyph | 工具 SVG 图标控件 | `/fx-svg/tools.svg` |  |
| TransportLeftSVGGlyph | 向左传输 SVG 图标控件 | `/fx-svg/transport-left.svg` |  |
| TransportRightSVGGlyph | 向右传输 SVG 图标控件 | `/fx-svg/transport-right.svg` |  |
| TransportSVGGlyph | 传输 SVG 图标控件 | `/fx-svg/arrow-left-right-line.svg` |  |
| TruncateSVGGlyph | 截断 SVG 图标控件 | `/fx-svg/truncate.svg` |  |
| TunnelingSVGGlyph | 隧道 SVG 图标控件 | `/fx-svg/tunneling.svg` |  |
| UnCollectSVGGlyph | 取消收藏 SVG 图标控件 | `/fx-svg/star.svg` |  |
| UnCompressSVGGlyph | 解压 SVG 图标控件 | `/fx-svg/uncompress.svg` |  |
| UndoSVGGlyph | 撤销 SVG 图标控件 | `/fx-svg/data_revoke.svg` |  |
| UnLockSVGGlyph | 解锁 SVG 图标控件 | `/fx-svg/unlock.svg` |  |
| UnPauseSVGGlyph | 取消暂停 SVG 图标控件 | `/fx-svg/un-pause.svg` |  |
| Up1SVGGlyph | 向上 SVG 图标控件 | `/fx-svg/up1.svg` |  |
| UploadBoxSVGGlyph | 上传 SVG 图标控件 | `/fx-svg/upload-box.svg` |  |
| UploadDownloadSVGGlyph | 上传下载 SVG 图标控件 | `/fx-svg/upload-download.svg` | heightScaling=0.875 |
| UploadSVGGlyph | 上传 SVG 图标控件 | `/fx-svg/upload.svg` |  |
| UpSVGGlyph | 向上 SVG 图标控件 | `/fx-svg/up.svg` |  |
| UserSVGGlyph | 用户 SVG 图标控件 | `/fx-svg/user.svg` |  |
| WarningSVGGlyph | 警告 SVG 图标控件 | `/fx-svg/warning.svg` |  |
| WholeWordSVGGlyph | 全字匹配 SVG 图标控件 | `/fx-svg/whole-word.svg` | heightScaling=0.83 |

## 数据库图标（database）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| ColumnSVGGlyph | 数据库列 SVG 图标控件 | `/fx-svg/database/column.svg` |  |
| DamengSVGGlyph | 达梦数据库 SVG 图标控件 | `/fx-svg/database/dameng.svg` |  |
| DatabaseSVGGlyph | 数据库 SVG 图标控件 | `/fx-svg/database/database.svg` |  |
| DumpSVGGlyph | 数据库导出 SVG 图标控件 | `/fx-svg/database/dump.svg` |  |
| EditTableSVGGlyph | 编辑表 SVG 图标控件 | `/fx-svg/database/edit_table.svg` |  |
| EventSVGGlyph | 数据库事件 SVG 图标控件 | `/fx-svg/database/event.svg` |  |
| ExplainSVGGlyph | 执行计划 SVG 图标控件 | `/fx-svg/database/explain.svg` |  |
| FunctionSVGGlyph | 数据库函数 SVG 图标控件 | `/fx-svg/database/function.svg` |  |
| MariadbSVGGlyph | MariaDB 数据库 SVG 图标控件 | `/fx-svg/database/mariadb.svg` |  |
| MongodbSVGGlyph | MongoDB 数据库 SVG 图标控件 | `/fx-svg/database/mongodb.svg` | widthScaling=0.7 |
| MysqlSVGGlyph | MySQL 数据库 SVG 图标控件 | `/fx-svg/database/mysql.svg` |  |
| OracleSVGGlyph | Oracle 数据库 SVG 图标控件 | `/fx-svg/database/oracle.svg` |  |
| ProcedureSVGGlyph | 存储过程 SVG 图标控件 | `/fx-svg/database/procedure.svg` |  |
| RunFileSVGGlyph | 运行文件 SVG 图标控件 | `/fx-svg/database/run-file.svg` |  |
| RunSqlFileSVGGlyph | 运行 SQL 文件 SVG 图标控件 | `/fx-svg/database/runSqlFile.svg` |  |
| SchemaSVGGlyph | 数据库模式 SVG 图标控件 | `/fx-svg/database/schema.svg` |  |
| SqlServerSVGGlyph | SQL Server 数据库 SVG 图标控件 | `/fx-svg/database/sqlserver.svg` |  |
| TableSVGGlyph | 数据库表 SVG 图标控件 | `/fx-svg/database/table.svg` |  |
| ViewSVGGlyph | 数据库视图 SVG 图标控件 | `/fx-svg/database/view.svg` |  |

## 分页图标（page）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| PageFirstSVGGlyph | 首页 SVG 图标控件 | `/fx-svg/page/page-first.svg` |  |
| PageLastSVGGlyph | 末页 SVG 图标控件 | `/fx-svg/page/page-last.svg` |  |
| PageNextSVGGlyph | 下一页 SVG 图标控件 | `/fx-svg/page/page-next.svg` |  |
| PagePrevSVGGlyph | 上一页 SVG 图标控件 | `/fx-svg/page/page-prev.svg` |  |
| PageSettingSVGGlyph | 分页设置 SVG 图标控件 | `/fx-svg/page/page-setting3.svg` |  |

## 密钥图标（key）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| GenerateKeySVGGlyph | 生成密钥 SVG 图标控件 | `/fx-svg/key/generate-key.svg` |  |
| KeySVGGlyph | 密钥 SVG 图标控件 | `/fx-svg/key/key.svg` |  |

## 布局图标（layout）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| Layout1SVGGlyph | 布局一 SVG 图标控件 | `/fx-svg/layout/layout1.svg` |  |
| Layout2SVGGlyph | 布局二 SVG 图标控件 | `/fx-svg/layout/layout2.svg` |  |

## 提示图标（alert）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| ErrorSVGGlyph | 错误提示 SVG 图标控件 | `/fx-svg/alert/error-fill.svg` |  |
| InfoSVGGlyph | 信息提示 SVG 图标控件 | `/fx-svg/alert/info-fill.svg` |  |
| QuestionSVGGlyph | 询问提示 SVG 图标控件 | `/fx-svg/alert/question-fill.svg` |  |
| WarningSVGGlyph | 警告提示 SVG 图标控件 | `/fx-svg/alert/warning-fill.svg` |  |

## 文件图标（file 根目录）

> 文件通用图标。

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| File3gpSVGGlyph | 3GP 视频文件 SVG 图标控件 | `/fx-svg/file/file-3gp.svg` |  |
| File7zSVGGlyph | 7Z 压缩文件 SVG 图标控件 | `/fx-svg/file/file-7z.svg` |  |
| FileAddSVGGlyph | 新增文件 SVG 图标控件 | `/fx-svg/file/file-add.svg` |  |
| FileLinkSVGGlyph | 文件链接 SVG 图标控件 | `/fx-svg/file/file-symlink-file.svg` |  |
| FileSVGGlyph | 文件 SVG 图标控件 | `/fx-svg/file/file.svg` |  |
| FileUploadSVGGlyph | 文件上传 SVG 图标控件 | `/fx-svg/file/file-upload.svg` |  |
| FolderAddSVGGlyph | 新增文件夹 SVG 图标控件 | `/fx-svg/file/folder-add.svg` |  |
| FolderLinkSVGGlyph | 文件夹链接 SVG 图标控件 | `/fx-svg/file/file-symlink-directory.svg` |  |
| FolderSVGGlyph | 文件夹 SVG 图标控件 | `/fx-svg/file/folder.svg` |  |
| FolderUploadSVGGlyph | 文件夹上传 SVG 图标控件 | `/fx-svg/file/folder-upload.svg` |  |

### 文件图标 - A 系列（file/a）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileAacSVGGlyph | AAC 音频文件 SVG 图标控件 | `/fx-svg/file/a/file-aac.svg` |  |
| FileActionScriptSVGGlyph | ActionScript 源文件 SVG 图标控件 | `/fx-svg/file/a/file-actionscript.svg` |  |
| FileAmrSVGGlyph | AMR 音频文件 SVG 图标控件 | `/fx-svg/file/a/file-amr.svg` |  |
| FileApkSVGGlyph | APK 安装包 SVG 图标控件 | `/fx-svg/file/a/file-apk.svg` |  |
| FileAsciidoctorSVGGlyph | AsciiDoc 文档 SVG 图标控件 | `/fx-svg/file/a/file-asciidoctor.svg` |  |
| FileAsmSVGGlyph | 汇编源文件 SVG 图标控件 | `/fx-svg/file/a/file-asm.svg` |  |
| FileAspSVGGlyph | ASP 文件 SVG 图标控件 | `/fx-svg/file/a/file-asp.svg` |  |
| FileAspxSVGGlyph | ASPX 文件 SVG 图标控件 | `/fx-svg/file/a/file-aspx.svg` |  |

### 文件图标 - B 系列（file/b）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileBatSVGGlyph | Windows 批处理文件 SVG 图标控件 | `/fx-svg/file/b/file-bat.svg` |  |
| FileBibtexSVGGlyph | BibTeX 文献文件 SVG 图标控件 | `/fx-svg/file/b/file-bibtex.svg` |  |
| FileBinSVGGlyph | 二进制文件 SVG 图标控件 | `/fx-svg/file/b/file-bin.svg` |  |
| FileBmpSVGGlyph | BMP 位图图像文件 SVG 图标控件 | `/fx-svg/file/b/file-bmp.svg` |  |
| FileBz2SVGGlyph | BZ2 压缩文件 SVG 图标控件 | `/fx-svg/file/b/file-bz2.svg` |  |

### 文件图标 - C 系列（file/c）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileCerSVGGlyph | 证书文件 SVG 图标控件 | `/fx-svg/file/c/file-cer.svg` |  |
| FileCfgSVGGlyph | 配置文件 SVG 图标控件 | `/fx-svg/file/c/file-cfg.svg` |  |
| FileChmSVGGlyph | CHM 帮助文档 SVG 图标控件 | `/fx-svg/file/c/file-chm.svg` |  |
| FileClassSVGGlyph | Java 字节码文件 SVG 图标控件 | `/fx-svg/file/c/file-class.svg` |  |
| FileClojureSVGGlyph | Clojure 源文件 SVG 图标控件 | `/fx-svg/file/c/file-clojure.svg` |  |
| FileCmdSVGGlyph | Windows 命令脚本文件 SVG 图标控件 | `/fx-svg/file/c/file-cmd.svg` |  |
| FileCoffeeScriptSVGGlyph | CoffeeScript 源文件 SVG 图标控件 | `/fx-svg/file/c/file-coffeescript.svg` |  |
| FileCompressSVGGlyph | 压缩文件 SVG 图标控件 | `/fx-svg/file/c/file-compress.svg` |  |
| FileComSVGGlyph | COM 可执行文件 SVG 图标控件 | `/fx-svg/file/c/file-com.svg` |  |
| FileConfigSVGGlyph | 配置文件 SVG 图标控件 | `/fx-svg/file/c/file-config.svg` |  |
| FileConfSVGGlyph | 配置文件 SVG 图标控件 | `/fx-svg/file/c/file-conf.svg` |  |
| FileCppSVGGlyph | C++ 源文件 SVG 图标控件 | `/fx-svg/file/c/file-cpp.svg` |  |
| FileCssSVGGlyph | CSS 样式文件 SVG 图标控件 | `/fx-svg/file/c/file-css.svg` |  |
| FileCsSVGGlyph | C# 源文件 SVG 图标控件 | `/fx-svg/file/c/file-cs.svg` |  |
| FileCSVGGlyph | CSV 文件 SVG 图标控件 | `/fx-svg/file/c/file-c.svg` |  |
| FileCudaSVGGlyph | CUDA 源文件 SVG 图标控件 | `/fx-svg/file/c/file-cuda.svg` |  |

### 文件图标 - D 系列（file/d）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileDartSVGGlyph | Dart 源文件 SVG 图标控件 | `/fx-svg/file/d/file-dart.svg` |  |
| FileDbSVGGlyph | 数据库文件 SVG 图标控件 | `/fx-svg/file/d/file-db.svg` |  |
| FileDiffSVGGlyph | 差异文件 SVG 图标控件 | `/fx-svg/file/d/file-diff.svg` |  |
| FileDllSVGGlyph | 动态链接库文件 SVG 图标控件 | `/fx-svg/file/d/file-dll.svg` |  |
| FileDmgSVGGlyph | macOS 磁盘映像文件 SVG 图标控件 | `/fx-svg/file/d/file-dmg.svg` |  |
| FileDockerfileSVGGlyph | Dockerfile 文件 SVG 图标控件 | `/fx-svg/file/d/file-dockerfile.svg` |  |
| FileDotSVGGlyph | Graphviz DOT 文件 SVG 图标控件 | `/fx-svg/file/d/file-dot.svg` |  |
| FileDsstoreSVGGlyph | .DS_Store 文件 SVG 图标控件 | `/fx-svg/file/d/file-ds_store.svg` |  |
| FileDylibSVGGlyph | macOS 动态库文件 SVG 图标控件 | `/fx-svg/file/d/file-dylib.svg` |  |

### 文件图标 - E 系列（file/e）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileEpubSVGGlyph | EPUB 电子书 SVG 图标控件 | `/fx-svg/file/e/file-epub.svg` |  |
| FileErlangSVGGlyph | Erlang 源文件 SVG 图标控件 | `/fx-svg/file/e/file-erlang.svg` |  |
| FileExcelSVGGlyph | Excel 表格文件 SVG 图标控件 | `/fx-svg/file/e/file-excel.svg` |  |
| FileExeSVGGlyph | Windows 可执行文件 SVG 图标控件 | `/fx-svg/file/e/file-exe.svg` |  |

### 文件图标 - F 系列（file/f）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileFlacSVGGlyph | FLAC 音频文件 SVG 图标控件 | `/fx-svg/file/f/file-flac.svg` |  |
| FileFlvSVGGlyph | FLV 视频文件 SVG 图标控件 | `/fx-svg/file/f/file-flv.svg` |  |
| FileFsharpSVGGlyph | F# 源文件 SVG 图标控件 | `/fx-svg/file/f/file-fsharp.svg` |  |

### 文件图标 - G 系列（file/g）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileGifSVGGlyph | GIF 图像文件 SVG 图标控件 | `/fx-svg/file/g/file-gif.svg` |  |
| FileGitIgnoreSVGGlyph | Git 忽略配置文件 SVG 图标控件 | `/fx-svg/file/g/file-gitignore.svg` |  |
| FileGitRebaseSVGGlyph | Git Rebase 配置文件 SVG 图标控件 | `/fx-svg/file/g/file-git-rebase.svg` |  |
| FileGoSVGGlyph | Go 源文件 SVG 图标控件 | `/fx-svg/file/g/file-go.svg` |  |
| FileGradleSVGGlyph | Gradle 构建文件 SVG 图标控件 | `/fx-svg/file/g/file-gradle.svg` |  |
| FileGroovySVGGlyph | Groovy 源文件 SVG 图标控件 | `/fx-svg/file/g/file-groovy.svg` |  |
| FileGzSVGGlyph | GZIP 压缩文件 SVG 图标控件 | `/fx-svg/file/g/file-gzip.svg` |  |

### 文件图标 - H 系列（file/h）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileHandlebarsSVGGlyph | Handlebars 模板文件 SVG 图标控件 | `/fx-svg/file/h/file-handlebars.svg` |  |
| FileHlslSVGGlyph | HLSL 着色器源文件 SVG 图标控件 | `/fx-svg/file/h/file-hlsl.svg` |  |
| FileHtmlSVGGlyph | HTML 网页文件 SVG 图标控件 | `/fx-svg/file/h/file-html.svg` |  |

### 文件图标 - I 系列（file/i）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileIcnsSVGGlyph | macOS 图标文件 SVG 图标控件 | `/fx-svg/file/i/file-icns.svg` |  |
| FileIcoSVGGlyph | 图标文件 SVG 图标控件 | `/fx-svg/file/i/file-ico.svg` |  |
| FileImageSVGGlyph | 图像文件 SVG 图标控件 | `/fx-svg/file/i/file-image.svg` |  |
| FileInfSVGGlyph | 安装信息文件 SVG 图标控件 | `/fx-svg/file/i/file-inf.svg` |  |
| FileIniSVGGlyph | INI 配置文件 SVG 图标控件 | `/fx-svg/file/i/file-ini.svg` |  |
| FileIsoSVGGlyph | ISO 光盘映像文件 SVG 图标控件 | `/fx-svg/file/i/file-iso.svg` |  |

### 文件图标 - J 系列（file/j）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileJarSVGGlyph | Java 归档文件 SVG 图标控件 | `/fx-svg/file/j/file-jar.svg` |  |
| FileJavaSVGGlyph | Java 源文件 SVG 图标控件 | `/fx-svg/file/j/file-java.svg` |  |
| FileJpgSVGGlyph | JPEG 图像文件 SVG 图标控件 | `/fx-svg/file/j/file-jpg.svg` |  |
| FileJsonSVGGlyph | JSON 数据文件 SVG 图标控件 | `/fx-svg/file/j/file-json.svg` |  |
| FileJspSVGGlyph | JSP 页面文件 SVG 图标控件 | `/fx-svg/file/j/file-jsp.svg` |  |
| FileJsSVGGlyph | JavaScript 源文件 SVG 图标控件 | `/fx-svg/file/j/file-js.svg` |  |
| FileJuliaSVGGlyph | Julia 源文件 SVG 图标控件 | `/fx-svg/file/j/file-julia.svg` |  |

### 文件图标 - K 系列（file/k）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileKconfigSVGGlyph | Kconfig 配置文件 SVG 图标控件 | `/fx-svg/file/k/file-kconfig.svg` |  |
| FileKmkSVGGlyph | 内核模块构建文件 SVG 图标控件 | `/fx-svg/file/k/file-kmk.svg` |  |
| FileKtSVGGlyph | Kotlin 源文件 SVG 图标控件 | `/fx-svg/file/k/file-kt.svg` |  |

### 文件图标 - L 系列（file/l）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileLatexSVGGlyph | LaTeX 文档 SVG 图标控件 | `/fx-svg/file/l/file-latex.svg` |  |
| FileLessSVGGlyph | LESS 样式文件 SVG 图标控件 | `/fx-svg/file/l/file-less.svg` |  |
| FileLuaSVGGlyph | Lua 源文件 SVG 图标控件 | `/fx-svg/file/l/file-lua.svg` |  |

### 文件图标 - M 系列（file/m）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileM4aSVGGlyph | M4A 音频文件 SVG 图标控件 | `/fx-svg/file/m/file-m4a.svg` |  |
| FileMakefileSVGGlyph | Makefile 构建文件 SVG 图标控件 | `/fx-svg/file/m/file-makefile.svg` |  |
| FileMarkdownSVGGlyph | Markdown 文档 SVG 图标控件 | `/fx-svg/file/m/file-markdown.svg` |  |
| FileMkvSVGGlyph | MKV 视频文件 SVG 图标控件 | `/fx-svg/file/m/file-mkv.svg` |  |
| FileMovSVGGlyph | MOV 视频文件 SVG 图标控件 | `/fx-svg/file/m/file-mov.svg` |  |
| FileMp3SVGGlyph | MP3 音频文件 SVG 图标控件 | `/fx-svg/file/m/file-mp3.svg` |  |
| FileMp4SVGGlyph | MP4 视频文件 SVG 图标控件 | `/fx-svg/file/m/file-mp4.svg` |  |

### 文件图标 - O 系列（file/o）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileObjectiveCPPSVGGlyph | Objective-C++ 源文件 SVG 图标控件 | `/fx-svg/file/o/file-objective-c.svg` |  |
| FileObjectiveCSVGGlyph | Objective-C 源文件 SVG 图标控件 | `/fx-svg/file/o/file-objective-cpp.svg` |  |
| FileOcxSVGGlyph | OCX 控件文件 SVG 图标控件 | `/fx-svg/file/o/file-ocx.svg` |  |
| FileOggSVGGlyph | OGG 音频文件 SVG 图标控件 | `/fx-svg/file/o/file-ogg.svg` |  |

### 文件图标 - P 系列（file/p）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FilePcmSVGGlyph | PCM 音频文件 SVG 图标控件 | `/fx-svg/file/p/file-pcm.svg` |  |
| FilePdfSVGGlyph | PDF 文档 SVG 图标控件 | `/fx-svg/file/p/file-pdf.svg` |  |
| FilePerlSVGGlyph | Perl 源文件 SVG 图标控件 | `/fx-svg/file/p/file-perl.svg` |  |
| FilePhpSVGGlyph | PHP 源文件 SVG 图标控件 | `/fx-svg/file/p/file-php.svg` |  |
| FilePlistSVGGlyph | PLIST 配置文件 SVG 图标控件 | `/fx-svg/file/p/file-plist.svg` |  |
| FilePowershellSVGGlyph | PowerShell 脚本文件 SVG 图标控件 | `/fx-svg/file/p/file-powershell.svg` |  |
| FilePptSVGGlyph | PowerPoint 演示文稿 SVG 图标控件 | `/fx-svg/file/p/file-ppt.svg` |  |
| FilePropertiesSVGGlyph | Properties 配置文件 SVG 图标控件 | `/fx-svg/file/p/file-properties.svg` |  |
| FileProtobufSVGGlyph | Protocol Buffers 文件 SVG 图标控件 | `/fx-svg/file/p/file-protobuf.svg` |  |
| FilePsdSVGGlyph | PSD 图像文件 SVG 图标控件 | `/fx-svg/file/p/file-psd.svg` |  |
| FilePugSVGGlyph | Pug 模板文件 SVG 图标控件 | `/fx-svg/file/p/file-pug.svg` |  |
| FilePycSVGGlyph | Python 字节码文件 SVG 图标控件 | `/fx-svg/file/p/file-pyc.svg` |  |
| FilePySVGGlyph | Python 源文件 SVG 图标控件 | `/fx-svg/file/p/file-py.svg` |  |

### 文件图标 - R 系列（file/r）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileRarSVGGlyph | RAR 压缩文件 SVG 图标控件 | `/fx-svg/file/r/file-rar.svg` |  |
| FileRazorSVGGlyph | Razor 模板文件 SVG 图标控件 | `/fx-svg/file/r/file-razor.svg` |  |
| FileRestructuredtextSVGGlyph | reStructuredText 文档 SVG 图标控件 | `/fx-svg/file/r/file-restructuredtext.svg` |  |
| FileRmSVGGlyph | RM 视频文件 SVG 图标控件 | `/fx-svg/file/r/file-rm.svg` |  |
| FileRmvbSVGGlyph | RMVB 视频文件 SVG 图标控件 | `/fx-svg/file/r/file-rmvb.svg` |  |
| FileRpmSVGGlyph | RPM 安装包 SVG 图标控件 | `/fx-svg/file/r/file-rpm.svg` |  |
| FileRssSVGGlyph | RSS 订阅文件 SVG 图标控件 | `/fx-svg/file/r/file-rss.svg` |  |
| FileRSVGGlyph | R 源文件 SVG 图标控件 | `/fx-svg/file/r/file-r.svg` |  |
| FileRtfSVGGlyph | RTF 文档 SVG 图标控件 | `/fx-svg/file/r/file-rtf.svg` |  |
| FileRubySVGGlyph | Ruby 源文件 SVG 图标控件 | `/fx-svg/file/r/file-ruby.svg` |  |
| FileRustSVGGlyph | Rust 源文件 SVG 图标控件 | `/fx-svg/file/r/file-rust.svg` |  |

### 文件图标 - S 系列（file/s）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileSasSVGGlyph | SAS 源文件 SVG 图标控件 | `/fx-svg/file/s/file-sas.svg` |  |
| FileScalaSVGGlyph | Scala 源文件 SVG 图标控件 | `/fx-svg/file/s/file-scala.svg` |  |
| FileScssSVGGlyph | SCSS 样式文件 SVG 图标控件 | `/fx-svg/file/s/file-scss.svg` |  |
| FileSearchResultSVGGlyph | 搜索结果 SVG 图标控件 | `/fx-svg/file/s/file-search-result.svg` |  |
| FileShaderlabSVGGlyph | ShaderLab 着色器文件 SVG 图标控件 | `/fx-svg/file/s/file-shaderlab.svg` |  |
| FileShSVGGlyph | Shell 脚本文件 SVG 图标控件 | `/fx-svg/file/s/file-sh.svg` |  |
| FileSoSVGGlyph | 共享库文件 SVG 图标控件 | `/fx-svg/file/s/file-so.svg` |  |
| FileSqlSVGGlyph | SQL 脚本文件 SVG 图标控件 | `/fx-svg/file/s/file-sql.svg` |  |
| FileSrtSVGGlyph | SRT 字幕文件 SVG 图标控件 | `/fx-svg/file/s/file-srt.svg` |  |
| FileSvgSVGGlyph | SVG 矢量图文件 SVG 图标控件 | `/fx-svg/file/s/file-svg.svg` |  |
| FileSwfSVGGlyph | SWF 动画文件 SVG 图标控件 | `/fx-svg/file/s/file-swf.svg` |  |
| FileSwiftSVGGlyph | Swift 源文件 SVG 图标控件 | `/fx-svg/file/s/file-swift.svg` |  |

### 文件图标 - T 系列（file/t）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileTarSVGGlyph | TAR 归档文件 SVG 图标控件 | `/fx-svg/file/t/file-tar.svg` |  |
| FileTerminalSVGGlyph | 终端文件 SVG 图标控件 | `/fx-svg/file/t/file-terminal.svg` |  |
| FileTexSVGGlyph | TeX 文档 SVG 图标控件 | `/fx-svg/file/t/file-tex.svg` |  |
| FileTextSVGGlyph | 文本文件 SVG 图标控件 | `/fx-svg/file/t/file-text.svg` |  |
| FileTomlSVGGlyph | TOML 配置文件 SVG 图标控件 | `/fx-svg/file/t/file-toml.svg` |  |
| FileTsSVGGlyph | TypeScript 源文件 SVG 图标控件 | `/fx-svg/file/t/file-ts.svg` |  |
| FileTsxSVGGlyph | TypeScript JSX 源文件 SVG 图标控件 | `/fx-svg/file/t/file-tsx.svg` |  |
| FileTtfSVGGlyph | TTF 字体文件 SVG 图标控件 | `/fx-svg/file/t/file-ttf.svg` |  |
| FileTwigSVGGlyph | Twig 模板文件 SVG 图标控件 | `/fx-svg/file/t/file-twig.svg` |  |

### 文件图标 - U 系列（file/u）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileUnknownSVGGlyph | 未知类型文件 SVG 图标控件 | `/fx-svg/file/u/file-unknown.svg` |  |

### 文件图标 - V 系列（file/v）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileVbsSVGGlyph | VBScript 脚本文件 SVG 图标控件 | `/fx-svg/file/v/file-vbs.svg` |  |
| FileVbSVGGlyph | Visual Basic 源文件 SVG 图标控件 | `/fx-svg/file/v/file-vb.svg` |  |
| FileVimSVGGlyph | Vim 脚本文件 SVG 图标控件 | `/fx-svg/file/v/file-vim.svg` |  |

### 文件图标 - W 系列（file/w）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileWarSVGGlyph | Java Web 归档文件 SVG 图标控件 | `/fx-svg/file/w/file-war.svg` |  |
| FileWavSVGGlyph | WAV 音频文件 SVG 图标控件 | `/fx-svg/file/w/file-wav.svg` |  |
| FileWbmpSVGGlyph | WBMP 图像文件 SVG 图标控件 | `/fx-svg/file/w/file-wbmp.svg` |  |
| FileWebmSVGGlyph | WebM 视频文件 SVG 图标控件 | `/fx-svg/file/w/file-webm.svg` |  |
| FileWebpSVGGlyph | WebP 图像文件 SVG 图标控件 | `/fx-svg/file/w/file-webp.svg` |  |
| FileWmaSVGGlyph | WMA 音频文件 SVG 图标控件 | `/fx-svg/file/w/file-wma.svg` |  |
| FileWordSVGGlyph | Word 文档 SVG 图标控件 | `/fx-svg/file/w/file-word.svg` |  |
| FileWpsSVGGlyph | WPS 文档 SVG 图标控件 | `/fx-svg/file/w/file-wps.svg` |  |

### 文件图标 - X 系列（file/x）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileXlsSVGGlyph | Excel 表格文件 SVG 图标控件 | `/fx-svg/file/x/file-xls.svg` |  |
| FileXmlSVGGlyph | XML 数据文件 SVG 图标控件 | `/fx-svg/file/x/file-xml.svg` |  |
| FileXslSVGGlyph | XSL 样式表文件 SVG 图标控件 | `/fx-svg/file/x/file-xsl.svg` |  |
| FileXzSVGGlyph | XZ 压缩文件 SVG 图标控件 | `/fx-svg/file/x/file-xz.svg` |  |

### 文件图标 - Y 系列（file/y）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileYamlSVGGlyph | YAML 配置文件 SVG 图标控件 | `/fx-svg/file/y/file-yaml.svg` |  |
| FileYmlSVGGlyph | YAML 配置文件 SVG 图标控件 | `/fx-svg/file/y/file-yml.svg` |  |

### 文件图标 - Z 系列（file/z）

| 类名 | 职责 | 图标路径 | 缩放/备注 |
|---|---|---|---|
| FileZipSVGGlyph | ZIP 压缩文件 SVG 图标控件 | `/fx-svg/file/z/file-zip.svg` |  |

---

- 合计覆盖 SVG 图标类：**337** 个。

# fx-plus 校验（validator）

覆盖 `cn.oyzh.fx.plus.validator` 包。经核查，该包绝大多数类被整体注释（死代码），**仅 2 个类为正式代码**：`ValidatorUtil`、`Verifiable`。以下为已注释（不纳入）的类清单，供参考：`Validator`、`BaseValidator`、`BaseVerifier`、`Verifier`、`VerifyFailHandler`、`TipVerifyFailHandler`、`MaxLenVerifier`、`MinLenVerifier`、`MaxValVerifier`、`MinValVerifier`、`DecimalVerifier`、`NumberVerifier`、`RequiredVerifier`。

## ValidatorUtil
- 职责：校验工具类，校验失败时给组件加红色边框并请求焦点。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `validFail(EventTarget)` | 失败提示 | 委托 `validFail(target, 1500)` |
  | `validFail(EventTarget,int)` | 失败提示（可延时恢复） | 对 `Region`：记录原边框（含圆角），设红色实线边框，`requestFocus`，delay>0 时 `FXUtil.runLater` 恢复原边框 |
- 调用链：`FXComboBox.validate / FXTextField.validate → ValidatorUtil.validFail`

---

## Verifiable
- 职责：校验能力接口，供控件实现以暴露统一的 `validate()` 方法。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `validate()` | 执行校验 | 默认返回 `true`，由控件覆盖（如 `FXComboBox`/`FXTextField`/`FXTextArea` 在必填为空时调用 `ValidatorUtil.validFail` 并返回 false） |
- 调用链：`FXComboBox.validate → ValidatorUtil.validFail`；`FXTextField.validate → ValidatorUtil.validFail`

---

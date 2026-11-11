#!/usr/bin/env bash
#
# 根据 Ghostty 头文件重新生成 FFM 绑定（jextract 25）。
#
# 用法：
#   GHOSTTY_SRC=/path/to/ghostty \
#   JEXTRACT=/path/to/jextract-25/bin/jextract \
#   ./fx-tty/tools/generate-bindings.sh
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_PACKAGE="cn.oyzh.fx.tty.ghostty.bindings"
TARGET_DIR="$ROOT/src/main/java/cn/oyzh/fx/tty/ghostty/bindings"

[[ -n "${GHOSTTY_SRC:-}" ]] || { echo "error: 请设置 GHOSTTY_SRC" >&2; exit 1; }
[[ -x "${JEXTRACT:-}" ]] || { echo "error: 请设置 JEXTRACT 指向 jextract 25 可执行文件" >&2; exit 1; }

HEADER="$GHOSTTY_SRC/include/ghostty/vt.h"
INCLUDE="$GHOSTTY_SRC/include"
[[ -f "$HEADER" ]] || { echo "error: 找不到 $HEADER" >&2; exit 1; }

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

echo "==> jextract: $HEADER"
"$JEXTRACT" \
  -I "$INCLUDE" \
  -t "$TARGET_PACKAGE" \
  --header-class-name ghostty_vt_h \
  --output "$TMP" \
  "$HEADER"

GENERATED="$TMP/cn/oyzh/fx/tty/ghostty/bindings"
[[ -d "$GENERATED" ]] || { echo "error: jextract 未生成预期目录" >&2; exit 1; }

rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
cp "$GENERATED"/*.java "$TARGET_DIR/"

echo "==> 已生成 $(find "$TARGET_DIR" -name '*.java' | wc -l | tr -d ' ') 个绑定文件到 $TARGET_DIR"
echo "    提示：6 个平台的 C 结构体布局已验证一致，同一套绑定可用于所有平台。"

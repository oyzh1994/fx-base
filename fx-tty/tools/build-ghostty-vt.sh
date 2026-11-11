#!/usr/bin/env bash
#
# 从 Ghostty 源码交叉编译 libghostty-vt，并复制到 fx-tty 的资源目录。
#
# 用法：
#   GHOSTTY_SRC=/path/to/ghostty ./fx-tty/tools/build-ghostty-vt.sh
#
# 可选环境变量：
#   GHOSTTY_SRC         Ghostty 源码目录（必须包含 build.zig，建议使用提交 c3203ea4）
#   ZIG                 Zig 0.16.0 可执行文件路径；未设置时自动下载
#   GHOSTTY_BUILD_DIR   构建工作目录，默认 fx-tty/target/ghostty-build
#   DEST_DIR            产物输出目录，默认 fx-tty/src/main/resources/ghostty
#   TARGETS             只构建指定平台，空格分隔，例如 "macos-aarch64 windows-aarch64"
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESOURCES="${DEST_DIR:-$ROOT/src/main/resources/ghostty}"
WORK="${GHOSTTY_BUILD_DIR:-$ROOT/target/ghostty-build}"
ZIG_VERSION="0.16.0"

if [[ -z "${GHOSTTY_SRC:-}" ]]; then
  echo "error: 请设置 GHOSTTY_SRC 指向 Ghostty 源码目录" >&2
  exit 1
fi
GHOSTTY_SRC="$(cd "$GHOSTTY_SRC" && pwd)"
[[ -f "$GHOSTTY_SRC/build.zig" ]] || {
  echo "error: $GHOSTTY_SRC 中找不到 build.zig" >&2
  exit 1
}

mkdir -p "$WORK/tools" "$WORK/out" "$WORK/logs"

host_os="$(uname -s)"
host_arch="$(uname -m)"
case "$host_os" in
  Darwin) zig_os="macos" ;;
  Linux)  zig_os="linux" ;;
  *)
    echo "error: 当前脚本只能在 macOS / Linux 上运行（Windows 可用 WSL/Git Bash）" >&2
    exit 1
    ;;
esac
case "$host_arch" in
  arm64|aarch64) zig_arch="aarch64" ;;
  x86_64|amd64)  zig_arch="x86_64" ;;
  *)
    echo "error: 不支持的主机架构: $host_arch" >&2
    exit 1
    ;;
esac

if [[ -z "${ZIG:-}" ]]; then
  zig_dir="$WORK/tools/zig-$zig_arch-$zig_os-$ZIG_VERSION"
  if [[ ! -x "$zig_dir/zig" ]]; then
    archive="$WORK/tools/zig-$zig_arch-$zig_os-$ZIG_VERSION.tar.xz"
    if [[ ! -f "$archive" ]]; then
      echo "==> 下载 Zig $ZIG_VERSION ($zig_arch-$zig_os)"
      curl -fL --retry 3 -o "$archive" \
        "https://ziglang.org/download/$ZIG_VERSION/zig-$zig_arch-$zig_os-$ZIG_VERSION.tar.xz"
    fi
    tar -xf "$archive" -C "$WORK/tools"
  fi
  ZIG="$zig_dir/zig"
fi
"$ZIG" version >/dev/null || {
  echo "error: Zig 不可用: $ZIG" >&2
  exit 1
}

targets=(
  "linux-x86_64|x86_64-linux-gnu|lib/libghostty-vt.so.0.1.0|so"
  "linux-aarch64|aarch64-linux-gnu|lib/libghostty-vt.so.0.1.0|so"
  "macos-x86_64|x86_64-macos|lib/libghostty-vt.0.1.0.dylib|dylib"
  "macos-aarch64|aarch64-macos|lib/libghostty-vt.0.1.0.dylib|dylib"
  "windows-x86_64|x86_64-windows-gnu|bin/ghostty-vt.dll|dll"
  "windows-aarch64|aarch64-windows-gnu|bin/ghostty-vt.dll|dll"
)

selected="${TARGETS:-}"
for entry in "${targets[@]}"; do
  IFS='|' read -r id triple library ext <<< "$entry"
  if [[ -n "$selected" && " $selected " != *" $id "* ]]; then
    continue
  fi
  echo "==> 构建 $id ($triple)"
  if ! ( cd "$GHOSTTY_SRC" && \
      ZIG_LOCAL_CACHE_DIR="$WORK/zig-cache-$id" \
      ZIG_GLOBAL_CACHE_DIR="$WORK/zig-global" \
      "$ZIG" build \
        -Dtarget="$triple" \
        -Doptimize=ReleaseFast \
        -Dapp-runtime=none \
        -Demit-lib-vt=true \
        -Demit-xcframework=false \
        -Dvt-features=-snapshot,-color,-glyph-protocol \
        --prefix "$WORK/out/$id" ) > "$WORK/logs/$id.log" 2>&1; then
    echo "error: $id 构建失败，日志: $WORK/logs/$id.log" >&2
    tail -20 "$WORK/logs/$id.log" >&2
    exit 1
  fi
  source_library="$WORK/out/$id/$library"
  [[ -f "$source_library" ]] || {
    echo "error: 未生成预期产物: $source_library" >&2
    exit 1
  }
  mkdir -p "$RESOURCES/$id"
  destination="$RESOURCES/$id/libghostty-vt-$id.$ext"
  cp "$source_library" "$destination"
  chmod 755 "$destination"
  echo "    -> $destination"
done

echo "==> 完成。资源目录: $RESOURCES"
shasum -a 256 "$RESOURCES"/*/libghostty-vt-* 2>/dev/null || true

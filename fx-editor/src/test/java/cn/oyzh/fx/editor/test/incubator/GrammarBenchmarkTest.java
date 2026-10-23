package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import org.junit.Test;
import tm4java.grammar.IGrammar;
import tm4java.grammar.IGrammarSource;
import tm4java.grammar.IStateStack;
import tm4java.grammar.IToken;
import tm4java.grammar.ITokenizeLineResult;
import tm4java.registry.Registry;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * TextMate 语法配置基准测试。
 * <p>
 * 对每个 {@link EditorFormatType} 测量：
 * <ul>
 *     <li>语法加载时间（解析 JSON/PList 并注册）</li>
 *     <li>首次分词时间（包含规则懒编译）</li>
 *     <li>稳定分词吞吐（行/秒，通用语料 + 语言样例）</li>
 * </ul>
 * 结果输出到控制台，并写入 {@code target/grammar-benchmark.csv}。
 *
 * @author oyzh
 * @since 2026-10-23
 */
public class GrammarBenchmarkTest {

    /**
     * 语法加载次数，取中位数
     */
    private static final int LOAD_ROUNDS = 3;

    /**
     * 分词吞吐测量预算（毫秒）
     */
    private static final long TOKENIZE_BUDGET_MS = 100;

    /**
     * 分词吞吐最少测量轮数
     */
    private static final int MIN_PASSES = 10;

    /**
     * 通用语料，用于横向比较所有语法的扫描成本
     */
    private static final List<String> GENERIC_CORPUS = List.of(
            "// line comment and # hash and -- dash and ! bang",
            "/* block comment */ { ( [ < > ] ) }",
            "const value = 123.45e-6; var_name = 'text' + \"escaped\\n\";",
            "function call(arg1, arg2) { return arg1 && arg2 || !arg1; }",
            "SELECT id, name FROM users WHERE id = 42 AND name = 'alice';",
            "<tag attribute=\"value\" data-id='1'>content &amp; text</tag>",
            "@decorator(option: true, flag=false) # trailing",
            "0xFF 0b1010 0o755 1_000_000 3.14 1.0d0",
            "if (a <= b && c != d) { x += 1; } else { y -= 2; }"
    );

    /**
     * 新补充语法的语言样例
     */
    private static final Map<EditorFormatType, List<String>> SAMPLES = new EnumMap<>(EditorFormatType.class);

    static {
        SAMPLES.put(EditorFormatType.GRAPHQL, List.of(
                "# query comment",
                "query User($id: ID!, $first: Int = 10) {",
                "  user(id: $id) { id name email friends(first: $first) { nodes { name } } }",
                "}",
                "type User implements Node @auth(requires: ADMIN) { id: ID! name: String }"
        ));
        SAMPLES.put(EditorFormatType.NGINX, List.of(
                "# nginx comment",
                "worker_processes auto;",
                "http {",
                "  server { listen 80; server_name example.com;",
                "    location /api/ { proxy_pass http://127.0.0.1:8080; proxy_set_header Host $host; }",
                "    access_log /var/log/nginx/access.log;",
                "  }",
                "}"
        ));
        SAMPLES.put(EditorFormatType.EDITORCONFIG, List.of(
                "# editorconfig comment",
                "root = true",
                "[*.java]",
                "indent_style = space",
                "indent_size = 4",
                "end_of_line = lf",
                "charset = utf-8"
        ));
        SAMPLES.put(EditorFormatType.DOTENV, List.of(
                "# dotenv comment",
                "APP_ENV=production",
                "SECRET_KEY=\"change me\"",
                "PORT=8080",
                "export DATABASE_URL='postgres://user:pass@localhost/db'"
        ));
        SAMPLES.put(EditorFormatType.HTTP, List.of(
                "### get user",
                "GET /api/users/42 HTTP/1.1",
                "Host: example.com",
                "Authorization: Bearer abc.def.ghi",
                "Accept: application/json",
                "",
                "HTTP/1.1 200 OK",
                "Content-Type: application/json"
        ));
        SAMPLES.put(EditorFormatType.HASKELL, List.of(
                "-- haskell comment",
                "{- block comment -}",
                "module Main where",
                "import qualified Data.Map as M",
                "data Tree a = Leaf | Node (Tree a) a (Tree a) deriving (Show, Eq)",
                "sumTree :: Tree Int -> Int",
                "sumTree Leaf = 0",
                "sumTree (Node l x r) = sumTree l + x + sumTree r"
        ));
        SAMPLES.put(EditorFormatType.ELIXIR, List.of(
                "# elixir comment",
                "defmodule Demo.User do",
                "  @moduledoc \"\"\"User module\"\"\"",
                "  defstruct [:id, :name, active?: true]",
                "  def greet(%__MODULE__{name: name}) when is_binary(name), do: \"Hello, #{name}\"",
                "  def sum(list), do: Enum.reduce(list, 0, &(&1 + &2))",
                "end"
        ));
        SAMPLES.put(EditorFormatType.OCAML, List.of(
                "(* ocaml comment *)",
                "type 'a tree = Leaf | Node of 'a tree * 'a * 'a tree",
                "let rec sum = function",
                "  | Leaf -> 0",
                "  | Node (l, x, r) -> sum l + x + sum r",
                "let () = Printf.printf \"%d\\n\" (sum (Node (Leaf, 1, Leaf)))"
        ));
        SAMPLES.put(EditorFormatType.SOLIDITY, List.of(
                "// SPDX-License-Identifier: MIT",
                "pragma solidity ^0.8.20;",
                "contract Token {",
                "  mapping(address => uint256) public balanceOf;",
                "  event Transfer(address indexed from, address indexed to, uint256 value);",
                "  function transfer(address to, uint256 value) external returns (bool) {",
                "    require(balanceOf[msg.sender] >= value, \"insufficient balance\");",
                "    balanceOf[msg.sender] -= value; balanceOf[to] += value;",
                "    emit Transfer(msg.sender, to, value); return true;",
                "  }",
                "}"
        ));
        SAMPLES.put(EditorFormatType.ZIG, List.of(
                "// zig comment",
                "const std = @import(\"std\");",
                "pub fn main() !void {",
                "    var list = std.ArrayList(u8).init(std.heap.page_allocator);",
                "    defer list.deinit();",
                "    try list.appendSlice(\"hello\");",
                "    std.debug.print(\"{s}\\n\", .{list.items});",
                "}"
        ));
        SAMPLES.put(EditorFormatType.NIX, List.of(
                "# nix comment",
                "{ pkgs ? import <nixpkgs> { } }:",
                "let",
                "  name = \"demo\";",
                "  version = \"1.0.0\";",
                "in pkgs.stdenv.mkDerivation {",
                "  inherit name version;",
                "  src = ./.;",
                "  buildInputs = [ pkgs.openssl pkgs.zlib ];",
                "  meta.description = ''A demo package'';",
                "}"
        ));
        SAMPLES.put(EditorFormatType.TCL, List.of(
                "# tcl comment",
                "proc greet {name} {",
                "    set message \"Hello, $name\"",
                "    puts $message",
                "    return $message",
                "}",
                "if {[string length [greet world]] > 0} { puts ok }"
        ));
        SAMPLES.put(EditorFormatType.FORTRAN, List.of(
                "! fortran comment",
                "program hello",
                "  implicit none",
                "  integer :: i, total",
                "  real(kind=8) :: x = 1.0d0",
                "  total = 0",
                "  do i = 1, 10",
                "    total = total + i",
                "  end do",
                "  write(*,*) 'total =', total",
                "end program hello"
        ));
        SAMPLES.put(EditorFormatType.PASCAL, List.of(
                "{ pascal comment }",
                "program Hello;",
                "uses SysUtils;",
                "var i: Integer; s: string;",
                "begin",
                "  s := 'hello';",
                "  for i := 1 to 10 do",
                "    WriteLn(s, ' ', i);",
                "end."
        ));
        SAMPLES.put(EditorFormatType.STARLARK, List.of(
                "# starlark comment",
                "load(\"@rules_cc//cc:defs.bzl\", \"cc_library\")",
                "def _impl(ctx):",
                "    return [DefaultInfo(files = depset(ctx.files.srcs))]",
                "cc_library(",
                "    name = \"demo\",",
                "    srcs = glob([\"**/*.cc\"]),",
                "    deps = [\":lib\"],",
                ")"
        ));
        SAMPLES.put(EditorFormatType.WAT, List.of(
                ";; wat comment",
                "(module",
                "  (import \"env\" \"memory\" (memory 1))",
                "  (func $add (param $a i32) (param $b i32) (result i32)",
                "    local.get $a",
                "    local.get $b",
                "    i32.add)",
                "  (export \"add\" (func $add)))"
        ));
        SAMPLES.put(EditorFormatType.REGEXP, List.of(
                "# regexp comment",
                "^(?P<scheme>https?)://([\\w.-]+)(?::(\\d+))?(/.*)?$",
                "\\b(?:foo|bar)\\s*=\\s*[\"']([^\"']*)[\"']",
                "(?<=[a-z])(?=[A-Z])|\\d{4}-\\d{2}-\\d{2}"
        ));
    }

    /**
     * 新增语法集合，用于结果分组统计
     */
    private static final Set<EditorFormatType> NEW_FORMATS = new HashSet<>(SAMPLES.keySet());

    @Test
    public void benchmark() throws Exception {
        List<Result> results = new ArrayList<>();
        int failures = 0;
        long startedAt = System.currentTimeMillis();

        for (EditorFormatType type : EditorFormatType.values()) {
            if (type == EditorFormatType.RAW) {
                continue;
            }
            try {
                Result result = benchmark(type);
                if (result != null) {
                    results.add(result);
                }
            } catch (Throwable ex) {
                failures++;
                System.out.println("FAIL\t" + type.name() + "\t" + ex);
            }
        }

        results.sort(Comparator.comparingDouble(Result::loadMs).reversed());
        writeCsv(results);
        printSummary(results, failures, System.currentTimeMillis() - startedAt);

        assertFalse("存在加载失败的语法", failures > 0);
        assertTrue("未收集到基准结果", !results.isEmpty());
    }

    private Result benchmark(EditorFormatType type) throws Exception {
        String resource = "/tm4javafx/grammars/" + type.getFullSyntaxesName();
        long bytes = resourceBytes(resource);
        if (bytes <= 0) {
            return null;
        }

        // 加载耗时：多次测量取中位数
        double[] loads = new double[LOAD_ROUNDS];
        IGrammar grammar = null;
        for (int i = 0; i < LOAD_ROUNDS; i++) {
            long begin = System.nanoTime();
            Registry registry = new Registry();
            grammar = registry.addGrammar(IGrammarSource.fromResource(EditorFormatType.class, resource));
            loads[i] = millis(begin);
            assertNotNull("语法加载失败: " + type, grammar);
        }
        double loadMs = median(loads);

        // 首次分词：包含规则编译
        List<String> sample = SAMPLES.getOrDefault(type, GENERIC_CORPUS);
        long begin = System.nanoTime();
        int tokens = tokenizeOnce(grammar, sample);
        double firstTokenizeMs = millis(begin);
        assertTrue("未产生token: " + type, tokens > 0);

        // 稳定分词吞吐
        double genericLps = throughput(grammar, GENERIC_CORPUS);
        double sampleLps = sample == GENERIC_CORPUS ? genericLps : throughput(grammar, sample);

        return new Result(type.name(), NEW_FORMATS.contains(type), bytes, loadMs, firstTokenizeMs, genericLps, sampleLps);
    }

    private static int tokenizeOnce(IGrammar grammar, List<String> lines) {
        int tokens = 0;
        IStateStack state = null;
        for (String line : lines) {
            ITokenizeLineResult<IToken[]> result = grammar.tokenizeLine(line, state, null);
            tokens += result.tokens().length;
            state = result.ruleStack();
        }
        return tokens;
    }

    private static double throughput(IGrammar grammar, List<String> lines) {
        int passes = 0;
        long count = 0;
        long begin = System.nanoTime();
        do {
            IStateStack state = null;
            for (String line : lines) {
                ITokenizeLineResult<IToken[]> result = grammar.tokenizeLine(line, state, null);
                count += result.tokens().length;
                state = result.ruleStack();
            }
            passes++;
        } while (passes < MIN_PASSES
                || (System.nanoTime() - begin) < TOKENIZE_BUDGET_MS * 1_000_000L);
        double seconds = Math.max(1e-9, (System.nanoTime() - begin) / 1_000_000_000.0);
        return (passes * (double) lines.size()) / seconds;
    }

    private static long resourceBytes(String resource) throws Exception {
        try (var stream = EditorFormatType.class.getResourceAsStream(resource)) {
            return stream == null ? -1 : stream.readAllBytes().length;
        }
    }

    private static double millis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

    private static double median(double[] values) {
        double[] copy = values.clone();
        java.util.Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static void writeCsv(List<Result> results) throws Exception {
        StringBuilder csv = new StringBuilder("format,new,bytes,load_ms,first_tokenize_ms,generic_lines_per_sec,sample_lines_per_sec\n");
        for (Result r : results) {
            csv.append(r.name()).append(',')
                    .append(r.isNew()).append(',')
                    .append(r.bytes()).append(',')
                    .append(String.format(Locale.ROOT, "%.3f", r.loadMs())).append(',')
                    .append(String.format(Locale.ROOT, "%.3f", r.firstTokenizeMs())).append(',')
                    .append(String.format(Locale.ROOT, "%.0f", r.genericLinesPerSec())).append(',')
                    .append(String.format(Locale.ROOT, "%.0f", r.sampleLinesPerSec())).append('\n');
        }
        Path out = Path.of("target", "grammar-benchmark.csv");
        Files.createDirectories(out.getParent());
        Files.writeString(out, csv.toString(), StandardCharsets.UTF_8);
        System.out.println("BENCH_CSV\t" + out.toAbsolutePath());
    }

    private static void printSummary(List<Result> results, int failures, long elapsedMs) {
        System.out.println("BENCH_SUMMARY\tformats=" + results.size()
                + "\tnew=" + results.stream().filter(Result::isNew).count()
                + "\tfailures=" + failures
                + "\telapsed_ms=" + elapsedMs);

        System.out.println("---- slowest load (top 10) ----");
        results.stream().limit(10)
                .forEach(r -> System.out.printf(Locale.ROOT, "%-28s load=%8.2fms first=%8.2fms size=%7.1fKB%n",
                        r.name(), r.loadMs(), r.firstTokenizeMs(), r.bytes() / 1024.0));

        System.out.println("---- lowest generic throughput (top 10) ----");
        results.stream()
                .sorted(Comparator.comparingDouble(Result::genericLinesPerSec))
                .limit(10)
                .forEach(r -> System.out.printf(Locale.ROOT, "%-28s generic=%10.0f lines/s sample=%10.0f lines/s%n",
                        r.name(), r.genericLinesPerSec(), r.sampleLinesPerSec()));

        System.out.println("---- new formats ----");
        results.stream()
                .filter(Result::isNew)
                .sorted(Comparator.comparing(Result::name))
                .forEach(r -> System.out.printf(Locale.ROOT, "%-16s load=%7.2fms first=%7.2fms generic=%9.0f lines/s sample=%9.0f lines/s size=%6.1fKB%n",
                        r.name(), r.loadMs(), r.firstTokenizeMs(), r.genericLinesPerSec(), r.sampleLinesPerSec(), r.bytes() / 1024.0));

        double totalLoad = results.stream().mapToDouble(Result::loadMs).sum();
        double avgLoad = totalLoad / results.size();
        double totalSize = results.stream().mapToLong(Result::bytes).sum();
        System.out.printf(Locale.ROOT, "BENCH_TOTAL\tload_sum=%.1fms\tload_avg=%.2fms\tsize=%.1fKB%n",
                totalLoad, avgLoad, totalSize / 1024.0);
    }

    private record Result(String name, boolean isNew, long bytes, double loadMs,
                          double firstTokenizeMs, double genericLinesPerSec, double sampleLinesPerSec) {
    }
}

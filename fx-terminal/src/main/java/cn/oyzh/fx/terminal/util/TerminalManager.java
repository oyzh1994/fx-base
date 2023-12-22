package cn.oyzh.fx.terminal.util;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 终端管理类
 *
 * @author oyzh
 * @since 2023/7/21
 */
@UtilityClass
public class TerminalManager {

    /**
     * 命令处理器列表
     */
    private static final Map<String, TerminalCommandHandler> CACHE = new ConcurrentHashMap<>();

    /**
     * 列表命令处理器
     *
     * @return 命令处理器列表
     */
    public static Collection<TerminalCommandHandler> listHandler() {
        return CACHE.values().stream().sorted(Comparator.comparing(TerminalCommandHandler::commandFullName)).collect(Collectors.toList());
    }

    /**
     * 寻找命令处理器
     *
     * @param commandText 命令内容
     * @param matchType   匹配类型 1: 命令开头匹配内容 2: 命令匹配内容 3: 命令开头匹配内容或者内容开庭匹配命令
     * @return 命令处理器列表
     */
    public static List<TerminalCommandHandler> findHandlers(String commandText, int matchType) {
        List<TerminalCommandHandler> commands = new ArrayList<>();
        for (TerminalCommandHandler value : CACHE.values()) {
            String command = value.commandFullName();
            if (matchType == 1 && StrUtil.startWithIgnoreCase(command, commandText)) {
                commands.add(value);
            } else if (matchType == 2 && StrUtil.equalsIgnoreCase(command, commandText)) {
                commands.add(value);
            } else if (matchType == 3 && (StrUtil.startWithIgnoreCase(command, commandText) || StrUtil.startWithIgnoreCase(commandText, command))) {
                commands.add(value);
            }
        }
        return commands.parallelStream().sorted(Comparator.comparing(TerminalCommandHandler::commandFullName)).collect(Collectors.toList());
    }

    /**
     * 列举命令
     *
     * @return 命令列表
     */
    public static List<String> listCommands() {
        return CACHE.values().stream().map(TerminalCommandHandler::commandName).collect(Collectors.toList());
    }

    /**
     * 寻找命令
     *
     * @param commandText 命令文本
     * @return 命令列表
     */
    public static List<String> findCommands(String commandText) {
        List<String> commands = new ArrayList<>();
        for (TerminalCommandHandler value : CACHE.values()) {
            if (StrUtil.startWithIgnoreCase(value.commandName(), commandText)) {
                commands.add(value.commandName());
            }
        }
        return commands;
    }

    /**
     * 注册命令处理器
     *
     * @param handler 处理器
     */
    public static void registerHandler(TerminalCommandHandler handler) {
        if (handler != null) {
            CACHE.put(handler.commandFullName(), handler);
        }
    }

    /**
     * 获取命令处理器
     *
     * @param input 输入
     * @return 命令处理器
     */
    public static TerminalCommandHandler findHandler(String input) {
        if (input != null) {
            String[] words = TerminalUtil.split(input);
            List<TerminalCommandHandler> list = CACHE.values().parallelStream().filter(s -> StrUtil.equalsIgnoreCase(s.commandName(), words[0])).toList();
            if (!list.isEmpty()) {
                if (words.length >= 2) {
                    List<TerminalCommandHandler> list1 = list.parallelStream().filter(s -> StrUtil.equalsIgnoreCase(s.commandSubName(), words[1])).toList();
                    if (!list1.isEmpty()) {
                        return list1.get(0);
                    }
                }
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 获取处理器
     *
     * @param commandName 命令名称
     * @return 命令处理器
     */
    public static TerminalCommandHandler getHandler(String commandName) {
        return CACHE.get(commandName);
    }
}

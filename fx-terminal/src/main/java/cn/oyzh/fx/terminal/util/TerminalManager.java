package cn.oyzh.fx.terminal.util;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 终端管理类
 *
 * @author oyzh
 * @since 2023/7/21
 */
@UtilityClass
public class TerminalManager {


    static {

    }


    private static void scanHandler() {
    }

    // /**
    //  * 命令处理器列表
    //  */
    // private static final Map<String, TerminalCommandHandler<?,?>> CACHE = new ConcurrentHashMap<>();
    //

    private static final List<TerminalCommandHandler<?, ?>> COMMAND_HANDLERS = new ArrayList<>();

    /**
     * 列表命令处理器
     *
     * @return 命令处理器列表
     */
    public static Collection<TerminalCommandHandler<?, ?>> listHandler() {
        return new ArrayList<>(COMMAND_HANDLERS);
    }

    /**
     * 列表命令处理器
     *
     * @param commandHandlerClass 处理器类
     */
    public static void registerHandler(Class<?> commandHandlerClass) {
        try {
            Object object = commandHandlerClass.getConstructor().newInstance();
            registerHandler((TerminalCommandHandler<?, ?>) object);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 列举命令处理器
     *
     * @param commandHandler 处理器
     */
    public static void registerHandler(TerminalCommandHandler<?, ?> commandHandler) {
        if (commandHandler != null) {
            COMMAND_HANDLERS.add(commandHandler);
        }
    }

    /**
     * 寻找命令处理器
     *
     * @param commandHandlerClass 处理器类
     * @return 处理器
     */
    public static <T extends TerminalCommandHandler<?,?>> T findHandler(Class<?> commandHandlerClass) {
        if (commandHandlerClass != null) {
            for (TerminalCommandHandler<?, ?> commandHandler : COMMAND_HANDLERS) {
                if (commandHandler.getClass().isAssignableFrom(commandHandlerClass)) {
                    return (T) commandHandler;
                }
            }
        }
        return null;
    }

    /**
     * 寻找命令处理器
     *
     * @param commandText 命令内容
     * @param matchType   匹配类型 1: 命令开头匹配内容 2: 命令匹配内容 3: 命令开头匹配内容或者内容开庭匹配命令
     * @return 命令处理器列表
     */
    public static List<TerminalCommandHandler<?,?>> findHandlers(String commandText, int matchType) {
        List<TerminalCommandHandler<?,?>> commands = new ArrayList<>();
        for (TerminalCommandHandler<?,?> value : COMMAND_HANDLERS) {
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

    // /**
    //  * 列举命令
    //  *
    //  * @return 命令列表
    //  */
    // public static List<String> listCommands() {
    //     return CACHE.values().stream().map(TerminalCommandHandler::commandName).collect(Collectors.toList());
    // }

    // /**
    //  * 寻找命令
    //  *
    //  * @param commandText 命令文本
    //  * @return 命令列表
    //  */
    // public static List<String> findCommands(String commandText) {
    //     List<String> commands = new ArrayList<>();
    //     for (TerminalCommandHandler value : COMMAND_HANDLERS) {
    //         if (StrUtil.startWithIgnoreCase(value.commandName(), commandText)) {
    //             commands.add(value.commandName());
    //         }
    //     }
    //     return commands;
    // }

    /**
     * 获取命令处理器
     *
     * @param input 输入
     * @return 命令处理器
     */
    public static TerminalCommandHandler findHandler(String input) {
        if (input != null) {
            String[] words = TerminalUtil.split(input);
            List<TerminalCommandHandler<?, ?>> list = COMMAND_HANDLERS.parallelStream().filter(s -> StrUtil.equalsIgnoreCase(s.commandName(), words[0])).toList();
            if (!list.isEmpty()) {
                if (words.length >= 2) {
                    List<TerminalCommandHandler<?, ?>> list1 = list.parallelStream().filter(s -> StrUtil.equalsIgnoreCase(s.commandSubName(), words[1])).toList();
                    if (!list1.isEmpty()) {
                        return list1.getFirst();
                    }
                }
                return list.getFirst();
            }
        }
        return null;
    }

    // /**
    //  * 获取处理器
    //  *
    //  * @param commandName 命令名称
    //  * @return 命令处理器
    //  */
    // public static TerminalCommandHandler getHandler(String commandName) {
    //     return COMMAND_HANDLERS.get(commandName);
    // }
}

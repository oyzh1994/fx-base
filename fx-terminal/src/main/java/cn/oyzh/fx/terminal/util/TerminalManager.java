package cn.oyzh.fx.terminal.util;

import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.TerminalConst;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.exception.TerminalException;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 终端管理类
 *
 * @author oyzh
 * @since 2023/7/21
 */
@UtilityClass
public class TerminalManager {

    private static final List<TerminalCommandHandler<?, ?>> COMMAND_HANDLERS = new ArrayList<>();

    static {
        scanHandler();
    }

    private static void scanHandler() {
        // 类过滤器
        Predicate<Class<?>> filter = c -> {
            if (c.isArray() || c.isAnnotation() || c.isEnum() || c.isHidden() || c.isInterface() || c.isRecord()) {
                return false;
            }
            int modifiers = c.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                return false;
            }
            if (TerminalCommandHandler.class.isAssignableFrom(c)) {
                registerHandler(c);
                return true;
            }
            return false;
        };
        try {
            // 标准命令
            String standard = TerminalConst.standard();
            ClassUtil.scanClasses(standard, filter);
            // 扩展命令
            String packageBase = TerminalConst.scanBase();
            if (packageBase != null) {
                ClassUtil.scanClasses(packageBase, filter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
            if (findHandler(commandHandlerClass) != null) {
                throw new TerminalException("Multiple Command Handler for: " + commandHandlerClass);
            }
            Object object = ClassUtil.newInstance(commandHandlerClass);
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
    public static <T extends TerminalCommandHandler<?, ?>> T findHandler(Class<?> commandHandlerClass) {
        if (commandHandlerClass != null) {
            for (TerminalCommandHandler<?, ?> commandHandler : COMMAND_HANDLERS) {
                if (commandHandler.getClass() == commandHandlerClass) {
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
    public static List<TerminalCommandHandler<?, ?>> findHandlers(String commandText, int matchType) {
        List<TerminalCommandHandler<?, ?>> commands = new ArrayList<>();
        for (TerminalCommandHandler<?, ?> value : COMMAND_HANDLERS) {
            String command = value.commandFullName();
            if (matchType == 1 && StringUtil.startWithIgnoreCase(command, commandText)) {
                commands.add(value);
            } else if (matchType == 2 && StringUtil.equalsIgnoreCase(command, commandText)) {
                commands.add(value);
            } else if (matchType == 3 && (StringUtil.startWithIgnoreCase(command, commandText) || StringUtil.startWithIgnoreCase(commandText, command))) {
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
    //         if (StringUtil.startWithIgnoreCase(value.commandName(), commandText)) {
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
    public static TerminalCommandHandler<?, ?> findHandler(String input) {
        if (input != null) {
            String[] words = TerminalUtil.split(input);
            List<TerminalCommandHandler<?, ?>> list = COMMAND_HANDLERS.parallelStream().filter(s -> StringUtil.equalsIgnoreCase(s.commandName(), words[0])).toList();
            if (!list.isEmpty()) {
                if (words.length >= 2) {
                    List<TerminalCommandHandler<?, ?>> list1 = list.parallelStream().filter(s -> StringUtil.equalsIgnoreCase(s.commandSubName(), words[1])).toList();
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

package cn.oyzh.fx.terminal.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import lombok.Getter;
import lombok.Setter;
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

    /**
     * 命令处理器列表
     */
    private static final List<TerminalCommandHandler<?, ?>> COMMAND_HANDLERS = new ArrayList<>(128);

    // static {
    //     scanHandler();
    // }
    //
    // private static void scanHandler() {
    //     try {
    //         JulLog.info("scanHandler start");
    //         // 类过滤器
    //         Predicate<Class<?>> filter = c -> {
    //             if (c.isArray() || c.isAnnotation() || c.isEnum() || c.isHidden() || c.isInterface() || c.isRecord()) {
    //                 return false;
    //             }
    //             int modifiers = c.getModifiers();
    //             if (Modifier.isAbstract(modifiers)) {
    //                 return false;
    //             }
    //             if (TerminalCommandHandler.class.isAssignableFrom(c)) {
    //                 registerHandler(c);
    //                 return true;
    //             }
    //             return false;
    //         };
    //         // 标准命令
    //         String standard = TerminalConst.standard();
    //         List<Class<?>> list = ClassUtil.scanClasses(standard, filter);
    //         // 扩展命令
    //         String packageBase = TerminalConst.scanBase();
    //         if (packageBase != null) {
    //             List<Class<?>> list1 = ClassUtil.scanClasses(packageBase, filter);
    //             list.addAll(list1);
    //         }
    //         JulLog.info("scanHandler finish classList:{}", list);
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         JulLog.warn("scanHandler error", ex);
    //     }
    // }

    /**
     * 加载处理器操作
     */
    @Setter
    @Getter
    private static Runnable loadHandlerAction;

    /**
     * 是否已经加载
     */
    private static boolean loaded = false;

    /**
     * 加载处理器
     */
    private static void doLoadHandler() {
        try {
            if (!loaded && loadHandlerAction != null) {
                loaded = true;
                loadHandlerAction.run();
            }
        } catch (Exception ex) {
            loaded = false;
            ex.printStackTrace();
            JulLog.warn("doLoadHandlers error", ex);
        }
    }

    /**
     * 列表命令处理器
     *
     * @return 命令处理器列表
     */
    public static Collection<TerminalCommandHandler<?, ?>> listHandler() {
        doLoadHandler();
        return new ArrayList<>(COMMAND_HANDLERS);
    }

    /**
     * 列表命令处理器
     *
     * @param commandHandlerClass 处理器类
     */
    public static void registerHandler(Class<? extends TerminalCommandHandler<?, ?>> commandHandlerClass) {
        try {
            if (findHandler(commandHandlerClass) != null) {
                JulLog.warn("commandHandlerClass:{} is registered", commandHandlerClass);
                // throw new TerminalException("Multiple Command Handler for: " + commandHandlerClass);
                return;
            }
            TerminalCommandHandler<?, ?> object = ClassUtil.newInstance(commandHandlerClass);
            registerHandler(object);
            if (object != null) {
                JulLog.info("registerHandler success, commandHandlerClass:{}", commandHandlerClass);
            } else {
                JulLog.warn("registerHandler fail, commandHandlerClass:{}", commandHandlerClass);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("registerHandler error commandHandlerClass:{}", commandHandlerClass, ex);
        }
    }

    /**
     * 列举命令处理器
     *
     * @param commandHandler 处理器
     */
    public static void registerHandler(TerminalCommandHandler<?, ?> commandHandler) {
        try {
            if (commandHandler != null) {
                COMMAND_HANDLERS.add(commandHandler);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("registerHandler error commandHandler:{}", commandHandler.getClass(), ex);
        }
    }

    /**
     * 寻找命令处理器
     *
     * @param commandHandlerClass 处理器类
     * @return 处理器
     */
    public static <T extends TerminalCommandHandler<?, ?>> T findHandler(Class<?> commandHandlerClass) {
        try {
            if (commandHandlerClass != null) {
                doLoadHandler();
                for (TerminalCommandHandler<?, ?> commandHandler : COMMAND_HANDLERS) {
                    if (commandHandler != null && commandHandler.getClass() == commandHandlerClass) {
                        return (T) commandHandler;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("registerHandler error commandHandlerClass:{}", commandHandlerClass, ex);
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
        try {
            doLoadHandler();
            List<TerminalCommandHandler<?, ?>> commands = new ArrayList<>(COMMAND_HANDLERS.size());
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
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("registerHandler error commandText:{} matchType:{}", commandText, matchType, ex);
        }
        return null;
    }

    /**
     * 获取命令处理器
     *
     * @param input 输入
     * @return 命令处理器
     */
    public static TerminalCommandHandler<?, ?> findHandler(String input) {
        try {
            if (input != null) {
                doLoadHandler();
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
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("findHandler error input:{}", input, ex);
        }
        return null;
    }
}

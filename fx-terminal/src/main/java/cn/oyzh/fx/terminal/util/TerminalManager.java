package cn.oyzh.fx.terminal.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 终端管理类
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class TerminalManager {

    /**
     * 命令处理器列表
     * key: 终端名称 value: 命令处理器列表
     */
    private static final Map<String, List<TerminalCommandHandler<?, ?>>> COMMAND_HANDLERS = HashMap.newHashMap(128);

    /**
     * 处理器加载操作列表
     * key: 终端名称 value: 操作器
     */
    private static final Map<String, Runnable> LOAD_HANDLES = HashMap.newHashMap(2);

    /**
     * 处理器加载标记列表
     * key: 终端名称 value: 标记
     */
    private static final Map<String, Boolean> LOAD_FLAGS = HashMap.newHashMap(2);

    /**
     * 设置加载操作
     *
     * @param name        终端名称
     * @param loadHandler 加载操作
     */
    public static void setLoadHandler(String name, Runnable loadHandler) {
        LOAD_HANDLES.put(name, loadHandler);
    }

    /**
     * 获取加载操作
     *
     * @param name 终端名称
     * @return 加载操作
     */
    public static Runnable getLoadHandler(String name) {
        return LOAD_HANDLES.get(name);
    }

    /**
     * 加载处理器
     *
     * @param name 终端名称
     */
    private static void doLoadHandler(String name) {
        if (LOAD_FLAGS.containsKey(name)) {
            return;
        }
        try {
            Runnable loadHandler = getLoadHandler(name);
            if (loadHandler != null) {
                LOAD_FLAGS.put(name, true);
                loadHandler.run();
            }
        } catch (Exception ex) {
            LOAD_FLAGS.put(name, false);
            ex.printStackTrace();
            JulLog.warn("doLoadHandler error", ex);
        }
    }

    /**
     * 列表命令处理器
     *
     * @param name 终端名称
     * @return 命令处理器列表
     */
    public static Collection<TerminalCommandHandler<?, ?>> listHandler(String name) {
        doLoadHandler(name);
        return new ArrayList<>(COMMAND_HANDLERS.get(name));
    }

    /**
     * 列表命令处理器
     *
     * @param name                终端名称
     * @param commandHandlerClass 处理器类
     */
    public static void registerHandler(String name, Class<? extends TerminalCommandHandler<?, ?>> commandHandlerClass) {
        try {
            if (findHandler(name, commandHandlerClass) != null) {
                JulLog.warn("commandHandlerClass:{} is registered", commandHandlerClass);
                return;
            }
            TerminalCommandHandler<?, ?> object = ClassUtil.newInstance(commandHandlerClass);
            registerHandler(name, object);
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
     * @param name           终端名称
     * @param commandHandler 处理器
     */
    public static void registerHandler(String name, TerminalCommandHandler<?, ?> commandHandler) {
        try {
            if (commandHandler != null) {
                List<TerminalCommandHandler<?, ?>> list = COMMAND_HANDLERS.computeIfAbsent(name, k -> new ArrayList<>());
                list.add(commandHandler);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("registerHandler error commandHandler:{}", commandHandler.getClass(), ex);
        }
    }

    /**
     * 寻找命令处理器
     *
     * @param name                终端名称
     * @param commandHandlerClass 处理器类
     * @return 处理器
     */
    public static <T extends TerminalCommandHandler<?, ?>> T findHandler(String name, Class<?> commandHandlerClass) {
        try {
            if (commandHandlerClass != null) {
                Collection<TerminalCommandHandler<?, ?>> handlers = listHandler(name);
                for (TerminalCommandHandler<?, ?> commandHandler : handlers) {
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
     * @param name        终端名称
     * @param commandText 命令内容
     * @param matchType   匹配类型 1: 命令开头匹配内容 2: 命令匹配内容 3: 命令开头匹配内容或者内容开庭匹配命令
     * @return 命令处理器列表
     */
    public static List<TerminalCommandHandler<?, ?>> findHandlers(String name, String commandText, int matchType) {
        try {
            Collection<TerminalCommandHandler<?, ?>> handlers = listHandler(name);
            List<TerminalCommandHandler<?, ?>> commands = new ArrayList<>(handlers.size());
            for (TerminalCommandHandler<?, ?> value : handlers) {
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
     * @param name  终端名称
     * @param input 输入
     * @return 命令处理器
     */
    public static TerminalCommandHandler<?, ?> findHandler(String name, String input) {
        try {
            if (input != null) {
                Collection<TerminalCommandHandler<?, ?>> handlers = listHandler(name);
                String[] words = TerminalUtil.split(input);
                List<TerminalCommandHandler<?, ?>> list = handlers.parallelStream().filter(s -> StringUtil.equalsIgnoreCase(s.commandName(), words[0])).toList();
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

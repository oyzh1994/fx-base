// package cn.oyzh.fx.terminal.util;
//
// import cn.hutool.extra.spring.SpringUtil;
// import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.PostConstruct;
// import java.util.Map;
//
// /**
//  * 命令处理器扫描器
//  *
//  * @author oyzh
//  * @since 2023/7/21
//  */
// @Component
// public class TerminalScanner {
//
//     /**
//      * 扫描命令处理器
//      */
//     @PostConstruct
//     public void scanHandler() {
//         Map<String, TerminalCommandHandler> map = SpringUtil.getBeansOfType(TerminalCommandHandler.class);
//         if (map != null) {
//             for (TerminalCommandHandler value : map.values()) {
//                 TerminalManager.registerHandler(value);
//             }
//         }
//     }
// }

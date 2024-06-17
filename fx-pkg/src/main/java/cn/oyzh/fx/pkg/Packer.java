package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.comporess.CompressHandler;
import cn.oyzh.fx.pkg.comporess.CompressNameHandler;
import cn.oyzh.fx.pkg.config.ConfHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.config.ExtPackrConfigParser;
import cn.oyzh.fx.pkg.jar.JarHandler;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.jre.JreHandler;
import cn.oyzh.fx.pkg.pack.EndHandler;
import cn.oyzh.fx.pkg.pack.PackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class Packer {

    private final List<Handler> handlers = new ArrayList<>();

    private final PackHandler packHandler = new PackHandler();

    private final ExtPackrConfigParser extPackrConfigParser = new ExtPackrConfigParser();

    public void registerEndHandler() {
        this.registerHandler(new EndHandler());
    }

    public void registerConfHandler() {
        this.registerHandler(new ConfHandler());
    }

    public void registerJreHandler(String configFile) {
        JreHandler jreHandler = new JreHandler();
        jreHandler.parse(configFile);
        this.registerHandler(jreHandler);
    }

    public void registerJarHandler(String configFile) {
        this.registerHandler(new JarHandler(configFile));
    }

    public void registerJLinkHandler(String configFile) {
        this.registerHandler(new JLinkHandler(configFile));
    }

    public void registerCompressHandler() {
        this.registerHandler(new CompressHandler());
    }

    public void registerCompressNameHandler() {
        this.registerHandler(new CompressNameHandler());
    }

    public void registerHandler(Handler handler) {
        this.handlers.add(handler);
        this.handlers.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));
    }

    private List<PreHandler> preHandlers() {
        List<PreHandler> preHandlers = new ArrayList<>();
        for (Handler handler : handlers) {
            if (handler instanceof PreHandler preHandler) {
                preHandlers.add(preHandler);
            }
        }
        return preHandlers;
    }

    private List<PostHandler> postHandlers() {
        List<PostHandler> postHandlers = new ArrayList<>();
        for (Handler handler : handlers) {
            if (handler instanceof PostHandler postHandler) {
                postHandlers.add(postHandler);
            }
        }
        return postHandlers;
    }

    public void pack(String configFile) throws Exception {
        ExtPackrConfig packrConfig = this.extPackrConfigParser.parse(configFile);
        for (PreHandler preHandler : this.preHandlers()) {
            preHandler.handle(packrConfig);
        }
        this.packHandler.handle(packrConfig);
        for (PostHandler postHandler : this.postHandlers()) {
            postHandler.handle(packrConfig);
        }
    }
}

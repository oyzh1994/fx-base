package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.packr.PackrHandler;
import cn.oyzh.fx.pkg.packr.PackrConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class Packer {

    private final List<Handler> handlers = new ArrayList<>();

    private final PackrHandler packrHandler = new PackrHandler();

    public void registerHandler(Handler handler) {
        this.handlers.add(handler);
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
        PackrConfig config = this.packrHandler.parse(configFile);
        ExtConfig extConfig = new ExtConfig();
        for (PreHandler preHandler : this.preHandlers()) {
            preHandler.handle(config, extConfig);
        }
        this.packrHandler.handle(config, extConfig);
        for (PostHandler postHandler : this.postHandlers()) {
            postHandler.handle(config, extConfig);
        }
    }

}

package org.wumoe.devore.httpserver;

import org.wumoe.devore.Devore;
import org.wumoe.devore.plugins.DPlugin;

public class HttpServerPlugin extends DPlugin {
    @Override
    public void onEnable() {
        Devore.addModule("http.server", new HttpServerModule());
    }

    @Override
    public void onDisable() {

    }
}

package org.wumoe.devore.httpserver;

import com.sun.net.httpserver.HttpServer;
import org.wumoe.devore.lang.token.Token;

public class DHttpServer extends Token {
    public HttpServer server;

    public DHttpServer(HttpServer server) {
        this.server = server;
    }

    @Override
    public String type() {
        return "httpserver";
    }

    @Override
    public String str() {
        return server.toString();
    }

    @Override
    public Token copy() {
        return new DHttpServer(server);
    }

    @Override
    public int compareTo(Token token) {
        return token instanceof DHttpServer hs && hs.server.equals(this.server) ? 0 : -1;
    }
}

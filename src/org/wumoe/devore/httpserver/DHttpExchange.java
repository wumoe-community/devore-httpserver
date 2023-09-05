package org.wumoe.devore.httpserver;

import com.sun.net.httpserver.HttpExchange;
import org.wumoe.devore.lang.token.Token;

public class DHttpExchange extends Token {
    public HttpExchange exchange;

    public DHttpExchange(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String type() {
        return "httpexchange";
    }

    @Override
    public String str() {
        return exchange.toString();
    }

    @Override
    public Token copy() {
        return new DHttpExchange(exchange);
    }

    @Override
    public int compareTo(Token token) {
        return token instanceof DHttpExchange ex && ex.exchange.equals(this.exchange) ? 0 : -1;
    }
}

package org.wumoe.devore.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.*;
import org.wumoe.devore.module.Module;
import org.wumoe.devore.parser.AstNode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpServerModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("httpserver-create", ((args, env) -> {
            if (!(args.get(0) instanceof DInt port))
                throw new DevoreCastException(args.get(0).type(), "int");
            try {
                return new DHttpServer(HttpServer.create(new InetSocketAddress(port.toBigIntger().intValue()), 0));
            } catch (IOException e) {
                throw new DevoreRuntimeException(e.getMessage());
            }
        }), 1, false);
        dEnv.addTokenFunction("httpserver-start", ((args, env) -> {
            if (!(args.get(0) instanceof DHttpServer server))
                throw new DevoreCastException(args.get(0).type(), "httpserver");
            server.server.start();
            return DWord.WORD_NIL;
        }), 1, false);
        dEnv.addTokenFunction("httpserver-context-create", ((args, env) -> {
            if (!(args.get(0) instanceof DHttpServer server))
                throw new DevoreCastException(args.get(0).type(), "httpserver");
            if (!(args.get(1) instanceof DString path))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DFunction func))
                throw new DevoreCastException(args.get(2).type(), "function");
            server.server.createContext(path.toString(), (exchange) -> {
                AstNode asts = AstNode.nullAst.copy();
                asts.add(new AstNode(new DHttpExchange(exchange)));
                byte[] result = func.call(asts, env.createChild()).toString().getBytes(StandardCharsets.UTF_8);
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, result.length);
                OutputStream os = exchange.getResponseBody();
                os.write(result);
                os.close();
                exchange.close();
            });
            return DWord.WORD_NIL;
        }), 3, false);
        dEnv.addTokenFunction("httpserver-exchange-get", ((args, env) -> {
            if (!(args.get(0) instanceof DHttpExchange exchange))
                throw new DevoreCastException(args.get(0).type(), "httpexchange");
            Map<String, String> data = formData2Dic(exchange.exchange.getRequestURI().getQuery());
            Map<Token, Token> table = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                table.put(DString.valueOf(entry.getKey()), DString.valueOf(entry.getValue()));
            }
            return DTable.valueOf(table);
        }), 1, false);
        dEnv.addTokenFunction("httpserver-exchange-post", ((args, env) -> {
            if (!(args.get(0) instanceof DHttpExchange exchange))
                throw new DevoreCastException(args.get(0).type(), "httpexchange");
            Map<String, String> data = formData2Dic(exchangePostData(exchange));
            Map<Token, Token> table = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                table.put(DString.valueOf(entry.getKey()), DString.valueOf(entry.getValue()));
            }
            return DTable.valueOf(table);
        }), 1, false);
    }

    private static Map<String,String> formData2Dic(String formData) {
        Map<String,String> result = new HashMap<>();
        if(formData== null || formData.trim().isEmpty())
            return result;
        final String[] items = formData.split("&");
        Arrays.stream(items).forEach(item ->{
            final String[] keyAndVal = item.split("=");
            if( keyAndVal.length == 2) {
                final String key = URLDecoder.decode(keyAndVal[0], StandardCharsets.UTF_8);
                final String val = URLDecoder.decode(keyAndVal[1], StandardCharsets.UTF_8);
                result.put(key,val);
            }
        });
        return result;
    }

    private static String exchangePostData(DHttpExchange exchange) {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        try (InputStreamReader in = new InputStreamReader(exchange.exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }
}

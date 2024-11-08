package ru.yandex.java_kanban.server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.server.adapters.DurationAdapter;
import ru.yandex.java_kanban.server.adapters.LocalDateTimeAdapter;
import ru.yandex.java_kanban.server.enums.HttpMethod;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

abstract class BaseHttpHandler implements HttpHandler {
    protected static final int HTTP_OK = 200;
    protected static final int HTTP_CREATED = 201;
    protected static final int HTTP_NOT_FOUND = 404;
    protected static final int HTTP_METHOD_NOT_ALLOWED = 405;
    protected static final int HTTP_NOT_ACCEPTABLE = 406;
    protected static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    protected HttpMethod getMethod(HttpExchange exchange) {
        return HttpMethod.valueOf(exchange.getRequestMethod());
    }

    protected String[] getPathElements(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(1).split("/");
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, HTTP_OK);
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, HTTP_CREATED);
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, HTTP_NOT_FOUND);
    }

    protected void sendNotAllowed(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, HTTP_METHOD_NOT_ALLOWED);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
       sendResponse(h, text, HTTP_NOT_ACCEPTABLE);
    }

    protected void sendServerError(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, HTTP_INTERNAL_SERVER_ERROR);
    }

    private void sendResponse(HttpExchange h, String text, int code) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);

        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}

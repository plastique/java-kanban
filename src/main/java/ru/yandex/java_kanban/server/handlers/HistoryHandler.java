package ru.yandex.java_kanban.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.managers.contracts.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();

        if (getMethod(exchange).equals("GET")) {
            sendText(exchange, gson.toJson(taskManager.getHistory()));
            return;
        }

        sendNotFound(exchange, "Not Found");
    }
}

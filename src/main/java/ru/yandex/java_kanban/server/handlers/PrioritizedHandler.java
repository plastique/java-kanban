package ru.yandex.java_kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.server.enums.HttpMethod;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (getMethod(exchange) == HttpMethod.GET) {
            sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
            return;
        }

        sendNotAllowed(exchange, "Not Allowed");
    }
}

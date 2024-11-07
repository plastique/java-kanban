package ru.yandex.java_kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.exceptions.NotFoundException;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] pathElements = getPathElements(exchange);
        boolean hasId = pathElements.length >= 2;
        boolean requireSubtasks = pathElements.length == 3 && pathElements[2].equals("subtasks");
        int id = hasId ? Integer.parseInt(pathElements[1]) : 0;
        Epic epic = null;

        switch (getMethod(exchange)) {
            case "GET":
                if (!hasId) {
                    sendText(exchange, gson.toJson(taskManager.getEpics()));
                    return;
                }

                try {
                    if (requireSubtasks) {
                        sendText(exchange, gson.toJson(taskManager.getEpicSubtasks(id)));
                        return;
                    }
                    sendText(exchange, gson.toJson(getEpicById(id)));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Эпик не найден");
                } catch (Throwable e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка");
                }
                break;

            case "POST":
                epic = gson.fromJson(
                    new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    Epic.class
                );

                try {
                    epic = taskManager.createEpic(epic);
                    sendCreated(exchange, gson.toJson(epic));
                    return;
                } catch (Throwable e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка");
                }
                break;

            case "DELETE":
                taskManager.deleteSubtaskById(id);
                sendText(exchange, "Эпик удален");
                break;

            default:
                sendNotFound(exchange, "Not Found");
        }
    }

    private Epic getEpicById(int id) {
        Epic epic = taskManager.getEpicById(id);

        if (epic == null) {
            throw new NotFoundException("Epic not found");
        }

        return epic;
    }
}

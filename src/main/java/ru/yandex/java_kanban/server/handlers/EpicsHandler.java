package ru.yandex.java_kanban.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.exceptions.ManagerSaveException;
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
        int id = Integer.parseInt(pathElements[1]);
        Gson gson = new Gson();
        Epic epic = null;

        switch (getMethod(exchange)) {
            case "GET":
                if (!hasId) {
                    sendText(exchange, gson.toJson(taskManager.getEpics()));
                    return;
                }

                if (requireSubtasks) {
                    sendText(exchange, gson.toJson(taskManager.getEpicSubtasks(id)));
                    return;
                }

                try {
                    epic = getEpicById(id);
                    sendText(exchange, gson.toJson(epic));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Эпик не найден");
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
                } catch (ManagerSaveException e) {
                    sendServerError(exchange, "Внутренняя ошибка, не удалось сохранить эпик");
                }

                break;

            case "DELETE":
                try {
                    epic = getEpicById(id);
                    taskManager.deleteSubtaskById(id);
                    sendText(exchange, "Эпик удален");
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Эпик не найден");
                }
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

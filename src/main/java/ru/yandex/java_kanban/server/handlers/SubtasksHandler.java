package ru.yandex.java_kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.exceptions.NotFoundException;
import ru.yandex.java_kanban.exceptions.TaskIntersectionException;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] pathElements = getPathElements(exchange);
        boolean hasId = pathElements.length == 2;
        int id = hasId ? Integer.parseInt(pathElements[1]) : 0;
        Subtask subtask = null;

        switch (getMethod(exchange)) {
            case GET:
                if (!hasId) {
                    sendText(exchange, gson.toJson(taskManager.getSubtasks()));
                    return;
                }

                try {
                    subtask = getSubtaskById(id);
                    sendText(exchange, gson.toJson(subtask));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Подзадача не найдена");
                } catch (Throwable e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка, не удалось сохранить эпик");
                }
                break;

            case POST:
                subtask = gson.fromJson(
                    new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    Subtask.class
                );

                id = subtask.getId();
                hasId = id > 0;

                try {
                    if (!hasId) {
                        subtask = taskManager.createSubtask(subtask);
                        sendCreated(exchange, gson.toJson(subtask));
                        return;
                    }

                    Subtask existingSubtask = getSubtaskById(id);
                    subtask = taskManager.updateSubtask(subtask);
                    sendCreated(exchange, gson.toJson(subtask));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Подзадача не найдена");
                } catch (TaskIntersectionException e) {
                    sendHasInteractions(exchange, "Подзадача пересекается с существующей");
                } catch (Exception e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка");
                }
                break;

            case DELETE:
                taskManager.deleteSubtaskById(id);
                sendText(exchange, "Подзадача удалена");
                break;

            default:
                sendNotAllowed(exchange, "Not Allowed");
        }
    }

    private Subtask getSubtaskById(int id) {
        Subtask subtask = taskManager.getSubtaskById(id);

        if (subtask == null) {
            throw new NotFoundException("Subtask not found");
        }

        return subtask;
    }
}

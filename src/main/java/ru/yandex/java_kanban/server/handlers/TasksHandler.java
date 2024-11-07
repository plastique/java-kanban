package ru.yandex.java_kanban.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.exceptions.NotFoundException;
import ru.yandex.java_kanban.exceptions.TaskIntersectionException;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] pathElements = getPathElements(exchange);
        boolean hasId = pathElements.length == 2;
        int id = hasId ? Integer.parseInt(pathElements[1]) : 0;
        Task task = null;

        switch (getMethod(exchange)) {
            case "GET":
                if (!hasId) {
                    sendText(exchange, gson.toJson(taskManager.getTasks()));
                    return;
                }

                try {
                    task = getTaskById(id);
                    sendText(exchange, gson.toJson(task));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Задача не найдена");
                } catch (Throwable e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка");
                }
                break;

            case "POST":
                task = gson.fromJson(
                    new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    Task.class
                );

                id = task.getId();
                hasId = id > 0;

                try {
                    if (!hasId) {
                        task = taskManager.createTask(task);
                        sendCreated(exchange, gson.toJson(task));
                        return;
                    }

                    Task existingTask = getTaskById(id);
                    task = taskManager.updateTask(task);
                    sendCreated(exchange, gson.toJson(task));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Задача не найдена");
                } catch (TaskIntersectionException e) {
                    sendHasInteractions(exchange, "Задача пересекается с существующей");
                } catch (Throwable e) {
                    System.out.println("Error:" + e.getMessage());
                    sendServerError(exchange, "Внутренняя ошибка");
                }
                break;

            case "DELETE":
                taskManager.deleteTaskById(id);
                sendText(exchange, "Задача удалена");
                break;

            default:
                sendNotFound(exchange, "Not Found");
        }
    }

    private Task getTaskById(int id) {
        Task task = taskManager.getTaskById(id);

        if (task == null) {
            throw new NotFoundException("Task not found");
        }

        return task;
    }
}

package ru.yandex.java_kanban.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.java_kanban.exceptions.ManagerSaveException;
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
        int id = Integer.parseInt(pathElements[1]);
        Gson gson = new Gson();
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
                } catch (ManagerSaveException e) {
                    sendServerError(exchange, "Внутренняя ошибка, не удалось сохранить задачу");
                }

                break;

            case "DELETE":
                try {
                    task = getTaskById(id);
                    taskManager.deleteTaskById(id);
                    sendText(exchange, "Задача удалена");
                } catch (NotFoundException e) {
                    sendNotFound(exchange, "Задача не найдена");
                }
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

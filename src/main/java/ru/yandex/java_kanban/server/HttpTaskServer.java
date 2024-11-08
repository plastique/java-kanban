package ru.yandex.java_kanban.server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.java_kanban.managers.Managers;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(
            Managers.getDefault(Managers.getDefaultHistory())
        );
        server.start();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        server.start();
        System.out.println("Server started on port: " + PORT);
    }

    public void stop() {
        server.stop(1);
        System.out.println("Server stopped");
    }
}

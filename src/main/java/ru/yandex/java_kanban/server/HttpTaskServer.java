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
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault(Managers.getDefaultHistory());
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1);
    }
}

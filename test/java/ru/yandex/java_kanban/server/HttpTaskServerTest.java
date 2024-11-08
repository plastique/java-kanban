package ru.yandex.java_kanban.server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.InMemoryHistoryManager;
import ru.yandex.java_kanban.managers.InMemoryTaskManager;
import ru.yandex.java_kanban.managers.contracts.HistoryManager;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;
import ru.yandex.java_kanban.server.adapters.DurationAdapter;
import ru.yandex.java_kanban.server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    private static final String BASE_URL = "http://localhost:8080";

    HistoryManager historyManager = new InMemoryHistoryManager();
    TaskManager taskManager = new InMemoryTaskManager(historyManager);
    HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskManager.deleteTasks();
        taskManager.deleteSubtasks();
        taskManager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    private static class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    private static class EpicListTypeToken extends TypeToken<List<Epic>> {
    }

    private static class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    private Task makeTask() {
        return new Task(
            "Test 1",
            "Task description",
            TaskStatus.NEW,
            LocalDateTime.now(),
            Duration.ofMinutes(5)
        );
    }

    private Epic makeEpic() {
        return new Epic("Epic 1", "Epic description");
    }

    private Subtask makeSubtask(Epic epic) {
        return new Subtask(
                "Subtask 1",
                "Subtask description",
                TaskStatus.NEW,
                LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(35),
                epic.getId()
        );
    }

    @Test
    public void returnTaskList() throws IOException, InterruptedException {
        Task task1 = taskManager.createTask(makeTask());
        Task task2 = makeTask();
        task2.setStartTime(LocalDateTime.now().plusHours(2));
        task2.setName("Task 2");
        task2 = taskManager.createTask(task2);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), List.class);

        assertEquals(200, response.statusCode());
        assertEquals(2, tasks.size(), "Некорректное количество задач");
    }

    @Test
    public void returnTaskById() throws IOException, InterruptedException {
        Task task = taskManager.createTask(makeTask());

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromJson = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertEquals(task, taskFromJson, "Задачи не совпадают");
    }

    @Test
    public void taskAdded() throws IOException, InterruptedException {
        Task task = makeTask();
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromManager = taskManager.getTasks();

        assertEquals(201, response.statusCode());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void taskUpdated() throws IOException, InterruptedException {
        Task task = makeTask();
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        task = gson.fromJson(response.body(), Task.class);

        task.setName("Updated task name");
        task.setDescription("Updated task description");
        taskManager.updateTask(task);

        taskJson = gson.toJson(task);
        request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + task.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromManager = taskManager.getTasks();

        assertEquals(201, response.statusCode());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void taskDeleted() throws IOException, InterruptedException {
        Task task = taskManager.createTask(makeTask());

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + task.getId()))
                .DELETE()
                .build();

        int tasksCountBeforeDelete = taskManager.getTasks().size();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int tasksCountAfterDelete = taskManager.getTasks().size();

        assertEquals(200, response.statusCode());
        assertNotEquals(tasksCountBeforeDelete, tasksCountAfterDelete, "Некорректное количество задач");
    }

    @Test
    public void returnEpicList() throws IOException, InterruptedException {
        Epic epic1 = taskManager.createEpic(makeEpic());
        Epic epic2 = makeEpic();
        epic2.setName("Epic 2");
        epic2 = taskManager.createEpic(epic2);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), List.class);

        assertEquals(200, response.statusCode());
        assertEquals(2, tasks.size(), "Некорректное количество эпиков");
    }

    @Test
    public void returnEpicById() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epicFromJson = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertEquals(epic, epicFromJson, "Эпики не совпадают");
    }

    @Test
    public void returnEpicSubtasksList() throws IOException, InterruptedException {
        Epic epic1 = taskManager.createEpic(makeEpic());
        Epic epic2 = makeEpic();
        epic2.setName("Epic 2");
        epic2.setDescription("Epic 2 description");
        epic2 = taskManager.createEpic(epic2);

        Subtask subtask1 = taskManager.createSubtask(makeSubtask(epic1));

        Subtask subtask2 = makeSubtask(epic2);
        subtask2.setName("Subtask 2");
        subtask2.setDescription("Subtask 2 description");
        subtask2.setStartTime(LocalDateTime.now().plusHours(2));
        subtask2 = taskManager.createSubtask(subtask2);

        Subtask subtask3 = makeSubtask(epic2);
        subtask3.setName("Subtask 3");
        subtask3.setDescription("Subtask 3 description");
        subtask3.setStartTime(LocalDateTime.now().plusHours(3));
        subtask3 = taskManager.createSubtask(subtask3);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(String.format("%s/epics/%d/subtasks", BASE_URL, epic2.getId())))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertEquals(2, subtasks.size(), "Некорректное количество подзадач");
        assertEquals(subtask3, subtasks.get(1), "Подзадачи не совпадают");
    }

    @Test
    public void epicAdded() throws IOException, InterruptedException {
        Epic epic = makeEpic();
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epicsFromManager = taskManager.getEpics();

        assertEquals(201, response.statusCode());
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic.getName(), epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
    }

    @Test
    public void epicDeleted() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId()))
                .DELETE()
                .build();

        int epicsCountBeforeDelete = taskManager.getEpics().size();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int epicsCountAfterDelete = taskManager.getEpics().size();

        assertEquals(200, response.statusCode());
        assertNotEquals(epicsCountBeforeDelete, epicsCountAfterDelete, "Некорректное количество эпиков");
    }

    @Test
    public void returnSubtasksList() throws IOException, InterruptedException {
        Epic epic1 = taskManager.createEpic(makeEpic());
        Subtask subtask1 = taskManager.createSubtask(
                makeSubtask(epic1)
        );

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Subtask> tasks = gson.fromJson(response.body(), List.class);

        assertEquals(200, response.statusCode());
        assertEquals(1, tasks.size(), "Некорректное количество подзадач");
    }

    @Test
    public void returnSubtaskById() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask subtaskFromJson = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode());
        assertEquals(subtask, subtaskFromJson, "Подзадачи не совпадают");
    }

    @Test
    public void subtaskAdded() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = makeSubtask(epic);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasksFromManager = taskManager.getSubtasks();

        assertEquals(201, response.statusCode());
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask.getName(), subtasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void subtaskUpdated() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = makeSubtask(epic);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        subtask = gson.fromJson(response.body(), Subtask.class);

        subtask.setName("Updated subtask name");
        subtask.setDescription("Updated subtask description");
        taskManager.updateTask(subtask);

        subtaskJson = gson.toJson(subtask);
        request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromManager = taskManager.getTasks();

        assertEquals(201, response.statusCode());
        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void subtaskDeleted() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        int subtasksCountBeforeDelete = taskManager.getSubtasks().size();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int subtasksCountAfterDelete = taskManager.getSubtasks().size();

        assertEquals(200, response.statusCode());
        assertNotEquals(subtasksCountBeforeDelete, subtasksCountAfterDelete, "Некорректное количество подзадач");
    }

    @Test
    public void returnHistory() throws IOException, InterruptedException {
        Task task = taskManager.createTask(makeTask());
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        subtask = gson.fromJson(response.body(), Subtask.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromManager = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(1, tasksFromManager.size(), "Неверное количество задач в истории");
        assertEquals(subtask, tasksFromManager.get(0), "Задачи не совпадают");
    }

    @Test
    public void returnPrioritized() throws IOException, InterruptedException {
        Task task = taskManager.createTask(makeTask());
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = makeSubtask(epic);
        subtask.setStartTime(task.getStartTime().minusHours(1));
        subtask = taskManager.createSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonEl = JsonParser.parseString(response.body());
        JsonArray tasksArr = jsonEl.getAsJsonArray();
        Subtask subtaskJson = gson.fromJson(tasksArr.get(0).getAsJsonObject(), Subtask.class);

        assertEquals(2, tasksArr.size(), "Неверное количество задач в истории");
        assertEquals(subtask, subtaskJson, "Очередность задач не совпадает");
    }
}

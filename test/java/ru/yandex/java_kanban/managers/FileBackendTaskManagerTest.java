package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackendTaskManagerTest {
    private File file;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile("java-kanban-tests-", "-tasks").toFile();
        taskManager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(file.toPath());
    }

    @Test
    void saveEmptyListToFileIsCorrect() throws IOException {
        taskManager.save();
        boolean fileExists = file.exists();
        List<String> lines = Files.readAllLines(Paths.get(file.toString()));

        assertEquals(1, lines.size());
    }

    @Test
    void loadEmptyFileIsCorrect() {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> tasks = taskManager.getTasks();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void tasksAddedToStorage() throws IOException {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final Task savedTask = taskManager.createTask(task);

        Epic epic = new Epic("Test addNewTask", "Test addNewTask description");
        final Epic savedEpic = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test addNewTask", "Test addNewTask description", TaskStatus.NEW, savedEpic.getId());
        final Subtask savedSubtask = taskManager.createSubtask(subtask);

        final List<Task> tasks = taskManager.getTasks();
        final List<Epic> epics = taskManager.getEpics();
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertTrue(file.exists());
        assertEquals(3, tasks.size() + epics.size() + subtasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }
}

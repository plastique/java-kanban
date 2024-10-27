package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager(
                new InMemoryHistoryManager()
        );
    }

    @Test
    void taskAddedToStorage() {
        Task task = new Task(
                "Test addNewTask",
                "Test addNewTask description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );

        final Task savedTask = taskManager.createTask(task);
        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertNotNull(tasks, "Задачи не возвращаются.");

        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void epicAddedToStorage() {
        Epic epic = new Epic("Test addNewTask", "Test addNewTask description");

        final Epic savedEpic = taskManager.createEpic(epic);
        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        assertNotNull(epics, "Эпики не возвращаются.");

        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void subtaskAddedToStorage() {
        Epic epic = new Epic("Test addNewTask", "Test addNewTask description");
        final Epic savedEpic = taskManager.createEpic(epic);

        Subtask subtask = new Subtask(
                "Test addNewTask",
                "Test addNewTask description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 30),
                Duration.ofMinutes(35),
                epic.getId()
        );

        final Subtask savedSubtask = taskManager.createSubtask(subtask);
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertNotNull(subtasks, "Подзадачи не возвращаются.");

        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }
}
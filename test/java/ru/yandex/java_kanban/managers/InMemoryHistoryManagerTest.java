package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void addTaskToHistory() {
        Task task = new Task(
                "task",
                "description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "Нет задач в истории");
    }

    @Test
    void uniqueHistory() {
        Task task = new Task(
                "task",
                "description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );
        task.setId(1);
        Task task2 = new Task(
                "task2",
                "description2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 20),
                Duration.ofMinutes(20)
        );
        task2.setId(2);

        historyManager.add(task);
        historyManager.add(task2);
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");
        assertEquals(2, history.size(), "История не уникальна");
    }

    @Test
    void removeCorrectly() {
        Task task = new Task(
                "task",
                "description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );
        task.setId(1);
        Task task2 = new Task(
                "task2",
                "description2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 30),
                Duration.ofMinutes(20)
        );
        task2.setId(2);

        historyManager.add(task);
        historyManager.add(task2);
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "Размер истории некорректный");
    }
}

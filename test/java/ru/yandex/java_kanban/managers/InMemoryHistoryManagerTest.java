package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.models.Task;

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
        Task task = new Task("task", "description", TaskStatus.NEW);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "Нет задач в истории");
    }
}

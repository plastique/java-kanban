package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackendTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

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
        List<String> lines = Files.readAllLines(Paths.get(file.toString()));

        assertEquals(1, lines.size());
    }

    @Test
    void loadEmptyFileIsCorrect() {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> tasks = taskManager.getTasks();

        assertTrue(tasks.isEmpty());
    }
}

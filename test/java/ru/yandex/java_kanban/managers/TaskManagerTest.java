package ru.yandex.java_kanban.managers;

import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.exceptions.TaskIntersectionException;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager>{
    protected T taskManager;

    protected Task makeTask() {
        return new Task(
                "Test addNewTask",
                "Test addNewTask description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );
    }

    protected Epic makeEpic() {
        return new Epic("Test addNewEpic", "Test addNewEpic description");
    }

    protected Subtask makeSubtask(Epic epic) {
        return new Subtask(
                "Test addNewSubask",
                "Test addNewSubtask description",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 11, 0),
                Duration.ofMinutes(35),
                epic.getId()
        );
    }

    @Test
    void taskAdded() {
        Task task = makeTask();

        final Task savedTask = taskManager.createTask(task);
        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertNotNull(tasks, "Задачи не возвращаются.");

        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void taskUpdated() {
        Task task = taskManager.createTask(makeTask());
        String taskOriginName = task.getName();
        String taskNewName = "New task name";

        Task taskFromList = taskManager.getTaskById(task.getId());
        taskFromList.setName(taskNewName);
        taskManager.updateTask(taskFromList);

        Task updatedTask = taskManager.getTaskById(task.getId());

        assertNotEquals(taskNewName, taskOriginName);
        assertEquals(taskNewName, updatedTask.getName());
    }

    @Test
    void taskDeleted() {
        Task task = taskManager.createTask(makeTask());
        taskManager.deleteTaskById(task.getId());
        List<Task> tasks = taskManager.getTasks();

        assertEquals(0, tasks.size());
    }

    @Test
    void tasksDeleted() {
        taskManager.createTask(makeTask());
        taskManager.deleteTasks();
        List<Task> tasks = taskManager.getTasks();

        assertEquals(0, tasks.size());
    }

    @Test
    void epicAdded() {
        Epic epic = makeEpic();

        final Epic savedEpic = taskManager.createEpic(epic);
        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        assertNotNull(epics, "Эпики не возвращаются.");

        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void epicUpdated() {
        Epic epic = taskManager.createEpic(makeEpic());

        String epicOriginName = epic.getName();
        String epicNewName = "New epic name";

        Epic epicFromList = taskManager.getEpicById(epic.getId());
        epicFromList.setName(epicNewName);
        taskManager.updateEpic(epicFromList);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());

        assertNotEquals(epicNewName, epicOriginName);
        assertEquals(epicNewName, updatedEpic.getName());
    }

    @Test
    void epicDeleted() {
        Epic epic = taskManager.createEpic(makeEpic());

        taskManager.deleteEpicById(epic.getId());
        List<Epic> epics = taskManager.getEpics();

        assertEquals(0, epics.size());
    }

    @Test
    void episcDeleted() {
        Epic epic = taskManager.createEpic(makeEpic());
        taskManager.deleteEpics();
        List<Epic> epics = taskManager.getEpics();

        assertEquals(0, epics.size());
    }

    @Test
    void subtaskAdded() {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = makeSubtask(epic);

        final Subtask savedSubtask = taskManager.createSubtask(subtask);
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertNotNull(subtasks, "Подзадачи не возвращаются.");

        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void subtaskUpdated() {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = makeSubtask(epic);

        Subtask savedSubtask = taskManager.createSubtask(subtask);
        String subtaskOriginName = savedSubtask.getName();
        String subtaskNewName = "New subtask name";

        Subtask subtaskFromList = taskManager.getSubtaskById(savedSubtask.getId());
        subtaskFromList.setName(subtaskNewName);
        taskManager.updateSubtask(subtaskFromList);

        Subtask updatedSubtask = taskManager.getSubtaskById(savedSubtask.getId());

        assertNotEquals(subtaskNewName, subtaskOriginName);
        assertEquals(subtaskNewName, updatedSubtask.getName());
    }

    @Test
    void subtaskDeleted() {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        taskManager.deleteSubtaskById(subtask.getId());
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(0, subtasks.size());
    }

    @Test
    void subtasksDeleted() {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        taskManager.deleteSubtasks();
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(0, subtasks.size());
    }

    @Test
    void getSubtasksByEpic() {
        Epic epic1 = taskManager.createEpic(makeEpic());
        Epic epic2 = taskManager.createEpic(makeEpic());

        Subtask subtask1 = taskManager.createSubtask(makeSubtask(epic1));

        Subtask subtask2 = makeSubtask(epic2);
        subtask2.setStartTime(LocalDateTime.now().plusHours(2));
        subtask2 = taskManager.createSubtask(subtask2);

        Subtask subtask3 = makeSubtask(epic1);
        subtask3.setStartTime(LocalDateTime.now().plusHours(3));
        subtask3 = taskManager.createSubtask(subtask3);

        List<Subtask> subtasksEpic1 = taskManager.getEpicSubtasks(epic1.getId());
        List<Subtask> subtasksEpic2 = taskManager.getEpicSubtasks(epic2.getId());

        assertEquals(2, subtasksEpic1.size());
        assertEquals(1, subtasksEpic2.size());
    }

    @Test
    void getTaskHistory() {
        Task task1 = taskManager.createTask(makeTask());

        Task task2 = makeTask();
        task2.setStartTime(LocalDateTime.now().plusHours(2));
        taskManager.createTask(task2);

        Task task3 = makeTask();
        task3.setStartTime(LocalDateTime.now().plusHours(3));
        taskManager.createTask(task3);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task3.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(List.of(task1, task3), history);
    }

    @Test
    void getPrioritizedTasks() {
        Task task1 = taskManager.createTask(makeTask());
        Task task2 = taskManager.createTask(new Task(
                "task 2",
                "desc 2",
                TaskStatus.NEW,
                null,
                Duration.ofMinutes(0)
        ));

        List<Task> tasks = taskManager.getPrioritizedTasks();

        assertEquals(List.of(task1), tasks);
    }

    @Test
    void subtaskHasEpic() {
        Epic epic = taskManager.createEpic(makeEpic());
        Subtask subtask = taskManager.createSubtask(makeSubtask(epic));

        Epic savedEpic = taskManager.getEpicById(epic.getId());

        assertTrue(savedEpic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void epicHasStateNew() {
        Epic epic = taskManager.createEpic(makeEpic());

        taskManager.createSubtask(makeSubtask(epic));

        Subtask subtask2 = makeSubtask(epic);
        subtask2.setStartTime(LocalDateTime.now().plusHours(2));

        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicHasStateDone() {
        Epic epic = taskManager.createEpic(makeEpic());

        for (int i = 0; i <= 1; i++) {
            Subtask subtask = makeSubtask(epic);
            subtask.setStatus(TaskStatus.DONE);
            subtask.setStartTime(LocalDateTime.now().plusHours(2 + i));
            taskManager.createSubtask(subtask);
        }

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void epicHasStateInProgress() {
        Epic epic = taskManager.createEpic(makeEpic());

        Subtask subtask = makeSubtask(epic);
        subtask.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask);

        subtask = makeSubtask(epic);
        subtask.setStatus(TaskStatus.NEW);
        subtask.setStartTime(LocalDateTime.now().plusHours(2));
        taskManager.createSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicHasStateInProgressOne() {
        Epic epic = taskManager.createEpic(makeEpic());

        Subtask subtask = makeSubtask(epic);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.createSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void taskIntersectionOnEqualsTime() {
        Task task1 = makeTask();
        Task task2 = makeTask();

        task1.setStartTime(LocalDateTime.of(2024, 10, 29, 20, 0));
        task1.setDuration(Duration.ofMinutes(10));

        task2.setStartTime(LocalDateTime.of(2024, 10, 29, 20, 10));
        task2.setDuration(Duration.ofMinutes(10));

        assertThrowsExactly(TaskIntersectionException.class, () -> {
            taskManager.createTask(makeTask());
            taskManager.createTask(makeTask());
        });

        assertDoesNotThrow(() -> {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
        });
    }

    @Test
    void taskIntersectionOnTime() {
        Task task1 = makeTask();
        Task task2 = makeTask();
        Task task3 = makeTask();
        Task task4 = makeTask();

        task1.setStartTime(LocalDateTime.of(2024, 10, 29, 10, 30));
        task1.setDuration(Duration.ofMinutes(35));
        task1.setName("Task 1");

        task2.setStartTime(LocalDateTime.of(2024, 10, 29, 11, 0));
        task2.setDuration(Duration.ofMinutes(55));
        task2.setName("Task 2");

        task3.setStartTime(LocalDateTime.of(2024, 10, 29, 15, 0));
        task3.setDuration(Duration.ofMinutes(15));
        task3.setName("Task 3");

        task4.setStartTime(LocalDateTime.of(2024, 10, 29, 14, 0));
        task4.setDuration(Duration.ofMinutes(60));
        task4.setName("Task 4");

        assertThrowsExactly(TaskIntersectionException.class, () -> {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
            taskManager.createTask(task3);
            taskManager.createTask(task4);
        });
    }
}

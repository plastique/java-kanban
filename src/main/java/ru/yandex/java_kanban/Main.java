package ru.yandex.java_kanban;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.Managers;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault(
                Managers.getDefaultHistory()
        );

        Task task1 = taskManager.createTask(new Task(
                "Task 1",
                "description 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(45)
        ));
        Task task2 = taskManager.createTask(new Task(
                "Task 2",
                "description 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 11, 0),
                Duration.ofMinutes(60)
        ));

        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "des 1"));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "des 2"));

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 1",
                        "des 1",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 27, 12, 0),
                        Duration.ofMinutes(15),
                        epic1.getId()
                )
        );
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 2",
                        "des 2",
                        TaskStatus.IN_PROGRESS,
                        LocalDateTime.of(2024, 10, 27, 15, 0),
                        Duration.ofMinutes(20),
                        epic1.getId()
                )
        );
        Subtask subtask3 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 3",
                        "des 3",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 27, 15, 20),
                        Duration.ofMinutes(30),
                        epic2.getId()
                )
        );
        Subtask subtask4 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 4",
                        "des 4",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 27, 17, 0),
                        Duration.ofMinutes(40),
                        epic2.getId()
                )
        );

        printAllTasks(taskManager);

        task1.setStatus(TaskStatus.IN_PROGRESS);
        task1 = taskManager.updateTask(task1);

        taskManager.deleteEpicById(epic1.getId());

        subtask3.setStatus(TaskStatus.DONE);
        subtask3 = taskManager.updateSubtask(subtask3);

        taskManager.deleteSubtaskById(subtask4.getId());

        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("\nЗадачи");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nПодзадачи");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }
    }
}

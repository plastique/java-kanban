package ru.yandex.java_kanban;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.Managers;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault(
                Managers.getDefaultHistory()
        );

        Task task1 = taskManager.createTask(new Task("Task 1", "description 1", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "description 2", TaskStatus.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "des 1"));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "des 2"));

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Subtask 1", "des 1", TaskStatus.NEW, epic1)
        );
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Subtask 2", "des 2", TaskStatus.IN_PROGRESS, epic1)
        );
        Subtask subtask3 = taskManager.createSubtask(
                new Subtask("Subtask 3", "des 3", TaskStatus.NEW, epic2)
        );
        Subtask subtask4 = taskManager.createSubtask(
                new Subtask("Subtask 4", "des 4", TaskStatus.NEW, epic2)
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

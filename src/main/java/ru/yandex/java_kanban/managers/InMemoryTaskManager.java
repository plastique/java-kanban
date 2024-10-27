package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.contracts.HistoryManager;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int increment = 0;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
        prioritizedTasks = new TreeSet<>((taskA, taskB) -> {
            if (taskA.getStartTime() == null && taskB.getStartTime() == null) {
                return 0;
            } else if (taskA.getStartTime() == null) {
                return -1;
            } else if (taskB.getStartTime() == null) {
                return 1;
            }

            return taskA.getStartTime().compareTo(taskB.getStartTime());
        });
    }

    // Tasks
    @Override
    public Task createTask(Task task) {
        checkTaskIntersection(task);

        task.setId(getNewIncrement());
        tasks.put(task.getId(), task);
        addPrioritizedTask(task);

        return task;
    }

    @Override
    public Task updateTask(Task task) {
        checkTaskIntersection(task);

        tasks.put(task.getId(), task);
        addPrioritizedTask(task);

        return task;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);

        historyManager.add(task);

        return task;
    }

    @Override
    public void deleteTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            deletePrioritizedTask(task);
        }
        tasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        deletePrioritizedTask(tasks.get(id));
        historyManager.remove(id);
        tasks.remove(id);
    }

    // Epics
    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNewIncrement());
        epics.put(epic.getId(), epic);

        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());

        return oldEpic;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);

        historyManager.add(epic);

        return epic;
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> res = new ArrayList<>();

        Epic epic = epics.get(epicId);

        if (epic == null) {
            return res;
        }

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                res.add(subtask);
            }
        }

        return res;
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            deletePrioritizedTask(subtask);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic == null) {
            return;
        }

        historyManager.remove(id);

        for (Integer subtaskId : epic.getSubtaskIds()) {
            deletePrioritizedTask(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    // Subtasks
    @Override
    public Subtask createSubtask(Subtask subtask) {
        checkTaskIntersection(subtask);

        subtask.setId(getNewIncrement());
        subtasks.put(subtask.getId(), subtask);

        addPrioritizedTask(subtask);
        actualEpicSubtasks(subtask);

        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        checkTaskIntersection(subtask);

        Subtask currentSubtask = subtasks.get(subtask.getId());
        int currentEpicId = currentSubtask.getEpicId();

        currentSubtask.setName(subtask.getName());
        currentSubtask.setDescription(subtask.getDescription());
        currentSubtask.setStatus(subtask.getStatus());
        currentSubtask.setStartTime(subtask.getStartTime());
        currentSubtask.setDuration(subtask.getDuration());

        addPrioritizedTask(currentSubtask);
        Epic currentEpic = epics.get(currentEpicId);
        actualEpicData(currentEpic);

        return currentSubtask;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        historyManager.add(subtask);

        return subtask;
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            deletePrioritizedTask(subtask);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteSubtasks();
            actualEpicData(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask == null) {
            return;
        }

        historyManager.remove(id);
        deletePrioritizedTask(subtask);

        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return;
        }

        epic.getSubtaskIds().remove((Integer)subtask.getId());

        actualEpicData(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    // Other
    private int getNewIncrement() {
        return ++increment;
    }

    protected void actualEpicSubtasks(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return;
        }

        epic.addSubtask(subtask);
        actualEpicData(epic);
    }

    private void actualEpicData(Epic epic) {
        actualEpicStatus(epic);
        actualEpicTime(epic);
    }

    private void actualEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds == null || subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        int cnt = subtaskIds.size();
        int isDone = 0;
        int isNew = 0;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);

            if (subtask == null) {
                continue;
            }

            if (subtask.getStatus() == TaskStatus.NEW) {
                isNew++;
            } else if (subtask.getStatus() == TaskStatus.DONE) {
                isDone++;
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        if (cnt == isDone) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        if (cnt == isNew) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void actualEpicTime(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds == null || subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        epic.setStartTime(null);
        epic.setDuration(Duration.ZERO);

        if (subtaskIds.isEmpty()) {
            return;
        }

        subtaskIds.forEach(subtaskId -> {
            Subtask subtask = subtasks.get(subtaskId);

            if (subtask == null) {
                return;
            }

            LocalDateTime startTime = subtask.getStartTime();
            LocalDateTime endTime = subtask.getEndTime();

            if (epic.getStartTime() == null || epic.getStartTime().isAfter(startTime)) {
                epic.setStartTime(startTime);
            }

            if (epic.getDuration().isZero() || epic.getEndTime().isBefore(endTime)) {
                epic.setDuration(Duration.between(epic.getStartTime(), subtask.getEndTime()));
                epic.setEndTime(endTime);
            }
        });
    }

    private void addPrioritizedTask(Task task) {
        if (task == null) {
            return;
        }

        LocalDateTime startTime = task.getStartTime();

        if (startTime == null) {
            return;
        }

        prioritizedTasks.add(task);
    }

    private void deletePrioritizedTask(Task task) {
        prioritizedTasks.remove(task);
    }

    private void checkTaskIntersection(Task task) throws RuntimeException {
        if (task == null) {
            return;
        }

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();

        if (startTime == null) {
            return;
        }

        prioritizedTasks.forEach(prioritizedTask -> {
            if ((prioritizedTask.getStartTime().isBefore(startTime)
                        && prioritizedTask.getEndTime().isAfter(startTime))
                || (prioritizedTask.getStartTime().isBefore(endTime)
                        && prioritizedTask.getEndTime().isAfter(endTime))
            ) {
                throw new RuntimeException(String.format(
                        "Найдено пересечение в задачах: %s и %s",
                        task.getName(),
                        prioritizedTask.getName()
                ));
            }
        });
    }
}

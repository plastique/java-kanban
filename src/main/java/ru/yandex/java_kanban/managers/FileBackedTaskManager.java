package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.enums.TaskType;
import ru.yandex.java_kanban.exceptions.ManagerLoadException;
import ru.yandex.java_kanban.exceptions.ManagerSaveException;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String FILE_HEADER = "id,type,name,status,description,epic,startTime,duration";

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager self = new FileBackedTaskManager(file);
        self.load();

        return self;
    }

    public void save() throws ManagerSaveException {
        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        ) {
            bw.write(FILE_HEADER + "\n");

            for (Task task : getTasks()) {
                bw.write(toString(task) + "\n");
            }
            for (Epic task : getEpics()) {
                bw.write(toString(task) + "\n");
            }
            for (Subtask task : getSubtasks()) {
                bw.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить список задач");
        }
    }

    // Tasks
    @Override
    public Task createTask(Task task) throws ManagerSaveException {
        task = super.createTask(task);
        save();

        return task;
    }

    @Override
    public Task updateTask(Task task) throws ManagerSaveException {
        task = super.updateTask(task);
        save();

        return task;
    }

    @Override
    public void deleteTaskById(int id) throws ManagerSaveException {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteTasks() throws ManagerSaveException {
        super.deleteTasks();
        save();
    }

    // Epics
    @Override
    public Epic createEpic(Epic epic) throws ManagerSaveException {
        epic = super.createEpic(epic);
        save();

        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) throws ManagerSaveException {
        epic = super.updateEpic(epic);
        save();

        return epic;
    }

    @Override
    public void deleteEpicById(int id) throws ManagerSaveException {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    // Subtasks
    @Override
    public Subtask createSubtask(Subtask subtask) throws ManagerSaveException {
        subtask = super.createSubtask(subtask);
        save();

        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) throws ManagerSaveException {
        subtask = super.updateSubtask(subtask);
        save();

        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) throws ManagerSaveException {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteSubtasks() throws ManagerSaveException {
        super.deleteSubtasks();
        save();
    }

    // Other
    private void load() throws ManagerLoadException {
        try (
                BufferedReader br = new BufferedReader(new FileReader(file));
        ) {
            while (br.ready()) {
                String line = br.readLine();

                if (line.isBlank()) {
                    break;
                }

                Task task = fromString(line);

                if (task == null) {
                    continue;
                }

                switch (task.getType()) {
                    case EPIC:
                        epics.put(task.getId(), (Epic) task);
                        break;

                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        subtasks.put(task.getId(), subtask);
                        addPrioritizedTask(subtask);
                        actualEpicSubtasks(subtask);
                        break;

                    default:
                        tasks.put(task.getId(), task);
                        addPrioritizedTask(task);
                }

                increment = Math.max(task.getId(), increment);
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Не удалось загрузить файл");
        }
    }

    private String toString(Task task) {
        List<String> fields = new ArrayList<>();

        fields.add("" + task.getId());
        fields.add(task.getType().toString());
        fields.add(task.getName());
        fields.add(task.getStatus().toString());
        fields.add(task.getDescription());

        String epicId = "";

        if (task.getType() == TaskType.SUBTASK) {
            epicId += ((Subtask) task).getEpicId();
        }

        fields.add(epicId);
        fields.add(
                task.getStartTime() == null
                    ? null
                    : task.getStartTime().toString()
        );
        fields.add(
                task.getDuration() == null
                    ? null
                    : String.valueOf(task.getDuration().toMinutes())
        );

        return String.join(",", fields);
    }

    private Task fromString(String taskString) {
        String[] fields = taskString.split(",");

        int id = Integer.parseInt(fields[0]);
        Task task = null;

        if (id == 0) {
            return task;
        }

        TaskType type = TaskType.valueOf(fields[1]);
        TaskStatus status = TaskStatus.valueOf(fields[4]);
        String name = fields[2];
        String description = fields[3];
        LocalDateTime startTime = fields[6].equals("null")
                ? LocalDateTime.parse(fields[6])
                : null;
        Duration duration = fields[7].equals("null")
            ? null
            : Duration.ofMinutes(Long.parseLong(fields[7]));

        task = switch (type) {
            case EPIC -> new Epic(name, description);
            case SUBTASK -> new Subtask(name, description, status, startTime, duration, Integer.parseInt(fields[5]));
            default -> new Task(name, description, status, startTime, duration);
        };

        task.setId(id);

        return task;
    }
}

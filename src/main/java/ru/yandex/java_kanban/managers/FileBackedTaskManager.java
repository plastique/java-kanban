package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.enums.TaskType;
import ru.yandex.java_kanban.exceptions.ManagerLoadException;
import ru.yandex.java_kanban.exceptions.ManagerSaveException;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

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
            bw.write("id,type,name,status,description,epic\n");

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
                        actualEpicSubtasks(subtask);
                        break;

                    default:
                        tasks.put(task.getId(), task);
                }

                increment = task.getId();
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

        if (task instanceof Subtask) {
            epicId += ((Subtask) task).getEpicId();
        }

        fields.add(epicId);

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

        switch (type) {
            case EPIC:
                task = new Epic(name, description);
                break;

            case SUBTASK:
                Epic tmpEpic = new Epic("", "");
                tmpEpic.setId(Integer.parseInt(fields[5]));

                task = new Subtask(name, description, status, tmpEpic);
                break;

            default:
                task = new Task(name, description, status);
        }

        task.setId(id);

        return task;
    }
}

package ru.yandex.java_kanban.models;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW, null, null);
        subtaskIds = new ArrayList<>();
        endTime = null;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void deleteSubtasks() {
        subtaskIds.clear();
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Epic epic = (Epic) obj;

        return this.getId() == epic.getId();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", description='" + getDescription() + '\'' +
                ", subtaskIds=" + subtaskIds +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                '}';
    }
}

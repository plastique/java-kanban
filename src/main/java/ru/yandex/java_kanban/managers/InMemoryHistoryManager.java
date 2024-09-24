package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.managers.contracts.HistoryManager;
import ru.yandex.java_kanban.models.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    public static class HistoryList {
        private Node first;
        private Node last;
        private final Map<Integer, Node> map = new HashMap<>();

        private void linkLast(Task task) {
            Node newNode = new Node(task, last, null);

            if (last == null) {
                first = newNode;
            } else {
                last.setNext(newNode);
            }

            last = newNode;
        }

        private void removeNode(Node node) {
            if (node == null) {
                return;
            }

            Node prev = node.getPrev();
            Node next = node.getNext();

            map.remove(node.getTask().getId());

            if (node == first) {
                first = node.getNext();
            } else if (node == last) {
                last = node.getPrev();
            }

            if (prev != null) {
                prev.setNext(next);
            }

            if (next != null) {
                next.setPrev(prev);
            }
        }

        private Node getNode(int id) {
            return map.get(id);
        }

        private List<Task> getTasks() {
            List<Task> res = new ArrayList<>();
            Node el = first;

            while (el != null) {
                res.add(el.getTask());
                el = el.getNext();
            }

            return res;
        }
    }

    private final HistoryList history = new HistoryList();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        history.linkLast(task);
    }

    @Override
    public void remove(int id) {
        history.removeNode(history.getNode(id));
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }
}

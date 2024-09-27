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
            if (task == null) {
                return;
            }

            Node newNode = new Node(task, null, null);

            if (map.containsKey(task.getId())) {
                removeNode(map.get(task.getId()));
            }

            if (first == null) {
                first = newNode;
                last = newNode;
            } else {
                newNode.setPrev(last);
                last.setNext(newNode);
                last = newNode;
            }

            map.put(task.getId(), last);
        }

        private void removeNode(Node node) {
            if (node == null) {
                return;
            }

            map.remove(node.getTask().getId());

            Node prev = node.getPrev();
            Node next = node.getNext();

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

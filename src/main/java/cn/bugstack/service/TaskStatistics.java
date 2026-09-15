package cn.bugstack.service;

import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class TaskStatistics {

    /**
     * 按状态分组
     */
    public Map<TaskStatus, List<Task>> groupByStatus(List<Task> tasks) {
        return tasks.stream()
                .collect(groupingBy(Task::getStatus));
    }

    /**
     * 按优先级分组
     */
    public Map<TaskPriority, List<Task>> groupByPriority(List<Task> tasks) {
        return tasks.stream()
                .collect(groupingBy(Task::getPriority));
    }

    /**
     * 查询逾期且尚未结束的任务
     */
    public List<Task> findOverdueTasks(List<Task> tasks, LocalDateTime now) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .filter(task ->
                        task.getStatus() != TaskStatus.COMPLETED
                                && task.getStatus() != TaskStatus.CANCELLED)
                .toList();
    }

    /**
     * 查询每个项目的任务数量
     */
    public Map<UUID, Long> countTasksByProject(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getProjectId() != null)
                .collect(groupingBy(
                        Task::getProjectId,
                        counting()
                ));
    }

    /**
     * 按负责人统计任务数量
     */
    public Map<UUID, Long> countTasksByUser(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getAssigneeId() != null)
                .collect(groupingBy(
                        Task::getAssigneeId,
                        counting()
                ));
    }

    /**
     * 找到截止时间最早的任务
     */
    public Optional<Task> findEarliestDueTask(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null)
                .min(Comparator.comparing(Task::getDueDate));
    }
}

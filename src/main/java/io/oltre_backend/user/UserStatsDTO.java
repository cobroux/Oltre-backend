package io.oltre_backend.user;

public class UserStatsDTO {

    private int sportSessionsLast30Days;
    private double sportDistanceKmLast30Days;
    private int expensesThisMonth;
    private int tasksInProgress;

    public UserStatsDTO() {}

    public UserStatsDTO(int sportSessionsLast30Days, double sportDistanceKmLast30Days,
                         int expensesThisMonth, int tasksInProgress) {
        this.sportSessionsLast30Days = sportSessionsLast30Days;
        this.sportDistanceKmLast30Days = sportDistanceKmLast30Days;
        this.expensesThisMonth = expensesThisMonth;
        this.tasksInProgress = tasksInProgress;
    }

    public int getSportSessionsLast30Days() {
        return sportSessionsLast30Days;
    }

    public void setSportSessionsLast30Days(int sportSessionsLast30Days) {
        this.sportSessionsLast30Days = sportSessionsLast30Days;
    }

    public double getSportDistanceKmLast30Days() {
        return sportDistanceKmLast30Days;
    }

    public void setSportDistanceKmLast30Days(double sportDistanceKmLast30Days) {
        this.sportDistanceKmLast30Days = sportDistanceKmLast30Days;
    }

    public int getExpensesThisMonth() {
        return expensesThisMonth;
    }

    public void setExpensesThisMonth(int expensesThisMonth) {
        this.expensesThisMonth = expensesThisMonth;
    }

    public int getTasksInProgress() {
        return tasksInProgress;
    }

    public void setTasksInProgress(int tasksInProgress) {
        this.tasksInProgress = tasksInProgress;
    }
}

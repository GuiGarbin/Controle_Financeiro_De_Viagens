package org.example.trip;

import org.example.trip.expenses.Expenses;
import org.example.users.User;

import java.util.Date;
import java.util.List;

public class Trips {
    private int id;
    private String name;
    private String description;
    private String destination;
    private String currency;
    private boolean status;
    private List<Integer> membersId;
    private List<Expenses> listExpenses;
    private Date startDate;
    private Date endDate;
    private User createdBy;
    private Date createdAt;
    private Date updatedAt;
}

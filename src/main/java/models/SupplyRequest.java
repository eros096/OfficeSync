package models;

import java.time.LocalDate;

public class SupplyRequest {
    private final int requestId;
    private final String requester;
    private final int departmentId;
    private final String department;
    private final String supplyName;
    private final int quantity;
    private final LocalDate requestDate;
    private final String status;

    public SupplyRequest(int requestId, String requester, int departmentId, String department, String supplyName, int quantity, LocalDate requestDate, String status) {
        this.requestId = requestId;
        this.requester = requester;
        this.departmentId = departmentId;
        this.department = department;
        this.supplyName = supplyName;
        this.quantity = quantity;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getRequester() {
        return requester;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public String getSupplyName() {
        return supplyName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public String getStatus() {
        return status;
    }
}

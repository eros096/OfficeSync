package models;

public class User {
    public enum Role {
        ADMIN("Admin"),
        DEPARTMENT_HEAD("Head"),
        EMPLOYEE("Employee");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final int id;
    private final String fullName;
    private final String email;
    private final Role role;
    private final int departmentId;
    private final String department;

    public User(int id, String fullName, String email, Role role, int departmentId, String department) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.departmentId = departmentId;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public static Role roleFromDisplayName(String value) {
        if ("Department Head".equalsIgnoreCase(value)) {
            return Role.DEPARTMENT_HEAD;
        }
        for (Role role : Role.values()) {
            if (role.getDisplayName().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}

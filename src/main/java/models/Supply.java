package models;

public class Supply {
    private final int id;
    private final String name;
    private final String category;
    private final int stock;
    private final int reorderLevel;
    private final boolean available;

    public Supply(int id, String name, String category, int stock, int reorderLevel, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.reorderLevel = reorderLevel;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getAvailabilityStatus() {
        return available ? "Active" : "Not Active";
    }

    public String getStatus() {
        if (!available) {
            return "Out of Stock";
        }
        return stock <= reorderLevel ? "Low Stock" : "Available";
    }
}

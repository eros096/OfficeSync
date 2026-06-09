package models;

public class Supply {
    private final int id;
    private final String name;
    private final String category;
    private final int stock;
    private final int reorderLevel;

    public Supply(int id, String name, String category, int stock, int reorderLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.reorderLevel = reorderLevel;
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

    public String getStatus() {
        return stock <= reorderLevel ? "Low Stock" : "Available";
    }
}

package bikram.model;

import java.sql.Date;
import bikram.util.DateManager;
import bikram.util.IdGenerator;
import bikram.util.barcode.BarcodeGenerator;
import com.google.zxing.BarcodeFormat;

public class Product {

    private String id;
    private String name;
    private double price;      // Selling price per unit
    private double cost;       // Cost per unit (NEW)
    private double discount;
    private int quantity;
    private String description;
    private String category;
    private String supplier;
    private Date created_at;
    private Date expire_date;

    // Default constructor
    public Product() {}

    // Constructor with cost included
    public Product(String name, double price, double cost, int quantity, String description) {
        this.id = IdGenerator.idGenerate("PRD",4);
        this.name = name;
        this.price = price;
        this.cost = cost;  // set cost
        this.quantity = quantity;
        this.description = description;
        this.created_at = Date.valueOf(DateManager.getCreated_at());
        this.expire_date = Date.valueOf(DateManager.getExpiryDate());
        BarcodeGenerator.generateBarcode(this.getId(), this.getName(), BarcodeFormat.CODE_128, 300, 100);
    }

    public Product(String name, double price, int quantity, String desc) {
        this.name = name;
        this.price=price;
        this.quantity=quantity;
        this.description = desc;
    }


    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getExpire_date() { return expire_date; }
    public void setExpire_date(Date expire_date) { this.expire_date = expire_date; }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, cost=%.2f, quantity=%d}", id, name, price, cost, quantity);
    }
}

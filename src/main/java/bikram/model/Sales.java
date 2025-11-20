package bikram.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ⚡ Advanced Sales Model
 * Designed for easy database save/load without ORM.
 */
public class Sales {

    private int id;
    private String productId;
    private String name;
    private int quantity;
    private double purchasePrice;
    private double salePrice;
    private double discount; // percentage
    private double finalAmount;
    private double profit;
    private double loss;
    private double paidAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---------- Constructors ----------
    public Sales() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Sales(int id, String productId, String name, int quantity,
                 double purchasePrice, double salePrice, double discount,
                 LocalDateTime createdAt, LocalDateTime updatedAt,
                 String paymentMethod, double paidAmount) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.discount = discount;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        recalc();
    }

    // ---------- Logic ----------
    private void recalc() {
        double discounted = salePrice - (salePrice * (discount / 100.0));
        this.finalAmount = discounted * quantity;

        if (salePrice > purchasePrice) {
            profit = (salePrice - purchasePrice) * quantity;
            loss = 0;
        } else if (salePrice < purchasePrice) {
            loss = (purchasePrice - salePrice) * quantity;
            profit = 0;
        } else {
            profit = loss = 0;
        }
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------- Builder Pattern ----------
    public static class Builder {
        private final Sales s = new Sales();

        public Builder id(int id) { s.id = id; return this; }
        public Builder productId(String id) { s.productId = id; return this; }
        public Builder name(String n) { s.name = n; return this; }
        public Builder quantity(int q) { s.quantity = q; return this; }
        public Builder purchasePrice(double p) { s.purchasePrice = p; return this; }
        public Builder salePrice(double p) { s.salePrice = p; return this; }
        public Builder discount(double d) { s.discount = d; return this; }
        public Builder paymentMethod(String method) { s.paymentMethod = method; return this; }
        public Builder paidAmount(double amt) { s.paidAmount = amt; return this; }
        public Builder createdAt(LocalDateTime t) { s.createdAt = t; return this; }
        public Builder updatedAt(LocalDateTime t) { s.updatedAt = t; return this; }

        public Sales build() { s.recalc(); return s; }
    }

    // ---------- Database Conversion ----------
    public static Sales fromResultSet(ResultSet rs) throws SQLException {
        Sales s = new Sales();
        s.id = rs.getInt("id");
        s.productId = rs.getString("product_id");
        s.name = rs.getString("name");
        s.quantity = rs.getInt("quantity");
        s.purchasePrice = rs.getDouble("purchase_price");
        s.salePrice = rs.getDouble("sale_price");
        s.discount = rs.getDouble("discount");
        s.paymentMethod = rs.getString("payment_method");
        s.paidAmount = rs.getDouble("paid_amount");
        s.createdAt = LocalDateTime.parse(rs.getString("created_at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        s.updatedAt = LocalDateTime.parse(rs.getString("updated_at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        s.recalc();
        return s;
    }

    public String toSQLValues() {
        return String.format(
                "('%s','%s',%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,'%.2f','%s','%s','%s')",
                escape(productId), escape(name), quantity, purchasePrice, salePrice,
                discount, finalAmount, profit, loss, paidAmount, escape(paymentMethod),
                createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    // ---------- Getters & Setters ----------
    public int getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getSalePrice() { return salePrice; }
    public double getDiscount() { return discount; }
    public double getFinalAmount() { return finalAmount; }
    public double getProfit() { return profit; }
    public double getLoss() { return loss; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getPaidAmount() { return paidAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setQuantity(int q) { this.quantity = q; recalc(); }
    public void setSalePrice(double s) { this.salePrice = s; recalc(); }
    public void setPurchasePrice(double p) { this.purchasePrice = p; recalc(); }
    public void setDiscount(double d) { this.discount = d; recalc(); }
    public void setPaymentMethod(String m) { this.paymentMethod = m; }
    public void setPaidAmount(double amt) { this.paidAmount = amt; }

    // ---------- Utility ----------
    @Override
    public String toString() {
        return String.format("Sales{id=%d, product='%s', qty=%d, sale=%.2f, final=%.2f, pay='%s', paid=%.2f}",
                id, name, quantity, salePrice, finalAmount, paymentMethod, paidAmount);
    }
}

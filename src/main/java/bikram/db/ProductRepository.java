package bikram.db;

import bikram.model.Product;
import java.util.List;
import java.util.Map;

public interface ProductRepository {
    void createTable();
    void addProduct(Product product);

    double getTotalCost();

    double getProfitByProduct(String productName);

    Map<String, Double> getMonthlyProfit();

    Map<String, Integer> getMonthlySalesTrend();

    List<Product> getAllProducts();
    Product getProductById(String id);
    List<Product> getProductByName(String name);
    void updateProduct(Product product);
    boolean deleteProductById(String id);
    int deleteProductByName(String name);
    double getTotalProductValue();
    double getAveragePrice();

    Map<String, Integer> getCategorySales();
}

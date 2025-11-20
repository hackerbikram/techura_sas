package bikram.db;

import bikram.model.Sales;
import bikram.views.page.TechuraDashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface Salesrepository {
    public void createTable();
    public boolean saveSales(Sales s);
    public double getTotalRevenue();
    public double getTotalProfit();
    public double getTotalLoss();
    public int getTotalQuantitySold();
    public String getMostSoldProduct();
    public double getAverageSalesPerDay();
    public double getSalesByDateRange(LocalDate start, LocalDate end);
    public List<String> getTop5Products();
    public void printSalesSummary();
    public Map<String, Integer> getMonthlyNewUsers();
    public int countSales();
    public Map<String, Double> getMonthlyRevenue();
    public List<TechuraDashboard.Activity> getRecentActivities();

    Map<String, Double> getMonthlyProfit();

    int countSalesByProduct(String name);

    Map<String, Integer> getMonthlySalesTrend();

    double getProfitByProduct(String product);

    double getTotalCost();

    Map<String, Double> getDailySales();
}

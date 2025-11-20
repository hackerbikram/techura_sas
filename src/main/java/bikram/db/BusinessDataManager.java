package bikram.db;

import bikram.model.Business;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BusinessDataManager {

    private static final String FILE_PATH = "data/business.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public BusinessDataManager(){
        
    }
    // 🔹 Save business to JSON
    public static void saveBusiness(Business business) throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(business, writer);
        }
    }

    // 🔹 Load business from JSON (if not exist, create new empty one)
    public static Business loadBusiness() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            Business emptyBusiness = new Business("Techura", "Khadka Dhurba Bikram", "kobe higasi nadaku");
            saveBusiness(emptyBusiness);
            return emptyBusiness;
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, Business.class);
        }
    }

    // 🔹 Update specific fields dynamically (profit, loss, etc.)
    public static void updateProfit(double amount) throws IOException {
        Business business = loadBusiness();
        business.setProfit(amount);
        saveBusiness(business);
    }

    public static void updateLoss(double amount) throws IOException {
        Business business = loadBusiness();
        business.setLoss(amount);
        saveBusiness(business);
    }

    public static void updateBalance(double amount) throws IOException {
        Business business = loadBusiness();
        business.setBalance(amount);
        saveBusiness(business);
    }

    public static void updateBudget(double amount) throws IOException {
        Business business = loadBusiness();
        business.setBudgect("update", amount);
        saveBusiness(business);
    }
    public static void makeProfit(double amount) throws IOException {
        Business business = loadBusiness();
        double newProfit = business.getProfit()+amount;
        business.setProfit(newProfit);
        saveBusiness(business);
    }
    public static void makeLoss(double amount) throws IOException {
        Business business = loadBusiness();
        double newloss = business.getLoss()-amount;
        business.setLoss(newloss);
        saveBusiness(business);
    }
    public static void addBalence(double amount) throws IOException {
        Business business = loadBusiness();
        double newBalence = business.getBalance()+amount;
        business.setBalance(newBalence);
        saveBusiness(business);
    }
}

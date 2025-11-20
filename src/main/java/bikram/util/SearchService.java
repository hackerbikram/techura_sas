package bikram.util;

import java.util.*;
import java.util.stream.Collectors;
import bikram.model.*;

public class SearchService {

    private List<Product> products;
    private List<User> users;
    private List<Sales> sales;

    public SearchService(List<Product> products, List<User> users) {
        this.products = products;
        this.users = users;
        this.sales = sales;
    }

    public List<String> search(String query) {
        query = query.toLowerCase();

        List<String> results = new ArrayList<>();

        // 🛒 Product search
        if (products != null) {
            String finalQuery1 = query;
            results.addAll(products.stream()
                    .filter(p -> p.getId().toLowerCase().contains(finalQuery1) ||
                            p.getName().toLowerCase().contains(finalQuery1) ||
                            p.getCategory().toLowerCase().contains(finalQuery1))
                    .map(p -> "🛒 Product → " + p.getName() + " (ID: " + p.getId() + ")")
                    .collect(Collectors.toList()));
        }

        // 👤 User/Employee search
        if (users != null) {
            String finalQuery = query;
            results.addAll(users.stream()
                    .filter(u -> u.getId().toLowerCase().contains(finalQuery) ||
                            u.getFullName().toLowerCase().contains(finalQuery) ||
                            u.getEmail().toLowerCase().contains(finalQuery))
                    .map(u -> "👤 Employee → " + u.getFullName() + " (ID: " + u.getId() + ")")
                    .collect(Collectors.toList()));
        }


        if (results.isEmpty()) {
            results.add("⚠️ No results found for: " + query);
        }

        return results;
    }
}

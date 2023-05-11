package DataAccess;
import BusinessLayer.Category;
import BusinessLayer.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//The idea here is that I return categories in an empty form (only name and ID number) without the list of products of the category.
//Later we will have to manipulate the DB to bring the products that are under the category.
public class CategoryDaoImpl implements CategoryDao {
    private Connection connection;
    private Map<Integer, Category> categoryMapFromDB;

    public CategoryDaoImpl() throws SQLException {
        connection = DBConnector.connect();
        categoryMapFromDB = new HashMap<>();
    }

    public Map<Integer, Category> getCategoryMapFromDB() {
        return categoryMapFromDB;
    }

    @Override
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        PreparedStatement statement2 = null;
        ResultSet rs2 = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Categories");
            rs = statement.executeQuery();
            while (rs.next()) {
                Category category = null;
                int currCategoryID = rs.getInt("CategoryID");
                if (!categoryMapFromDB.containsKey(currCategoryID)) {
                    category = new Category(currCategoryID, rs.getString("CategoryName"));
                    categories.add(category);
                    categoryMapFromDB.put(currCategoryID, category);
                }
                else
                {
                    categories.add(categoryMapFromDB.get(currCategoryID));
                }
            }
            return categories;
        }
        catch (Exception e) {
            System.out.println("Error while getting all categories: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {rs.close();}
            if (statement != null) {statement.close();}
        }
    }

    @Override
    public Category getCategoryByID(int categoryID) throws SQLException {
        if (categoryMapFromDB.containsKey(categoryID)) {
            return categoryMapFromDB.get(categoryID);
        }
        PreparedStatement statement = null;
        ResultSet rs = null;
        Category category = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Categories WHERE CategoryID = ?");
            statement.setInt(1, categoryID);
            rs = statement.executeQuery();
            if (rs.next()) {
                category = new Category(rs.getInt("CategoryID"), rs.getString("CategoryName"));
                categoryMapFromDB.put(category.getCategoryID(), category);
            }
            return category;
        } catch (Exception e) {
            System.out.println("Error while getting category: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {statement.close();}
            if (rs != null) {rs.close();}
        }
    }

    @Override
    public Category addCategory(String categoryName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Category category;
        try {
            statement = connection.prepareStatement("INSERT INTO Categories (CategoryName) VALUES(?)");
            statement.setString(1, categoryName);
            statement.executeUpdate();
            rs = connection.createStatement().executeQuery("SELECT MAX(CategoryID) FROM Categories");
            int last_ID = rs.getInt(1);
            category = new Category(last_ID, categoryName);
            categoryMapFromDB.put(category.getCategoryID(), category);
            return category;
        } catch (Exception e) {
            System.out.println("Error while trying to add new category: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {statement.close();}
            if (rs != null) {rs.close();}
        }

    }

    public Category updateCategoryName(int categoryID, String categoryNewName) throws SQLException {
        Category category;
        PreparedStatement statement = null;
        try {
            if (categoryMapFromDB.containsKey(categoryID))
            {
                category = categoryMapFromDB.get(categoryID);
            }
            else {category = getCategoryByID(categoryID);}
            category.setCategoryName(categoryNewName);
            statement = connection.prepareStatement("UPDATE Categories SET CategoryName = ? WHERE CategoryID = ?");
            statement.setString(1, categoryNewName);
            statement.setInt(2, categoryID);
            statement.executeUpdate();
            categoryMapFromDB.put(category.getCategoryID(), category);
            return category;
        } catch (Exception e) {
            System.out.println("Error while trying to rename category: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {statement.close();}
        }

    }
}
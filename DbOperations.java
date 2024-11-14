package com.work.cookbook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import com.work.cookbook.model.Comment;
import com.work.cookbook.model.Message;
import com.work.cookbook.model.Ingredient;
import com.work.cookbook.model.Recipe;
import com.work.cookbook.model.User;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DbOperations {

    private static final DbConnector dbConnector = new DbConnector();

    // **************************
    // Here we have SQL for login system
    // **************************

    public boolean shareRecipeWithUser(Recipe recipe, User user, String message) {
        String sql = "INSERT INTO Messages (Recipe_ID, sender_ID, reciever id, Message_ID) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipe.getRecipeId());
            pstmt.setInt(2, user.getUserID());
            pstmt.setString(3, message);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) {
        try (Connection conn = dbConnector.getConnection()) {
            String query = "SELECT Password FROM User WHERE Username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String dbPassword = resultSet.getString("Password");
                        return password.equals(dbPassword);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    // display name of user
    public String fetchDisplayName(String username) {
        String displayName = null;
        try (Connection conn = dbConnector.getConnection()) {
            String query = "SELECT Display_Name FROM User WHERE Username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        displayName = resultSet.getString("Display_Name");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return displayName;
    }

    // check admin using iadmin from db
    public boolean checkAdminStatus(String username) {
        boolean isAdmin = false;
        try (Connection conn = dbConnector.getConnection()) {
            String query = "SELECT isadmin FROM User WHERE Username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isAdmin = resultSet.getBoolean("isadmin");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isAdmin;
    }

    public ObservableList<User> getUserListFromDB() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        DbConnector dbConnector = new DbConnector();

        // SQL query to fetch all users
        String sql = "SELECT * FROM User";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Create a new User object and add it to the list
                User user = new User(
                        rs.getInt("User_ID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Display_Name"),
                        rs.getBoolean("isadmin"));
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Database Error", "Could not load user data from the database.");
        }

        return userList;
    }

    // get list of full names of all users
    public List<String> getAllUserDisplayNames() {
        List<String> displayNames = new ArrayList<>();

        String query = "SELECT Display_Name FROM User";

        try (Connection connection = dbConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String displayName = resultSet.getString("Display_Name");
                displayNames.add(displayName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQLException
        }

        return displayNames;
    }

    // Get the userid by name of user
    public int getUserIdByName(String userName) {
        int userId = -1;

        String query = "SELECT User_ID FROM User WHERE Display_Name = ?";

        try (Connection connection = dbConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getInt("User_ID");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQLException
        }

        return userId;
    }

    // Retrieve id of the recipe by selecting name from db
    public int getRecipeIdByName(String recipeName) {
        int recipeId = -1;

        String query = "SELECT Recipe_ID FROM Recipe WHERE Name = ?";

        try (Connection connection = dbConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, recipeName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recipeId = resultSet.getInt("Recipe_ID");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQLException
        }

        return recipeId;
    }

    // Send the message to User(DB table) with recipe id, sender and reciever id and
    // a message
    public boolean sendMessageToUser(int recipeId, int senderId, int receiverId, String message) {
        boolean success = false;

        String query = "INSERT INTO Message (Sender_ID, Receiver_ID, Recipe_ID, Message_text) VALUES (?, ?, ?, ?)";

        try (Connection connection = dbConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, senderId);
            statement.setInt(2, receiverId);
            statement.setInt(3, recipeId);
            statement.setString(4, message);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                success = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQLException
        }

        return success;
    }
    // **************************
    // Here we have SQL for Admin system
    // **************************

    /**
     * Inserts a new user into the database.
     *
     * @param name     The user's display name.
     * @param username The user's username.
     * @param password The user's password.
     * @param isAdmin  Whether the user has admin rights.
     * @return true if the user was added successfully, false otherwise.
     */
    boolean insertUser(String name, String username, String password, boolean isAdmin) {
        String sql = "INSERT INTO User (Username, Password, Display_Name, isAdmin) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println(sql);

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setBoolean(4, isAdmin);
            int result = pstmt.executeUpdate();
            System.out.println(pstmt);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // delete the user from the db
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM User WHERE Username = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println(sql);
            pstmt.setString(1, username);
            int result = pstmt.executeUpdate();
            System.out.println(pstmt);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // db logic of modify user
    public boolean editUser(String oldUsername, String newName, String newUsername, String newPassword) {
        String sql = "UPDATE User SET Display_Name = ?, Username = ?, Password = ? WHERE Username = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, newUsername);
            pstmt.setString(3, newPassword);
            pstmt.setString(4, oldUsername);
            System.out.println(pstmt);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<User> searchUserByName(Connection conn, String searchText) throws SQLException {
        ObservableList<User> filteredList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM User WHERE Display_Name LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("User_ID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Display_Name"),
                        rs.getBoolean("isadmin"));
                filteredList.add(user);
            }
        }
        return filteredList;
    }

    // get comments
    public ObservableList<Comment> getCommentsForRecipe(int recipeId) {
        ObservableList<Comment> comments = FXCollections.observableArrayList();
        String sql = "SELECT Comment_ID, Comment_Text FROM Comment WHERE Recipe_ID = ?";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int commID = rs.getInt("Comment_ID");
                String commentText = rs.getString("Comment_Text");
                comments.add(new Comment(commID, commentText));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception properly
        }
        return comments;
    }

    // add a comment to db
    public Comment addComment(int recipeId, String displayName, String commentText) {
        DbConnector dbConnector = new DbConnector(); // Create an instance of DbConnector
        String sql = "INSERT INTO Comment (Recipe_ID, User_ID, Comment_Text) VALUES (?, (SELECT User_ID FROM User WHERE Display_Name = ?), ?)";
        try (Connection conn = dbConnector.getConnection(); // Use the instance to call getConnection()
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, displayName); // Use displayName instead of username
            pstmt.setString(3, commentText);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int commentId = generatedKeys.getInt(1);
                        return new Comment(commentId, commentText); // Return the new Comment object
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if the comment was not added successfully
    }

    // del a comment from db
    public boolean deleteComment(int commentId) {
        String sql = "DELETE FROM Comment WHERE Comment_ID = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches the existing comment made by a user on a specific recipe.
     *
     * @param recipeId The ID of the recipe.
     * @param username The username of the user.
     * @return The Comment object if found, otherwise null.
     */
    public Comment getCommentByUserAndRecipe(int recipeId, String displayName) {
        String sql = "SELECT Comment_ID, Comment_Text FROM Comment WHERE Recipe_ID = ? AND User_ID = (SELECT User_ID FROM User WHERE Display_Name = ?)";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, displayName); // Use displayName instead of username
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int commID = rs.getInt("Comment_ID");
                String commentText = rs.getString("Comment_Text");
                return new Comment(commID, commentText); // Return the found comment
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no comment is found
    }

    // update a comment from db
    public boolean updateComment(int commentId, String commentText) {
        DbConnector dbConnector = new DbConnector();
        String sql = "UPDATE Comment SET Comment_Text = ? WHERE Comment_ID = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, commentText);
            pstmt.setInt(2, commentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // **************************
    // Here we have SQL for Recipe system
    // **************************

    // populate recipes in the table
    public ObservableList<Recipe> getAllRecipes() {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList();

        String sql = "SELECT Recipe_ID, Name, Short_Description, Detailed_Description, Number_of_Persons, tag_name " +
                "FROM Recipe";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int recipeId = rs.getInt("Recipe_ID");
                String name = rs.getString("Name");
                String shortDescription = rs.getString("Short_Description");
                String detailedDescription = rs.getString("Detailed_Description");
                int numberOfPeople = rs.getInt("Number_of_Persons");
                String tag = rs.getString("tag_name");

                // If you're not using ingredients, you can pass null or an empty list here
                ObservableList<String> ingredients = FXCollections.observableArrayList();

                Recipe recipe = new Recipe(recipeId, name, shortDescription, detailedDescription, numberOfPeople,
                        ingredients, tag);
                recipes.add(recipe);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipes;
    }

    // add tag to db
    public void addTag(String newTag) throws SQLException {
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM cook.Tag WHERE Name = ?")) {

            checkStmt.setString(1, newTag);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Tag already exists.");
            } else {
                String insertSql = "INSERT INTO cook.Tag (Name) VALUES (?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, newTag);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    // add a recipe to db
    public void addRecipe(String name, String shortDescription, String detailedDescription, int numberOfPeople,
            ObservableList<String> ingredients, String tag) throws SQLException {

        try (Connection conn = dbConnector.getConnection()) {
            String sql = "INSERT INTO Recipe (Name, Short_Description, Detailed_Description, Number_of_Persons, tag_name) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, shortDescription);
                pstmt.setString(3, detailedDescription);
                pstmt.setInt(4, numberOfPeople);
                pstmt.setString(5, tag);

                pstmt.executeUpdate();
                System.out.println(pstmt);
                // Retrieve the auto-generated recipe ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int recipeId = generatedKeys.getInt(1);

                        // Insert ingredients with the corresponding recipe ID
                        for (String ingredientInfo : ingredients) {
                            String[] parts = ingredientInfo.split(":");
                            String ingredientName = parts[0].trim();
                            String ingredientQuantity = parts[1].trim();

                            String ingredientSql = "INSERT INTO Ingredient (Recipe_ID, Name, Quantity) VALUES (?, ?, ?)";
                            try (PreparedStatement ingredientStmt = conn.prepareStatement(ingredientSql)) {
                                ingredientStmt.setInt(1, recipeId);
                                ingredientStmt.setString(2, ingredientName);
                                ingredientStmt.setString(3, ingredientQuantity);
                                ingredientStmt.executeUpdate();
                                System.out.println(pstmt);
                            }
                        }
                    } else {
                        throw new SQLException("Failed to get the auto-generated recipe ID.");
                    }
                }
            }
        }
    }

    // get indgredients for a recipe
    public ObservableList<Ingredient> getIngredientsByRecipeId(String recipeId) {
        ObservableList<Ingredient> ingredientsList = FXCollections.observableArrayList();
        String sql = "SELECT Name, Quantity FROM Ingredient WHERE Recipe_ID = ?";

        try (Connection connection = dbConnector.getConnection();
                PreparedStatement stmnt = connection.prepareStatement(sql)) {

            stmnt.setString(1, recipeId);

            try (ResultSet resultSet = stmnt.executeQuery()) {
                while (resultSet.next()) {
                    String ingredientName = resultSet.getString("Name");
                    String quantity = resultSet.getString("Quantity");

                    Ingredient ingredient = new Ingredient(ingredientName, quantity);
                    ingredientsList.add(ingredient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredientsList;
    }

    // del a recipe from db
    public boolean deleteRecipe(Recipe recipe) {
        String sql = "DELETE FROM Recipe WHERE Recipe_ID = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recipe.getRecipeId());
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to update a recipe in the database
    public static boolean editRecipe(Recipe recipe) {
        try (Connection conn = dbConnector.getConnection()) {
            String query = "UPDATE Recipe SET Name=?, Short_Description=?, Detailed_Description=?, Number_of_Persons=?, tag_name=? WHERE Recipe_ID=?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, recipe.getName());
            statement.setString(2, recipe.getShortDescription());
            statement.setString(3, recipe.getDetailedDescription());
            statement.setInt(4, recipe.getNumberOfPeople());
            statement.setString(5, recipe.getTag());

            statement.setInt(6, recipe.getRecipeId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // get all users
    public static ObservableList<User> fetchAllUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM User";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("User_ID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Display_Name"),
                        rs.getBoolean("isadmin"));
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error properly
        }
        return userList;
    }

    // get all tags from db
    public static ObservableList<String> getAllTags() throws SQLException {
        ObservableList<String> tags = FXCollections.observableArrayList();

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cook.Tag");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tagName = rs.getString("Name");
                tags.add(tagName);
            }
        }
        return tags;
    }

    // add a recipe to favorite
    public boolean addToFavorites(String username, int recipeId, String recipeName, String shortDescription) {
        try (Connection conn = dbConnector.getConnection()) {
            // Insert the favorite recipe into the FavoriteRecipe table
            String sql = "INSERT INTO FavoriteRecipe (Username, Recipe_ID, RecipeName, ShortDescription) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setInt(2, recipeId);
                statement.setString(3, recipeName);
                statement.setString(4, shortDescription);
                statement.executeUpdate();
                return true; // Return true if the insertion is successful
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if there's an exception or the insertion fails
        }
    }

    // get favorite recipes
    public ObservableList<Recipe> getFavoriteRecipes(String username) throws SQLException {
        ObservableList<Recipe> favoriteRecipes = FXCollections.observableArrayList();

        String query = "SELECT RecipeName, ShortDescription FROM FavoriteRecipe WHERE Username = ?";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String recipeName = resultSet.getString("RecipeName");
                    String shortDescription = resultSet.getString("ShortDescription");

                    // Create a Recipe object with only name and short description and add it to the
                    // list
                    Recipe recipe = new Recipe(0, recipeName, shortDescription, "", 0, null, null);
                    favoriteRecipes.add(recipe);
                }
            }
        }
        return favoriteRecipes;
    }

    public int getFavoriteRecipeId(String username, String recipeName) throws SQLException {
        int recipeId = -1;
        try (Connection conn = dbConnector.getConnection()) {
            String query = "SELECT Recipe_ID FROM FavoriteRecipe WHERE Username = ? AND RecipeName = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, recipeName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        recipeId = resultSet.getInt("Recipe_ID");
                    }
                }
            }
        }
        return recipeId;
    }

    // del fav recipe
    public boolean deleteFavoriteRecipe(String username, int recipeId) {
        String deleteQuery = "DELETE FROM FavoriteRecipe WHERE Username = ? AND Recipe_ID = ?";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, recipeId);

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // get the shopping list with week numbers from db
    public ObservableList<String> getShoppingListFromDatabase() {
        ObservableList<String> shoppingListData = FXCollections.observableArrayList();
        String query = "SELECT ShoppingList_ID, Week_ID FROM ShoppingList";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String shoppingListId = resultSet.getString("ShoppingList_ID");
                String week = resultSet.getString("Week_ID");
                shoppingListData.add(week + " | " + shoppingListId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Error", "Failed to fetch shopping list data from the database.");
        }
        return shoppingListData;
    }

    // **************************
    // Here ends SQL for recipe system
    // **************************

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static ObservableList<Message> fetchMessages(String loggedInUsername) {
        ObservableList<Message> messageList = FXCollections.observableArrayList();
        String query = "SELECT m.Timestamp, m.Message_text, m.Message_ID, u.Display_Name, m.Receiver_ID, m.Recipe_ID " +
                "FROM Message m " +
                "JOIN User u ON m.Sender_ID = u.User_ID " +
                "WHERE m.Receiver_ID = (SELECT User_ID FROM User WHERE Display_Name = ?)";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (loggedInUsername != null) {
                pstmt.setString(1, loggedInUsername);
                ResultSet rs = pstmt.executeQuery();
                // Log the executed query
                System.out.println("Executed SQL query: " + pstmt.toString());

                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("Timestamp");
                    String text = rs.getString("Message_text");
                    String message_ID = rs.getString("Message_ID");
                    String sender = rs.getString("Display_Name");
                    String receiver = rs.getString("Receiver_ID");
                    String recipe_ID = rs.getString("Recipe_ID");

                    Message message = new Message(time, text, message_ID, sender, receiver, recipe_ID);
                    messageList.add(message);
                }
            } else {
                System.out.println("Logged-in username is NULL");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messageList;
    }

    public static String fetchRecipeDetails(String recipe_ID) {
        String query = "SELECT Name, Short_Description, Detailed_Description, tag_name FROM Recipe WHERE Recipe_ID = ?";
        String recipeDetails = "";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, recipe_ID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Concatenate recipe attributes into a single string
                String name = rs.getString("Name");
                String shortDescription = rs.getString("Short_Description");
                String detailedDescription = rs.getString("Detailed_Description");
                String tagName = rs.getString("tag_name");

                // Create a string representation of the recipe details
                recipeDetails = "Name:  " + name + "\n\n"
                        + "Short Description:  " + shortDescription + "\n\n"
                        + "Detailed Description:  " + detailedDescription + "\n\n"
                        + "Tag Name:  " + tagName;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipeDetails;
    }

    public static String fetchMessageDetails(String message_ID) {
        String query = "SELECT Message_text FROM Message WHERE Message_ID = ?";
        String messageDetails = "";

        try (Connection conn = dbConnector.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, message_ID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                messageDetails = rs.getString("Message_text");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messageDetails;
    }
}

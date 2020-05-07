package ch.epfl.polychef.users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.polychef.R;
import ch.epfl.polychef.pages.HomePage;
import ch.epfl.polychef.utils.Preconditions;

//TODO remove serializable
/**
 * Represents a Polychef User.
 */
public class User implements Serializable {

    private String email;
    private String username;
    private int profilePictureId;
    private List<String> recipes;
    private List<String> favourites;
    private List<String> subscribers;
    private List<String> subscriptions;

    private String key;

    /**
     * Constructs a User from basic information.
     * @param email the email address of the user
     * @param username the username of the user
     */
    public User(String email, String username){
        this.email = email;
        this.username = username;
        setProfilePictureId(0);
        recipes = new ArrayList<>();
        favourites = new ArrayList<>();
        subscribers = new ArrayList<>();
        subscriptions = new ArrayList<>();
    }

    /**
     * Construct empty User for Firebase.
     */
    public User() {
        setProfilePictureId(0);
        recipes = new ArrayList<>();
        favourites = new ArrayList<>();
        subscribers = new ArrayList<>();
        subscriptions = new ArrayList<>();
    }

    /**
     * Removes the null values in the list of recipes.
     */
    public void removeNullFromLists(){
        recipes.removeAll(Collections.singleton(null));
        //TODO remove from others as well?
    }

    /**
     * Gets the email of the user.
     * @return the email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the username of the user.
     * @return the username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the list of recipes of the user.
     * @return the list of recipes of the user
     */
    public List<String> getRecipes() {
        return new ArrayList<>(recipes);
    }

    /**
     * Gets the list of favorites of the user.
     * @return the list of favorites of the user
     */
    public List<String> getFavourites() {
        return new ArrayList<>(favourites);
    }

    /**
     * Gets the list of subscribers of the user.
     * @return the list of subscribers of the user
     */
    public List<String> getSubscribers() {
        return new ArrayList<>(subscribers);
    }

    /**
     * Gets the list of subscriptions of the user.
     * @return the list of subscriptions of the user
     */
    public List<String> getSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    /**
     * Gets the id of the user profile picture.
     * @return the id of the user profile picture
     */
    public int getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(int profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public void addRecipe(String recipe) {
        recipes.add(recipe);
    }

    public void addFavourite(String recipe){
        favourites.add(recipe);
    }

    public void removeFavourite(String recipe) {
        Preconditions.checkArgument(favourites.contains(recipe), "Can not remove recipe from favourite if it was not in favourite before");
        favourites.remove(recipe);
    }

    public void addSubscription(String user) {
        subscriptions.add(user);
    }

    public void removeSubscription(String email) {
        Preconditions.checkArgument(subscriptions.contains(email), "Can not remove from subscriptions");
        subscriptions.remove(email);
    }

    public void addSubscriber(String user) {
        subscribers.add(user);
    }

    public void removeSubscriber(String email) {
        Preconditions.checkArgument(subscribers.contains(email), "Can not remove from subscribers");
        subscribers.remove(email);
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getKey() {
        return this.key;
    }

    @NonNull
    @Override
    public String toString() {
        return "User: \n"
                + "Email=" + email + ",\n"
                + "username=" + username + ",\n"
                + "recipes=" + recipes + ",\n"
                + "favourites=" + favourites + ",\n"
                + "subscribers=" + subscribers + ",\n"
                + "subscriptions=" + subscriptions;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if(this == obj){
            return true;
        }

        if(obj instanceof User){
            User other = (User) obj;

            boolean isSamePerson = Objects.equals(email, other.email)
                    && Objects.equals(username, other.username);

            boolean hasSameRecipes = Objects.equals(recipes, other.recipes)
                    && Objects.equals(favourites, other.favourites);

            boolean hasSameSubs = Objects.equals(subscribers, other.subscribers)
                    && Objects.equals(subscriptions, other.subscriptions);

            return isSamePerson
                    && hasSameRecipes
                    && hasSameSubs;
        }

        return false;
    }

    public static int getResourceImageFromActivity(HomePage hostActivity,User userToDisplay){
        int profilePictureId=userToDisplay.getProfilePictureId();
        String photoName=hostActivity.getResources().getStringArray(R.array.profilePicturesNames)[profilePictureId];
        int resourceImage = hostActivity.getResources().getIdentifier(photoName, "drawable", hostActivity.getPackageName());
        return resourceImage;
    }
}

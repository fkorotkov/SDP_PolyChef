package ch.epfl.polychef.users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.polychef.utils.Preconditions;

//TODO remove serializable
public class User implements Serializable {

    private String email;
    private String username;
    private List<String> recipes;
    private List<String> favourites;
    private List<String> subscribers;
    private List<String> subscriptions;

    public User(String email, String username){
        this.email = email;
        this.username = username;
        recipes = new ArrayList<>();
        favourites = new ArrayList<>();
        subscribers = new ArrayList<>();
        subscriptions = new ArrayList<>();
    }

    public User() {
        recipes = new ArrayList<>();
        favourites = new ArrayList<>();
        subscribers = new ArrayList<>();
        subscriptions = new ArrayList<>();
    }

    public void removeNullFromLists(){
        recipes.removeAll(Collections.singleton(null));
        //TODO remove from others as well?
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRecipes() {
        return new ArrayList<>(recipes);
    }

    public List<String> getFavourites() {
        return new ArrayList<>(favourites);
    }

    public List<String> getSubscribers() {
        return new ArrayList<>(subscribers);
    }

    public List<String> getSubscriptions() {
        return new ArrayList<>(subscriptions);
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

    public void addSubscriber(String user) {
        subscribers.add(user);
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
}

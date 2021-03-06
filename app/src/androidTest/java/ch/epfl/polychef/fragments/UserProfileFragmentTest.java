package ch.epfl.polychef.fragments;

import android.content.Intent;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.intercepting.SingleActivityFactory;

import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

import ch.epfl.polychef.CallHandler;
import ch.epfl.polychef.R;
import ch.epfl.polychef.pages.HomePage;
import ch.epfl.polychef.recipe.Ingredient;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.recipe.RecipeBuilder;
import ch.epfl.polychef.recipe.RecipeStorage;
import ch.epfl.polychef.users.User;
import ch.epfl.polychef.users.UserStorage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class UserProfileFragmentTest {

    private String mockEmail = "mock@email.com";
    private String mockUsername = "mockUsername";
    private User mockUser;

    private FragmentTestUtils fragUtils = new FragmentTestUtils();

    private RecipeBuilder builder = new RecipeBuilder()
            .setRecipeDifficulty(Recipe.Difficulty.EASY)
            .addInstruction("test1instruction")
            .setPersonNumber(4)
            .setEstimatedCookingTime(30)
            .setEstimatedPreparationTime(30)
            .addIngredient("test1", 1.0, Ingredient.Unit.CUP);

    private SingleActivityFactory<HomePage> fakeHomePage = new SingleActivityFactory<HomePage>(
            HomePage.class) {
        @Override
        protected HomePage create(Intent intent) {
            HomePage activity = new UserProfileFragmentTest.FakeHomePage();
            return activity;
        }
    };

    @Rule
    public ActivityTestRule<HomePage> intentsTestRule = new ActivityTestRule<>(fakeHomePage, false,
            false);

    @Before
    public synchronized void initTest() throws InterruptedException {
        mockUser = new User(mockEmail, mockUsername);
        mockUser.setKey("test key");

        Intents.init();
        intentsTestRule.launchActivity(new Intent());
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        wait(1000);
    }

    public void startTest() {
        onView(withId(R.id.drawerProfileImage)).perform(click());
    }

    @Test
    public void userInfoIsDisplayed() {
        startTest();
        onView(withId(R.id.UsernameDisplay)).check(matches(withText(mockUsername)));
        onView(withId(R.id.UserEmailDisplay)).check(matches(withText(mockEmail)));
    }

    @Test
    public void nothingIsDisplayedWhenUserHasNoRecipe() {
        startTest();
        assertEquals(0, ((UserProfileFragment) fragUtils.getTestedFragment(intentsTestRule)).getUserRecyclerView().getAdapter().getItemCount());
    }

    @Test
    public synchronized void recipesAreDisplayedWhenUserHasFewOfThem() throws InterruptedException {
        testUserRecipes(0, UserProfileFragment.NB_OF_RECIPES_LOADED_AT_A_TIME + 1);
    }

    @Test
    public synchronized void recipesAreDisplayedWhenUserHasManyOfThem() throws InterruptedException {
        testUserRecipes(UserProfileFragment.NB_OF_RECIPES_LOADED_AT_A_TIME + 1, 20);
    }

    @Test
    public synchronized void userAchievementsDisplayCorrectToast() throws InterruptedException {
        mockUser.addRecipe("Recipe1");
        mockUser.addRecipe("Recipe2");

        startTest();

        onView(withId(R.id.userProfileFragment)).perform(swipeDown());
        onView(withId(R.id.cuistot_achievement)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.cuistot_achievement)).perform(click());

        onView(withText("Starting Chef"))
                .inRoot(withDecorView(not(is(intentsTestRule.getActivity()
                        .getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        wait(2000);

        onView(withId(R.id.followed_achievement)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.followed_achievement)).perform(click());

        onView(withText("No followers"))
                .inRoot(withDecorView(not(is(intentsTestRule.getActivity()
                        .getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        wait(2000);

        onView(withId(R.id.favorite_achievement)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.favorite_achievement)).perform(click());

        onView(withText("No favorite"))
                .inRoot(withDecorView(not(is(intentsTestRule.getActivity()
                        .getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        wait(20000);
    }

    public synchronized void testUserRecipes(int min, int max) throws InterruptedException {
        Random rnd = new Random();

        int nbr = rnd.nextInt(max - min) + min;   //min inclusive, max exclusive
        for(int i = 0; i < nbr; ++i){
            mockUser.addRecipe("Recipe " + i);
        }

        startTest();

        int recipeLoaded = UserProfileFragment.NB_OF_RECIPES_LOADED_AT_A_TIME;
        assertEquals(Math.min(recipeLoaded, nbr), ((UserProfileFragment) fragUtils.getTestedFragment(intentsTestRule)).getUserRecyclerView().getAdapter().getItemCount());

        for(int i = 0; i < nbr/recipeLoaded; ++i){
            onView(withId(R.id.userProfileFragment)).perform(swipeUp());
            wait(1000);

        }

        assertEquals(nbr, ((UserProfileFragment) fragUtils.getTestedFragment(intentsTestRule)).getUserRecyclerView().getAdapter().getItemCount());
    }

    @After
    public void finishActivity(){
        intentsTestRule.finishActivity();
        Intents.release();
    }

    class FakeHomePage extends HomePage{

        @Override
        public RecipeStorage getRecipeStorage(){
            RecipeStorage mockRecipeStorage = Mockito.mock(RecipeStorage.class);
            doAnswer(invocation -> {

                String uuid = invocation.getArgument(0);
                CallHandler<Recipe> caller = invocation.getArgument(1);

                caller.onSuccess(builder.setName(uuid).setAuthor(mockEmail).build());

                return null;
            }).when(mockRecipeStorage).readRecipeFromUuid(any(String.class), any(CallHandler.class));
            return mockRecipeStorage;
        }

        @Override
        public UserStorage getUserStorage(){
            UserStorage mockUserStorage = Mockito.mock(UserStorage.class);
            when(mockUserStorage.getAuthenticatedUser()).thenReturn(Mockito.mock(FirebaseUser.class));
            when(mockUserStorage.getPolyChefUser()).thenReturn(mockUser);
            return mockUserStorage;
        }

      @Override
        public FirebaseUser getUser() {
            FirebaseUser mockUser = Mockito.mock(FirebaseUser.class);
            when(mockUser.getEmail()).thenReturn(mockEmail);
            when(mockUser.getDisplayName()).thenReturn(mockUsername);
            return mockUser;
        }
    }
}

package ch.epfl.polychef.images;

import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.intercepting.SingleActivityFactory;

import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.polychef.CallHandler;
import ch.epfl.polychef.R;
import ch.epfl.polychef.fragments.FragmentTestUtils;
import ch.epfl.polychef.fragments.OnlineMiniaturesFragment;
import ch.epfl.polychef.image.ImageStorage;
import ch.epfl.polychef.pages.HomePage;
import ch.epfl.polychef.recipe.Ingredient;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.recipe.RecipeBuilder;
import ch.epfl.polychef.recipe.RecipeStorage;
import ch.epfl.polychef.users.User;
import ch.epfl.polychef.users.UserStorage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeMiniatureImageTest {

    private String mockEmail = "mock@email.com";
    private String mockUsername = "mockUsername";
    private User mockUser;
    private FragmentTestUtils fragUtils = new FragmentTestUtils();
    private ImageStorage mockImageStorage;

    public static RecipeBuilder recipeBuilder = new RecipeBuilder()
                .addInstruction("test instruction")
            .setPersonNumber(4)
            .setEstimatedCookingTime(35)
            .setEstimatedPreparationTime(40)
            .addIngredient("test", 1.0, Ingredient.Unit.CUP)
            .setRecipeDifficulty(Recipe.Difficulty.EASY)
            .setDate("20/05/01 12:00:00")
            .setAuthor("testAuthor");

    private Recipe recipe1 = recipeBuilder.setName("test1").setMiniatureFromPath("test_path").build();

    private Recipe recipe2 = recipeBuilder.setName("test2").setMiniatureFromPath("test_path2").build();

    private SingleActivityFactory<HomePage> fakeHomePage = new SingleActivityFactory<HomePage>(
            HomePage.class) {
        @Override
        protected HomePage create(Intent intent) {
            HomePage activity = new RecipeMiniatureImageTest.FakeHomePage();
            return activity;
        }
    };

    @Rule
    public ActivityTestRule<HomePage> intentsTestRule = new ActivityTestRule<>(fakeHomePage, false,
            false);

    @Before
    public synchronized void initTest() {
        mockUser = new User(mockEmail, mockUsername);
        mockImageStorage = mock(ImageStorage.class);
        doAnswer(invocation -> {

            CallHandler<byte[]> caller = invocation.getArgument(1);

            byte[] data = new byte[] {1, 2, 3, 4, 3, 2, 1};

            if(invocation.getArgument(0).equals("test_path")) {
                caller.onSuccess(data);
            } else {
                caller.onFailure();
            }
            return null;
        }).when(mockImageStorage).getImage(any(String.class), any(CallHandler.class));


        Intents.init();
        intentsTestRule.launchActivity(new Intent());
    }

    @Test
    public synchronized void canShowOnlineMiniature() throws InterruptedException {
        wait(2000);
        assertEquals(2, ((OnlineMiniaturesFragment) fragUtils.getTestedFragment(intentsTestRule)).getRecyclerView().getAdapter().getItemCount());
        onView(allOf(withId(R.id.miniatureRecipeImage), hasSibling(withChild(withText("test1"))))).check(matches(isDisplayed()));
        onView(withId(R.id.miniaturesOnlineList)).perform(actionOnItemAtPosition(1, scrollTo()));
        onView(allOf(withId(R.id.miniatureRecipeImage), hasSibling(withChild(withText("test2"))))).check(matches(isDisplayed()));
    }


    @After
    public void finishActivity(){
        intentsTestRule.finishActivity();
        Intents.release();
    }


    private class FakeHomePage extends HomePage {

        @Override
        public UserStorage getUserStorage(){
            return getMockUserStorage(mockUser);
        }

        @Override
        public RecipeStorage getRecipeStorage(){
            RecipeStorage mockRecipeStorage = mock(RecipeStorage.class);

            when(mockRecipeStorage.getCurrentDate()).thenReturn(RecipeStorage.OLDEST_RECIPE);

            doAnswer((call) -> {
                CallHandler<List<Recipe>> ch = call.getArgument(4);
                List<Recipe> results = new ArrayList<>();
                results.add(recipe1);
                results.add(recipe2);
                ch.onSuccess(results);
                return null;
            }).when(mockRecipeStorage).getNRecipes(any(Integer.class), any(String.class), any(String.class), any(Boolean.class), any(CallHandler.class));

            return mockRecipeStorage;
        }

        @Override
        public ImageStorage getImageStorage() {
            return mockImageStorage;
        }

        @Override
        public FirebaseUser getUser() {
            FirebaseUser mockUser = mock(FirebaseUser.class);
            return mockUser;
        }
    }

    public static UserStorage getMockUserStorage(User mockUser){
        UserStorage mockUserStorage = mock(UserStorage.class);
        when(mockUserStorage.getAuthenticatedUser()).thenReturn(mock(FirebaseUser.class));
        when(mockUserStorage.getPolyChefUser()).thenReturn(mockUser);
        return  mockUserStorage;
    }
}

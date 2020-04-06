package ch.epfl.polychef.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.polychef.R;
import ch.epfl.polychef.pages.EntryPage;
import ch.epfl.polychef.pages.LoginPage;

@RunWith(AndroidJUnit4.class)
public class EntryPageTest {
    @Rule
    public IntentsTestRule<EntryPage> intentsTestRule = new IntentsTestRule<>(EntryPage.class);

    @Test
    public void buttonTextIsLogin() {
        onView(ViewMatchers.withId(R.id.logButton)).check(matches(withText("Log in")));
    }

    @Test
    public void onClickLoginGoToLoginPage(){
        onView(withId(R.id.logButton)).perform(click());
        intended(hasComponent(LoginPage.class.getName()));
    }
}
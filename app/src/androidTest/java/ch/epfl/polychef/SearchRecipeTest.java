package ch.epfl.polychef;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.polychef.recipe.Ingredient;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.recipe.RecipeBuilder;
import ch.epfl.polychef.recipe.SearchRecipe;
import ch.epfl.polychef.utils.CallHandlerChecker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class SearchRecipeTest {

    private SearchRecipe mockSearchRecipe;
    private FirebaseDatabase mockDataBase;
    private DatabaseReference mockDatabaseReference;
    private DataSnapshot mockDataSnapshot;

    private DataSnapshot mockDataSnapshotWithRecipe0;
    private DataSnapshot mockDataSnapshotWithRecipe1;
    private DataSnapshot mockDataSnapshotWithRecipe2;

    private Recipe recipe0;
    private Recipe recipe1;
    private Recipe recipe2;

    private ValueEventListener givenValueEventListener;

    @Before
    public void initTests(){
        mockSearchRecipe= Mockito.mock(SearchRecipe.class,Mockito.CALLS_REAL_METHODS);
        mockDataBase= Mockito.mock(FirebaseDatabase.class);
        mockDatabaseReference=Mockito.mock(DatabaseReference.class);
        mockDataSnapshot=Mockito.mock(DataSnapshot.class);

        givenValueEventListener=null;

        when(mockSearchRecipe.getDatabase()).thenReturn(mockDataBase);

        when(mockDataBase.getReference("recipe")).thenReturn(mockDatabaseReference);

        doAnswer((call) -> {
            givenValueEventListener=  call.getArgument(0);
            return null;
        }).when(mockDatabaseReference).addValueEventListener(any(ValueEventListener.class));

        initializeMockRecipes();
    }

    @Test
    public void methodGetInstanceExist(){
        SearchRecipe.getInstance();
    }

    @Test
    public void testSearchForRecipeFailWithSnapshotNullValue(){

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(null,false);

        mockSearchRecipe.searchForRecipe("e",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(null);

        //trigger changes
        givenValueEventListener.onDataChange(mockDataSnapshot);

        callHandlerChecker.assertWasCalled();
    }

    @Test
    public void testSearchForRecipeFailOnCancel(){

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(null,false);

        mockSearchRecipe.searchForRecipe("e",callHandlerChecker);

        //trigger cancellation
        givenValueEventListener.onCancelled(Mockito.mock(DatabaseError.class));

        callHandlerChecker.assertWasCalled();
    }

    @Test
    public void testSearchForRecipeFindRecipeWithGivenWord(){
        List<Recipe> expectedRecipeList=new ArrayList<>();
        expectedRecipeList.add(recipe0);
        expectedRecipeList.add(recipe1);
        expectedRecipeList.add(recipe2);

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(expectedRecipeList ,true);

        mockSearchRecipe.searchForRecipe("3",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    @Test
    public void testSearchForRecipeFindNothing(){
        List<Recipe> expectedRecipeList=new ArrayList<>();

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(expectedRecipeList ,true);

        mockSearchRecipe.searchForRecipe("8",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    @Test
    public void testSearchForRecipeFindOverString(){
        List<Recipe> list=new ArrayList<>();
        list.add(recipe0);
        list.add(recipe1);

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(list ,true);

        mockSearchRecipe.searchForRecipe("2345",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    @Test
    public void testSearchForRecipeIsCaseInsensitive(){
        List<Recipe> expected=new ArrayList<>();
        expected.add(recipe2);

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(expected ,true);

        mockSearchRecipe.searchForRecipe("AbcD",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    @Test
    public void testSearchForIngredientIsCaseInsensitive(){
        List<Recipe> iLoveCodeClimate=new ArrayList<>();
        iLoveCodeClimate.add(recipe0);
        iLoveCodeClimate.add(recipe1);
        iLoveCodeClimate.add(recipe2);

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(iLoveCodeClimate ,true);

        mockSearchRecipe.searchRecipeByIngredient("moC",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    @Test
    public void testSearchForIngredientFindParticularValue(){
        List<Recipe> maListLaMailer=new ArrayList<>();
        maListLaMailer.add(recipe1);

        CallHandlerChecker<List<Recipe>> callHandlerChecker=new CallHandlerChecker<>(maListLaMailer ,true);

        mockSearchRecipe.searchRecipeByIngredient("ssssaltttt",callHandlerChecker);

        when(mockDataSnapshot.getValue()).thenReturn(0);

        givenValueEventListener.onDataChange(mockDataSnapshot);
    }

    private void initializeMockRecipes(){
        mockDataSnapshotWithRecipe0=Mockito.mock(DataSnapshot.class);
        mockDataSnapshotWithRecipe1=Mockito.mock(DataSnapshot.class);
        mockDataSnapshotWithRecipe2=Mockito.mock(DataSnapshot.class);

        recipe0=new RecipeBuilder().setName("Fuck Travis").addInstruction("Fuck CC")
                .setEstimatedPreparationTime(69).setEstimatedCookingTime(69).setPersonNumber(2)
                .addIngredient("CodeClimate", 69, Ingredient.Unit.CUP)
                .setRecipeDifficulty(Recipe.Difficulty.INTERMEDIATE).build();
        recipe1=new RecipeBuilder().setName("34").addInstruction("Yay").addIngredient("Mockitooo", 42, Ingredient.Unit.KILOGRAM)
                .addIngredient("salt", 420, Ingredient.Unit.KILOGRAM)
                .setPersonNumber(6).setEstimatedPreparationTime(999).setEstimatedCookingTime(999)
                .setRecipeDifficulty(Recipe.Difficulty.VERY_HARD).build();
        recipe2=new RecipeBuilder().setName("43-aBcD").addInstruction("Yay")
                .addIngredient("Mockitooo", 42, Ingredient.Unit.KILOGRAM).setPersonNumber(6).setEstimatedPreparationTime(1000)
                .setEstimatedCookingTime(1000).setRecipeDifficulty(Recipe.Difficulty.VERY_HARD).build();

        when(mockDataSnapshotWithRecipe0.getValue(Recipe.class)).thenReturn(recipe0);
        when(mockDataSnapshotWithRecipe1.getValue(Recipe.class)).thenReturn(recipe1);
        when(mockDataSnapshotWithRecipe2.getValue(Recipe.class)).thenReturn(recipe2);

        List<DataSnapshot> snapshotsList=new ArrayList<>();
        snapshotsList.add(mockDataSnapshotWithRecipe0);
        snapshotsList.add(mockDataSnapshotWithRecipe1);
        snapshotsList.add(mockDataSnapshotWithRecipe2);

        when(mockDataSnapshot.getChildren()).thenReturn(snapshotsList);
    }
}

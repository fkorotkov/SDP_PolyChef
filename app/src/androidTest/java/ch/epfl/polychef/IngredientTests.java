package ch.epfl.polychef;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import ch.epfl.polychef.recipe.Ingredient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IngredientTests {

    @Test
    public void ingredientsRejectsInvalidArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Ingredient("", 50, Ingredient.Unit.TEASPOON));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Ingredient("Carrots", -5, Ingredient.Unit.TEASPOON));
    }

    @Test
    public void gettersReturnCorrectAttributes(){
        Ingredient ingredient = new Ingredient("Carrots", 300, Ingredient.Unit.TABLESPOON);
        assertEquals(ingredient.getName(), "Carrots");
        assertEquals(ingredient.getUnit(), Ingredient.Unit.TABLESPOON);
        assertTrue(ingredient.getQuantity() == 300d);
    }

    @Test
    public void settersChangeAttributesAccordingly(){
        Ingredient ingredient = new Ingredient("Steaks", 2, Ingredient.Unit.KILOGRAM);
        ingredient.setQuantity(1);
        ingredient.setName("Steak");
        ingredient.setUnit(Ingredient.Unit.KILOGRAM);
        assertEquals(ingredient.getName(), "Steak");
        assertEquals(ingredient.getUnit(), Ingredient.Unit.KILOGRAM);
        assertTrue(ingredient.getQuantity() == 1d);
    }

    @Test
    public void toStringReturnsFormattedString(){
        Ingredient ingredient1 = new Ingredient("Oil", 0, Ingredient.Unit.NO_UNIT);
        Ingredient ingredient2 = new Ingredient("Eggs", 6, Ingredient.Unit.NONE);
        Ingredient ingredient3 = new Ingredient("Flour", 300, Ingredient.Unit.GRAM);

        assertEquals(ingredient1.toString(), "0.0 Oil");
        assertEquals(ingredient2.toString(), "Eggs");
        assertEquals(ingredient3.toString(), "300.0 grams of Flour");
    }

    @Test
    public void equalIngredientsAreCorrectlyDetected(){
        Ingredient ingredient1 = new Ingredient("Oil", 1, Ingredient.Unit.CUP);

        assertTrue(ingredient1.equals(new Ingredient("Oil", 1, Ingredient.Unit.CUP)));
        assertTrue(!ingredient1.equals(""));
        assertTrue(!ingredient1.equals(new Ingredient("Oil", 2, Ingredient.Unit.CUP)));
        assertTrue(!ingredient1.equals(new Ingredient("Oil", 1, Ingredient.Unit.KILOGRAM)));
        assertTrue(!ingredient1.equals(new Ingredient("Oils", 1, Ingredient.Unit.CUP)));
    }

}

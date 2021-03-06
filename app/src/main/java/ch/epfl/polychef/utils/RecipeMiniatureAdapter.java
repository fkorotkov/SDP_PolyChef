package ch.epfl.polychef.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.epfl.polychef.CallHandler;
import ch.epfl.polychef.R;
import ch.epfl.polychef.fragments.FullRecipeFragment;
import ch.epfl.polychef.image.ImageStorage;
import ch.epfl.polychef.pages.EntryPage;
import ch.epfl.polychef.pages.HomePage;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.users.UserStorage;

/**
 * This class is an adapter that take a list of recipes and update the fields of each miniature inside the miniature list in the recyclerView that is in the activity where the miniatures are shown.
 */
public class RecipeMiniatureAdapter extends RecyclerView.Adapter<RecipeMiniatureAdapter.MiniatureViewHolder> {

    private Context mainContext;
    private List<Recipe> recipeList;
    private RecyclerView recyclerview;
    private int fragmentContainerID;

    private ImageStorage imageStorage;
    private UserStorage userStorage;

    /**
     * Creates a new adapter of recipes to miniatures.
     *
     * @param mainContext  the context where the adapter will operate i.e the activity where the recyclerView is
     * @param recipeList   the list of all the recipes that will be displayed inside the recyclerView
     * @param recyclerView this is the recyclerView where the recipes will be displayed
     * @param fragmentContainerID the id of the fragment container where the miniature are displayed
     */
    public RecipeMiniatureAdapter(Context mainContext, List<Recipe> recipeList, RecyclerView recyclerView, int fragmentContainerID, ImageStorage storage) {
        this(mainContext, recipeList, recyclerView, fragmentContainerID, storage, null);
    }

    /**
     * Creates a new adapter of recipes to miniatures.
     *
     * @param mainContext  the context where the adapter will operate i.e the activity where the recyclerView is
     * @param recipeList   the list of all the recipes that will be displayed inside the recyclerView
     * @param recyclerView this is the recyclerView where the recipes will be displayed
     * @param fragmentContainerID the id of the fragment container where the miniature are displayed
     * @param userStorage the user storage to get the list of favorites from
     */
    public RecipeMiniatureAdapter(Context mainContext, List<Recipe> recipeList, RecyclerView recyclerView, int fragmentContainerID, ImageStorage storage, UserStorage userStorage) {
        this.mainContext = mainContext;
        this.recipeList = recipeList;
        this.recyclerview = recyclerView;
        this.fragmentContainerID = fragmentContainerID;
        this.imageStorage = storage;
        this.userStorage = userStorage;
    }

    /**
     * Change the recipes displayed by the recycler view.
     *
     * @param newRecipes the new recipes
     */
    public void changeList(List<Recipe> newRecipes){
        this.recipeList = newRecipes;
        notifyDataSetChanged();
    }

    /**
     * Gets the image storage corresponding to the adapter.
     * @return the image storage
     */
    public ImageStorage getImageStorage() {
        return imageStorage;
    }

    /**
     * This method create a new MiniatureViewHolder which contains the view which contains the information of the layout of one miniature and make that view listen to user clicks on him.
     *
     * @param parent   not used here but needed since it's an overridden method
     * @param viewType not used here but needed since it's an overridden method
     * @return the new MiniatureViewHolder containing the view
     */
    @NonNull
    @Override
    public MiniatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mainContext);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.miniature_layout, null);
        view.setOnClickListener(new MiniatureOnClickListener(recyclerview));
        return new MiniatureViewHolder(view);
    }

    /**
     * Fill the view with the field of one recipe in the recipe list.
     *
     * @param holder   the MiniatureViewHolder that we need to bind the recipe with
     * @param position the position in the miniature list, this is the position where the miniature will be displayed relatively to the other ones inside the recyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull MiniatureViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeTitle.setText(recipe.getName());
        holder.ratingBar.setRating((float) recipe.getRating().ratingAverage());
        holder.favouriteButton.setOnCheckedChangeListener(null);
        FavouritesUtils.getInstance().setFavouriteButton(userStorage, holder.favouriteButton, recipe);
        getImageFor(holder, recipe);
    }

    /**
     * Return the size of the list of the recipes.
     *
     * @return the size of the list of the recipes
     */
    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private void getImageFor(MiniatureViewHolder holder, Recipe recipe) {
        Either<String, Integer> miniatureMeta = recipe.getMiniaturePath();
        if(miniatureMeta.isNone()) {
            holder.image.setImageResource(Recipe.DEFAULT_MINIATURE_PATH);
        } else if(miniatureMeta.isRight()) {
            holder.image.setImageResource(miniatureMeta.getRight());
        } else {
            getImageStorage().getImage(miniatureMeta.getLeft(), new CallHandler<byte[]>() {
                @Override
                public void onSuccess(byte[] data) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    holder.image.setImageBitmap(bmp);
                }

                @Override
                public void onFailure() {
                    Toast.makeText(mainContext, mainContext.getString(R.string.errorImageRetrieve), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * This is the MiniatureViewHolder that contains the fields of one miniature to be filled when binned to one recipe in the list.
     */
    class MiniatureViewHolder extends RecyclerView.ViewHolder {

        TextView recipeTitle;
        ImageView image;
        RatingBar ratingBar;
        ToggleButton favouriteButton;

        MiniatureViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTitle = itemView.findViewById(R.id.recipeNameMiniature);
            ratingBar = itemView.findViewById(R.id.miniatureRatingBar);
            image = itemView.findViewById(R.id.miniatureRecipeImage);
            favouriteButton = itemView.findViewById(R.id.favouriteButton);
        }
    }

    /**
     * This class is the listener that when we click on a miniature it swaps to the new fragment with the full recipe displayed.
     */
    class MiniatureOnClickListener implements View.OnClickListener {

        RecyclerView recyclerView;

        MiniatureOnClickListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View view) {

            int recipePosition = recyclerView.getChildLayoutPosition(view);
            Recipe clickedRecipe = recipeList.get(recipePosition);

            //Create new Bundle to store recipe object inside the recipe fragment that will be open after
            Bundle bundle = new Bundle();
            bundle.putSerializable("Recipe", clickedRecipe);

            if(mainContext instanceof HomePage){
                ((HomePage) mainContext)
                        .getNavController()
                        .navigate(R.id.fullRecipeFragment, bundle);
            } else if(mainContext instanceof EntryPage){

                AppCompatActivity activity = (AppCompatActivity) mainContext;
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                FullRecipeFragment recipeFragment = new FullRecipeFragment();
                recipeFragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(fragmentContainerID, recipeFragment).addToBackStack(null).commit();
            } else {
                throw new IllegalStateException();
            }
        }
    }

}

package ch.epfl.polychef.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synnapps.carouselview.CarouselView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ch.epfl.polychef.CallHandler;
import ch.epfl.polychef.CallNotifier;
import ch.epfl.polychef.R;
import ch.epfl.polychef.image.ImageStorage;
import ch.epfl.polychef.pages.HomePage;
import ch.epfl.polychef.recipe.Ingredient;
import ch.epfl.polychef.recipe.Opinion;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.users.User;
import ch.epfl.polychef.users.UserStorage;
import ch.epfl.polychef.utils.Either;
import ch.epfl.polychef.utils.FavouritesUtils;
import ch.epfl.polychef.utils.OpinionsMiniatureAdapter;
import ch.epfl.polychef.utils.VoiceRecognizer;
import ch.epfl.polychef.utils.VoiceSynthesizer;

public class FullRecipeFragment extends Fragment implements CallHandler<byte[]>, CallNotifier<String> {
    private Recipe currentRecipe;
    private static final String TAG = "FullRecipeFragment";
    private static final String NEW_LINE = System.lineSeparator();
    private final List<Bitmap> imagesToDisplay = new ArrayList<>();
    private CarouselView carouselView;
    private ToggleButton favouriteButton;
    private TextView authorName;
    private VoiceRecognizer voiceRecognizer;
    private VoiceSynthesizer voiceSynthesizer;


    private NestedScrollView topScrollView;
    private RecyclerView opinionsRecyclerView;
    private OpinionsMiniatureAdapter opinionsAdapter;

    private boolean online;

    private HomePage hostActivity;

    private int indexOfInstruction=-1;

    private int containerId;


    /**
     * Required empty public constructor.
     */
    public FullRecipeFragment() {}

    /**
     * When the View is created we get the recipe and display its attributes.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_full_recipe, container, false);
        // Get the recipe from the click listener of the miniature recyclerView
        Bundle bundle = this.getArguments();
        if(bundle != null){
            currentRecipe = (Recipe) bundle.getSerializable("Recipe");
        }

        displayFavouriteButton(view);
        displayRecipeName(view);
        displayImage(view);
        displayRating(view);
        displayPrepAndCookTime(view);
        displayDifficulty(view);
        displayInstructions(view);
        displayIngredients(view);
        displayAuthorName(view);

        voiceRecognizer=new VoiceRecognizer(this);
        try {
            voiceSynthesizer = new VoiceSynthesizer(getActivity());
        }catch(UnsupportedOperationException e){
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        setupSwitch(view);

        addOpinion(view);

        containerId = container.getId();

        return view;
    }

    private void addOpinion(View view) {
        if(online) {
            opinionsRecyclerView = view.findViewById(R.id.opinionsList);
            opinionsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
            opinionsAdapter = new OpinionsMiniatureAdapter(this.getActivity(), opinionsRecyclerView, currentRecipe, hostActivity.getUserStorage());
            opinionsRecyclerView.setAdapter(opinionsAdapter);
            topScrollView = view.findViewById(R.id.fullRecipeFragment);
            topScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(!opinionsAdapter.isLoading()) {
                        if (!topScrollView.canScrollVertically(1)) {
                            opinionsAdapter.loadNewComments();
                        }
                    }
                }
            });
            opinionsAdapter.loadNewComments();
        }
    }

    private void displayAuthorName(View view) {
        authorName = view.findViewById(R.id.authorUsername);
        getUserStorage().getUserByEmail(currentRecipe.getAuthor(), new CallHandler<User>() {
            @Override
            public void onSuccess(User data) {
                authorName.setText(data.getUsername());
                authorName.setOnClickListener(v -> {
                    NavController navController = ((HomePage) getActivity()).getNavController();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("User", data);
                    navController.navigate(R.id.userProfileFragment, bundle);
                });
            }

            @Override
            public void onFailure() {
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button postButton = getView().findViewById(R.id.buttonRate);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() instanceof HomePage) {

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("RecipeToRate", currentRecipe);

                    NavController navController = ((HomePage) getActivity()).getNavController();
                    navController.navigate(R.id.rateRecipeFragment, bundle);

                }else {
                    Toast.makeText(getActivity(),getActivity().getString(R.string.errorOnlineFeature), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof HomePage){
            hostActivity = (HomePage) context;
            online = true;
        } else {
            online = false;
            hostActivity = null;
        }
    }

    private void displayFavouriteButton(View view) {
        favouriteButton = view.findViewById(R.id.favouriteButton);
        FavouritesUtils.getInstance().setFavouriteButton(getUserStorage(), view.findViewById(R.id.favouriteButton), currentRecipe);
    }

    private void setupSwitch(View view) {
        Switch onOffSwitch = view.findViewById(R.id.voiceRecognitionSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    voiceRecognizer.start(getActivity());
                }else{
                    voiceRecognizer.onStop();
                }
            }
        });
    }

    /**
     * Display the recipe images in the correct field in the activity.
     */
    private void displayImage(View view) {
        imagesToDisplay.clear();
        carouselView = view.findViewById(R.id.recipeImages);
        carouselView.setPageCount(imagesToDisplay.size());
        carouselView.setImageListener((position, imageView) -> imageView.setImageBitmap(imagesToDisplay.get(position)));

        Either<String, Integer> miniatureMeta = currentRecipe.getMiniaturePath();
        if(miniatureMeta.isNone()) {
            imagesToDisplay.add(BitmapFactory.decodeResource(getActivity().getResources(), Recipe.DEFAULT_MINIATURE_PATH));
            carouselView.setPageCount(imagesToDisplay.size());
        } else if(miniatureMeta.isRight()) {
            imagesToDisplay.add(BitmapFactory.decodeResource(getActivity().getResources(), miniatureMeta.getRight()));
            carouselView.setPageCount(imagesToDisplay.size());
        } else {
            getImageStorage().getImage(miniatureMeta.getLeft(), this);
        }
        for(String path: currentRecipe.getPicturesPath()) {
            getImageStorage().getImage(path, this);
        }
    }

    /**
     * Display the recipe name in the correct field in the activity.
     */
    private void displayRecipeName(View view){
        TextView recipeName = view.findViewById(R.id.recipeName);
        recipeName.setText(currentRecipe.getName());
    }

    /**
     * Display the rating of the recipe in the correct field in the activity.
     */
    private void displayRating(View view){
        final RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ratingBar.setRating((float) currentRecipe.getRating().ratingAverage());
    }

    /**
     * Display both the preparation time and the cooking time in the correct field in the activity.
     */
    private void displayPrepAndCookTime(View view){
        String prepText = "Prep time : "+currentRecipe.getEstimatedPreparationTime()+" mins";
        TextView prepTime = view.findViewById(R.id.prepTime);
        prepTime.setText(prepText);
        String cookText = "Cook time : "+currentRecipe.getEstimatedCookingTime()+" mins";
        TextView cookTime = view.findViewById(R.id.cookTime);
        cookTime.setText(cookText);
    }

    /**
     * Display the difficulty in the correct field in the activity.
     */
    private void displayDifficulty(View view){
        TextView difficulty = view.findViewById(R.id.difficulty);
        String diff = currentRecipe.getRecipeDifficulty().toString();
        String finalDiffStr = "Difficulty : " + diff.substring(0, 1).toUpperCase(Locale.ENGLISH).concat(diff.substring(1, diff.length()).toLowerCase(Locale.ENGLISH).replaceAll("_", " "));
        difficulty.setText(finalDiffStr);
    }

    /**
     * Display all the ingredients from the list in a big string.
     */
    private void displayIngredients(View view){
        StringBuilder strBuilder = new StringBuilder();
        for(Ingredient ingredient: currentRecipe.getIngredients()){
            strBuilder.append("● ");
            strBuilder.append(ingredient.toString());
            strBuilder.append(NEW_LINE);
            strBuilder.append(NEW_LINE);
        }
        // Remove the last line return since there is no more ingredients to display
        strBuilder.deleteCharAt(strBuilder.length() - 1);
        TextView ingredients = view.findViewById(R.id.ingredient0);
        ingredients.setText(strBuilder.toString());
    }

    /**
     * Display all the instructions from the list in a big string.
     */
    private void displayInstructions(View view){
        StringBuilder strBuilder = new StringBuilder();
        List<String> allInstructions = currentRecipe.getRecipeInstructions();
        for(int i = 0; i < allInstructions.size(); i++){
            strBuilder.append(i + 1);
            strBuilder.append(". ");
            strBuilder.append(allInstructions.get(i));
            strBuilder.append(NEW_LINE);
            strBuilder.append(NEW_LINE);
        }
        // Remove the last line return since there is no more instructions to display
        strBuilder.deleteCharAt(strBuilder.length() - 1);
        TextView instructions = view.findViewById(R.id.instruction0);
        instructions.setText(strBuilder.toString());
    }

    @Override
    public void onSuccess(byte[] data) {
        imagesToDisplay.add(BitmapFactory.decodeByteArray(data, 0, data.length));
        carouselView.setPageCount(imagesToDisplay.size());
    }

    @Override
    public void notify(String data) {
        List<String> allInstructions = currentRecipe.getRecipeInstructions();

        if(indexOfInstruction==-1){
            indexOfInstruction=0;
        }else if(data.equals(getResources().getString(R.string.commandPrevious))){
            indexOfInstruction=Math.max(indexOfInstruction-1,0);
        }else if(data.equals(getResources().getString(R.string.commandNext))){
            indexOfInstruction=Math.min(indexOfInstruction+1,allInstructions.size());
        }

        if(indexOfInstruction==allInstructions.size()){
            voiceSynthesizer.speak("Congratulations you reached the end");
        }else{
            voiceSynthesizer.speak(allInstructions.get(indexOfInstruction));
        }
    }

    @Override
    public void onFailure() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.errorImageRetrieve), Toast.LENGTH_LONG).show();
    }

    public ImageStorage getImageStorage() {
        return ImageStorage.getInstance();
    }

    public UserStorage getUserStorage() {
        if(hostActivity != null) {
            return hostActivity.getUserStorage();
        } else {
            return UserStorage.getInstance();
        }
    }

    public RecyclerView getOpinionsRecyclerView(){
        return opinionsRecyclerView;
    }

    @Override
    public void onStop() {
        super.onStop();
        voiceRecognizer.onStop();
        voiceSynthesizer.onStop();
    }
}

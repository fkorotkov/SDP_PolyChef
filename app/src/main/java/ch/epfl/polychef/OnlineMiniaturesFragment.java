package ch.epfl.polychef;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


import ch.epfl.polychef.adaptersrecyclerview.RecipeMiniatureAdapter;
import ch.epfl.polychef.recipe.Recipe;

public class OnlineMiniaturesFragment extends Fragment implements FireHandler {

    private RecyclerView onlineRecyclerView;

    private List<Recipe> dynamicRecipeList = new ArrayList<>();

    private int currentReadInt = 1;

    private int nbOfRecipesLoadedAtATime = 5;

    private boolean isLoading = false;

    private Firebase firebase;

    public OnlineMiniaturesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_miniatures_online, container, false);

        Bundle bundle = getArguments();
        int fragmentID = bundle.getInt("fragmentID");
        onlineRecyclerView = view.findViewById(R.id.miniaturesOnlineList);
        onlineRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        onlineRecyclerView.setAdapter(new RecipeMiniatureAdapter(this.getActivity(), dynamicRecipeList, onlineRecyclerView, fragmentID));
        // Add a scroll listener when we reach the end of the list we load new recipes from database
        onlineRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1)){
                    if(isLoading){
                        return;
                    }
                    // add a certain number of recipes at this end of the actual list
                    for(int i = 0; i < nbOfRecipesLoadedAtATime; i++){
                        Firebase.readRecipeFromFirebase(currentReadInt, OnlineMiniaturesFragment.this);
                        currentReadInt++;
                    }
                    isLoading = true;
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // For now when we enter the page we load the offline recipes first
        // add a certain number of recipes at this end of the actual list
        for(int i = 0; i < nbOfRecipesLoadedAtATime; i++){
            Firebase.readRecipeFromFirebase(currentReadInt, OnlineMiniaturesFragment.this);
            currentReadInt++;
        }

    }

    @Override
    public void onSuccess(Recipe recipe) {
        // The data has been acquired from database we can now update the recyclerView
        isLoading = false;
        dynamicRecipeList.add(recipe);
        onlineRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onFailure() {
        isLoading = false;
    }

}

package ch.epfl.polychef.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import ch.epfl.polychef.R;
import ch.epfl.polychef.recipe.Opinion;
import ch.epfl.polychef.recipe.Recipe;
import ch.epfl.polychef.users.UserStorage;

public class OpinionsMiniatureAdapter extends RecyclerView.Adapter<OpinionsMiniatureAdapter.MiniatureViewHolder> {

    private Context mainContext;
    private List<Opinion> opinions;
    private RecyclerView recyclerView;
    private UserStorage userStorage;
    private Recipe recipe;

    public OpinionsMiniatureAdapter(Context mainContext, Recipe recipe, RecyclerView recyclerView, UserStorage userStorage){
        this.mainContext = mainContext;
        this.recyclerView = recyclerView;
        this.userStorage = userStorage;
        this.recipe = recipe;
        opinions = new ArrayList<>();
        for(Opinion opinion : recipe.getRating().getAllOpinion().values()){
            opinions.add(opinion);
        }
    }

    @NonNull
    @Override
    public MiniatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mainContext);
        View view = inflater.inflate(R.layout.comment_layout, null);
        view.setOnClickListener(new OpinionsMiniatureAdapter.MiniatureOnClickListener(recyclerView));
        return new OpinionsMiniatureAdapter.MiniatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniatureViewHolder holder, int position) {
        Opinion opinion = opinions.get(position);
        holder.rate.setRating(opinion.getRate());
        holder.commentText.setText(opinion.getComment());

        // Handle image
    }

    @Override
    public int getItemCount() {
        return opinions.size();
    }

    class MiniatureViewHolder extends RecyclerView.ViewHolder {

        RatingBar rate;
        ImageView profilePict;
        TextView commentText;

        public MiniatureViewHolder(@NonNull View itemView) {
            super(itemView);
            rate = itemView.findViewById(R.id.ratingCommentBar);
            profilePict = itemView.findViewById(R.id.commentProfilePict);
            commentText = itemView.findViewById(R.id.commentText);
        }
    }

    /**
     * This class is the listener that when we click on a miniature it swaps to the new fragment with the full recipe displayed.
     */
    class MiniatureOnClickListener implements View.OnClickListener {

        RecyclerView recyclerView;

        public MiniatureOnClickListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View view) {
            // Go to user page
        }
    }
}

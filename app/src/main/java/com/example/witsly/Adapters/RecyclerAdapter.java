package com.example.witsly.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.witsly.FirebaseActions;
import com.example.witsly.Fragments.ViewQuestion;
import com.example.witsly.Models.Post;
import com.example.witsly.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

  private final ArrayList<Post> mPostList;
  private final Context context;
  private OnItemClickListener mListener;
  private final FirebaseActions firebaseActions = new FirebaseActions();
  FirebaseDatabase firebase = FirebaseDatabase.getInstance();

  public interface OnItemClickListener {
    void onItemClick(int pos);
    // Please modify to add necessary information from dB i.e. Title, Body etc.
    // Alternatively, pass the ID of the post at current position and we can get the necessary info
    // Use method in HomeFragment to navigate to ViewQuestion activity
  }

  public void setOnItemClickListener(final OnItemClickListener listener) {
    mListener = listener;
  }

  static class RecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView mPosterDetails;
    TextView mPostTitle;
    TextView mPostBody;
    TextView mVoteCount;
    CardView card;
    private final RadioButton mUpVoteButton;
    private final RadioButton mDownVoteButton;

    RecyclerViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
      super(itemView);
      mPosterDetails = itemView.findViewById(R.id.tv_poster2);
      mPostTitle = itemView.findViewById(R.id.tv_card_title2);
      mPostBody = itemView.findViewById(R.id.tv_card_body2);
      mVoteCount = itemView.findViewById(R.id.tv_vote_count2);
      mUpVoteButton = itemView.findViewById(R.id.btn_upvote2);
      mDownVoteButton = itemView.findViewById(R.id.btn_downvote2);
      final RadioGroup mRadioGroup = itemView.findViewById(R.id.radioGroup);
      card = itemView.findViewById(R.id.cardView);

      itemView.setOnClickListener(
          v -> {
            if (listener != null) {
              final int pos = getAdapterPosition();
              if (pos != RecyclerView.NO_POSITION) listener.onItemClick(pos);
            }
          });

      mDownVoteButton.setOnClickListener(
          v -> {
            // Insert Logic for Votes
            // Do Stuff
            if (mDownVoteButton.isChecked())
              mVoteCount.setTextColor(
                  ResourcesCompat.getColor(v.getResources(), R.color.Red, null));
            else if (mUpVoteButton.isChecked()) {
              // Do Stuff
            } else {
              // Do Stuff
            }
          });

      mUpVoteButton.setOnClickListener(
          v -> {
            // Insert Logic for Votes
            if (mDownVoteButton.isChecked()) {
              // Do Stuff
            } else // Do Stuff
            if (mUpVoteButton.isChecked())
              mVoteCount.setTextColor(
                  ResourcesCompat.getColor(v.getResources(), R.color.Green, null));
            else {
              // Do Stuff
            }
          });
    }
  }

  public RecyclerAdapter(final ArrayList<Post> postList, final Context context) {
    mPostList = postList;
    this.context = context;
  }

  @NonNull
  @Override
  public RecyclerViewHolder onCreateViewHolder(
      @NonNull final ViewGroup parent, final int viewType) {
    final View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item2, parent, false);

    return new RecyclerViewHolder(v, mListener);
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, final int position) {

    final Post cItem = mPostList.get(position);

    if (cItem.getTitle().length() > 15)
      holder.mPostTitle.setText((cItem.getTitle()).substring(0, 15));
    else holder.mPostTitle.setText(cItem.getTitle());

    if (cItem.getBody().length() > 30) holder.mPostBody.setText((cItem.getBody()).substring(0, 30));
    else holder.mPostBody.setText(cItem.getTitle());

    holder.mUpVoteButton.setOnClickListener(
        up -> {
          firebase
              .getReference("Likes")
              .child(cItem.getPostID())
              .child(cItem.getUid())
              .setValue(true)
              .addOnCompleteListener(
                  task1 -> {
                    if (task1.isSuccessful()) {
                      Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
                    }
                  })
              .addOnFailureListener(
                  e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

          // Do Stuff

        });
    holder.mDownVoteButton.setOnClickListener(
        down -> {
          firebase
              .getReference("Likes")
              .child(cItem.getPostID())
              .child(cItem.getUid())
              .setValue(false)
              .addOnCompleteListener(
                  task1 -> {
                    if (task1.isSuccessful()) {
                      Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
                    }
                  })
              .addOnFailureListener(
                  e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    firebaseActions.getUserDetails(
        cItem.getUid(),
        value -> {
          // value.getTag()

          // pass the tag we get
          holder.mPosterDetails.setText(
              "Posted by "
                  + value.getName()
                  + " "
                  + value.getSurname()
                  + " on: "
                  + (cItem.getDate()).substring(0, 10));
        });

    holder.mVoteCount.setText(cItem.getVote() + "");
    holder.card.setOnClickListener(
        v -> {
          final Bundle bundle = new Bundle();
          bundle.putString("postID", cItem.getPostID());

          final AppCompatActivity activity = (AppCompatActivity) context;

          final Fragment viewQuestion = new ViewQuestion();
          viewQuestion.setArguments(bundle);
          final FragmentTransaction transaction =
              activity.getSupportFragmentManager().beginTransaction();
          transaction.replace(R.id.container_frag, viewQuestion);

          transaction.addToBackStack(null);
          transaction.commit();
        });
  }

  @Override
  public int getItemCount() {
    return mPostList.size();
  }
}

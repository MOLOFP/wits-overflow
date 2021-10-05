package com.example.witsly.Firebase;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.witsly.Activities.votesGlobalVar;
import com.example.witsly.Interfaces.AddAnswer;
import com.example.witsly.Interfaces.AddComment;
import com.example.witsly.Interfaces.AddPost;
import com.example.witsly.Interfaces.AddTag;
import com.example.witsly.Interfaces.AddTopic;
import com.example.witsly.Interfaces.DeletePost;
import com.example.witsly.Interfaces.FirebaseAuthHandler;
import com.example.witsly.Interfaces.GetAllPosts;
import com.example.witsly.Interfaces.GetAnswers;
import com.example.witsly.Interfaces.GetBio;
import com.example.witsly.Interfaces.GetComments;
import com.example.witsly.Interfaces.GetPost;
import com.example.witsly.Interfaces.GetReputation;
import com.example.witsly.Interfaces.GetTags;
import com.example.witsly.Interfaces.GetTopics;
import com.example.witsly.Interfaces.GetUser;
import com.example.witsly.Interfaces.MarkPost;
import com.example.witsly.Interfaces.UpdateBio;
import com.example.witsly.Interfaces.UploadImg;
import com.example.witsly.Interfaces.UserDetails;
import com.example.witsly.Interfaces.getProfileImage;
import com.example.witsly.Models.Answer;
import com.example.witsly.Models.Comment;
import com.example.witsly.Models.Post;
import com.example.witsly.Models.Tag;
import com.example.witsly.Models.Topic;
import com.example.witsly.Models.User;
import com.example.witsly.Utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FirebaseActions {
  private static final String TAG = "LOG";
  private final FirebaseDatabase firebaseDatabase;
  private ArrayList<Post> postArrayList;
  private ArrayList<Answer> answersArrayList;
  private ArrayList<Comment> commentsArrayList;
  private ArrayList<Tag> tagArrayList;
  private ArrayList<Topic> topicArrayList;
  private FirebaseStorage storage;
  private FirebaseUser currentUser;

  public FirebaseActions() {
    firebaseDatabase = FirebaseDatabase.getInstance();
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    storage = FirebaseStorage.getInstance();
  }

  public void getUser(GetUser user) {
    DatabaseReference mDatabaseReference =
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.USERS);
    mDatabaseReference
        .child(currentUser.getUid())
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                User value = snapshot.getValue(User.class);
                user.processResponse(value, true);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                user.processResponse(null, false);
              }
            });
  }

  public void addPost(Post post, AddPost a) {

    DatabaseReference rel = firebaseDatabase.getReference(FirebaseUtils.POSTS).push();

    rel.setValue(post)
        .addOnCompleteListener(
            c -> {
              if (c.isSuccessful()) a.processResponse(true, rel.getKey());
            })
        .addOnFailureListener(e -> a.processResponse(false, null));
  }

  public void markPost(String pid, MarkPost mp) {

    DatabaseReference solved;
    solved = firebaseDatabase.getReference(FirebaseUtils.POSTS).child(pid).child("solved");

    solved.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              solved.setValue(!(Boolean) snapshot.getValue());
              mp.processResponse(!(Boolean) snapshot.getValue());
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void markAnswer(String pid, String aid) {

    DatabaseReference answer =
        firebaseDatabase.getReference(FirebaseUtils.ANSWERS).child(aid).child("correct");
    DatabaseReference post =
        firebaseDatabase.getReference(FirebaseUtils.POSTS).child(pid).child("solved");

    answer.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) post.setValue(!(Boolean) snapshot.getValue());
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });

    post.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) post.setValue(!(Boolean) snapshot.getValue());
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void getPost(String pid, GetPost g) {

    firebaseDatabase
        .getReference(FirebaseUtils.POSTS)
        .child(pid)
        .addValueEventListener(
            new ValueEventListener() {
              @SuppressLint("SetTextI18n")
              @Override
              public void onDataChange(@NotNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                  Post post = snapshot.getValue(Post.class);
                  assert post != null;
                  getUserDetails(post.getUID(), user -> g.processResponse(post, user));
                }
              }

              @Override
              public void onCancelled(@NotNull DatabaseError error) {}
            });
  }

  public void getAllPost(GetAllPosts g) {

    postArrayList = new ArrayList<>();
    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseUtils.POSTS);
    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            postArrayList.clear();
            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

              Post post = postSnapshot.getValue(Post.class);
              assert post != null;
              post.setPID(postSnapshot.getKey());

              postArrayList.add(post);
            }
            g.processResponse(postArrayList);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void getComments(String AnswerID, GetComments c) {
    commentsArrayList = new ArrayList<>();
    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseUtils.COMMENTS);
    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            commentsArrayList.clear();

            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

              Comment comment = postSnapshot.getValue(Comment.class);
              assert comment != null;
              comment.setCID(postSnapshot.getKey());

              if (comment.getAID().equals(AnswerID)) commentsArrayList.add(comment);
            }
            c.processResponse(commentsArrayList);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void getAnswers(String pid, GetAnswers a) {

    answersArrayList = new ArrayList<>();
    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseUtils.ANSWERS);
    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            answersArrayList.clear();

            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

              Answer answer = postSnapshot.getValue(Answer.class);
              String aid = postSnapshot.getKey();
              assert answer != null;
              answer.setAID(aid);

              if (answer.getPID().equals(pid)) answersArrayList.add(answer);
            }
            a.processResponse(answersArrayList);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void addAnswer(Answer answer, AddAnswer a) {
    firebaseDatabase
        .getReference(FirebaseUtils.ANSWERS)
        .push()
        .setValue(answer)
        .addOnCompleteListener(
            c -> {
              if (c.isSuccessful()) a.processResponse(true);
            })
        .addOnFailureListener(
            e -> {
              a.processResponse(false);
              Log.d(TAG, e.getMessage());
            });
  }

  public void addComment(Comment comment, AddComment a) {
    firebaseDatabase
        .getReference(FirebaseUtils.COMMENTS)
        .push()
        .setValue(comment)
        .addOnCompleteListener(
            c -> {
              if (c.isSuccessful()) a.processResponse(true);
            })
        .addOnFailureListener(
            e -> {
              a.processResponse(false);
              Log.d(TAG, e.getMessage());
            });
  }

  public void upVotePost(String pid, String uid) {
    DatabaseReference likes =
        firebaseDatabase.getReference(FirebaseUtils.LIKES).child(pid).child(uid);
    DatabaseReference vote =
        firebaseDatabase.getReference(FirebaseUtils.POSTS).child(pid).child(FirebaseUtils.VOTE);
    likes.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              likes.removeValue();
              vote.addListenerForSingleValueEvent(
                  new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      vote.setValue((long) snapshot.getValue() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                  });

            } else {
              likes.setValue(true);
              vote.addListenerForSingleValueEvent(
                  new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      vote.setValue((long) snapshot.getValue() + 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                  });
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void UpdateBIO(String userBio, UpdateBio b) {

    DatabaseReference bio =
        firebaseDatabase
            .getReference(FirebaseUtils.USERS)
            .child(currentUser.getUid())
            .child(FirebaseUtils.BIO);

    bio.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            bio.setValue(userBio);
            b.processResponse(true);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            b.processResponse(false);
          }
        });
  }

  public void AddBio(String userBio) {
    DatabaseReference bio =
        firebaseDatabase
            .getReference(FirebaseUtils.USERS)
            .child(currentUser.getUid())
            .child(FirebaseUtils.BIO);

    bio.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            bio.setValue(userBio);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public String getUid() {
    return currentUser.getUid();
  }

  public void AddReputation() {
    DatabaseReference rep =
        firebaseDatabase
            .getReference(FirebaseUtils.USERS)
            .child(currentUser.getUid())
            .child(FirebaseUtils.REPUTATION);

    // reputation (Answers)
    votesGlobalVar.totsum = 0;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Answers");
    reference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            votesGlobalVar.Asum = 0;

            for (DataSnapshot answersnapshot : snapshot.getChildren()) {
              int vote = Integer.parseInt(String.valueOf(answersnapshot.child("vote").getValue()));
              String uid = answersnapshot.child("uid").getValue(String.class);

              if (uid.equals(currentUser.getUid())) votesGlobalVar.Asum += vote;
            }

            // reputation (questions)
            DatabaseReference reference2 =
                FirebaseDatabase.getInstance().getReference().child("Posts");
            reference2.addValueEventListener(
                new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                    votesGlobalVar.Psum = 0;
                    for (DataSnapshot postsnapshot : snapshot.getChildren()) {
                      int qvote =
                          Integer.parseInt(String.valueOf(postsnapshot.child("vote").getValue()));
                      String quid = postsnapshot.child("uid").getValue(String.class);

                      if (quid.equals(currentUser.getUid())) votesGlobalVar.Psum += qvote;
                    }
                    votesGlobalVar.totsum = 0;
                    votesGlobalVar.totsum = votesGlobalVar.Asum + votesGlobalVar.Psum;
                    rep.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot snapshot) {

                            rep.setValue(votesGlobalVar.totsum + " points");
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    Log.d("TAG", "question votes : " + votesGlobalVar.Psum);
                    Log.d("TAG", "total votes : " + votesGlobalVar.totsum);
                    // tvReputation.setText( votesGlobalVar.totsum + " points");
                    Log.d("TAG", "answer points : " + votesGlobalVar.Asum);
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {}
                });
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void getReputation(GetReputation a) {
    DatabaseReference mDatabaseReference =
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.USERS);
    mDatabaseReference
        .child(currentUser.getUid())
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                  User userB = snapshot.getValue(User.class);
                  assert userB != null;
                  a.processResponse(userB);
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  public void getBio(GetBio b) {
    DatabaseReference mDatabaseReference =
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.USERS);
    mDatabaseReference
        .child(currentUser.getUid())
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bio = snapshot.child(FirebaseUtils.BIO).getValue(String.class);

                if (snapshot.exists()) {
                  User userB = snapshot.getValue(User.class);
                  assert userB != null;
                  getUserDetails(userB.getBio(), user -> b.processResponse(userB));
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  public void upVoteAnswer(String pid) {
    DatabaseReference likes =
        firebaseDatabase.getReference(FirebaseUtils.LIKES).child(pid).child(currentUser.getUid());
    DatabaseReference vote =
        firebaseDatabase.getReference(FirebaseUtils.ANSWERS).child(pid).child(FirebaseUtils.VOTE);
    likes.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              likes.removeValue();
              vote.addListenerForSingleValueEvent(
                  new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      vote.setValue((long) snapshot.getValue() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                  });

            } else {
              likes.setValue(true);
              vote.addListenerForSingleValueEvent(
                  new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      vote.setValue((long) snapshot.getValue() + 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                  });
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void addTag(Tag tag, AddTag t) {

    DatabaseReference rel = firebaseDatabase.getReference(FirebaseUtils.TAGS).push();

    rel.setValue(tag)
        .addOnCompleteListener(
            c -> {
              if (c.isSuccessful()) t.processResponse(rel.getKey());
            })
        .addOnFailureListener(e -> t.processResponse(null));
  }

  public void getTags(GetTags t) {
    tagArrayList = new ArrayList<>();
    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseUtils.TAGS);
    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            tagArrayList.clear();
            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

              Tag tag = postSnapshot.getValue(Tag.class);
              assert tag != null;
              tag.setTagID(postSnapshot.getKey());
              tagArrayList.add(tag);
            }
            t.processResponse(tagArrayList);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void addTopic(Topic topic, AddTopic t) {

    DatabaseReference rel = firebaseDatabase.getReference(FirebaseUtils.TOPICS).push();

    rel.setValue(topic)
        .addOnCompleteListener(
            c -> {
              if (c.isSuccessful()) t.processResponse(rel.getKey());
            })
        .addOnFailureListener(e -> t.processResponse(null));
  }

  public void getTopics(GetTopics t) {
    topicArrayList = new ArrayList<>();
    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseUtils.TOPICS);
    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            topicArrayList.clear();
            for (DataSnapshot postSnapshot : snapshot.getChildren()) {

              Topic topic = postSnapshot.getValue(Topic.class);
              assert topic != null;
              topic.setTopicID(postSnapshot.getKey());
              topicArrayList.add(topic);
            }
            t.processResponse(topicArrayList);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void isCurrentUserAdmin(FirebaseAuthHandler f) {

    DatabaseReference mDatabaseReference =
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.USERS);
    mDatabaseReference
        .child(currentUser.getUid())
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                User value = snapshot.getValue(User.class);
                assert value != null;
                f.processAuth(value.isAdmin(), null);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  public void getUserDetails(String user, UserDetails f) {
    DatabaseReference mDatabaseReference =
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.USERS);
    mDatabaseReference
        .child(user)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                User value = snapshot.getValue(User.class);

                f.processResponse(value);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  public void deletePost(String postKey, DeletePost dp) {
    DatabaseReference clickPost =
        firebaseDatabase.getReference().child(FirebaseUtils.POSTS).child(postKey);
    clickPost.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              clickPost.removeValue();
              dp.processResponse(true);
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void uploadPicture(Uri uri, UploadImg img) {

    DatabaseReference imgUrl =
        firebaseDatabase
            .getReference(FirebaseUtils.USERS)
            .child(currentUser.getUid())
            .child(FirebaseUtils.IMAGE);

    StorageReference storageReference = storage.getReference();
    StorageReference reference =
        storageReference.child(FirebaseUtils.IMG_PATH + currentUser.getUid());
    reference
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot ->
                imgUrl.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reference
                            .getDownloadUrl()
                            .addOnSuccessListener(
                                downloadUrl -> {
                                  imgUrl.setValue(downloadUrl.toString());
                                  img.processResponse(downloadUrl.toString());
                                });
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {
                        img.processResponse(null);
                      }
                    }))
        .addOnFailureListener(e -> img.processResponse(null));
  }

  public void getProfilePic(getProfileImage image) {

    StorageReference storageReference = storage.getReference();
    StorageReference pathReference =
        storageReference.child(FirebaseUtils.IMG_PATH + currentUser.getUid());

    pathReference
        .getDownloadUrl()
        .addOnSuccessListener(image::processResponse)
        .addOnFailureListener(exception -> {});
  }

  public void deleteAnswer(String postKey, DeletePost dp) {
    DatabaseReference clickPost =
        firebaseDatabase.getReference().child(FirebaseUtils.ANSWERS).child(postKey);
    clickPost.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              clickPost.removeValue();
              dp.processResponse(true);
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  public void getUserProfileImg(String UserID, getProfileImage p) {
    StorageReference storageReference = storage.getReference();
    StorageReference pathReference = storageReference.child(FirebaseUtils.IMG_PATH + UserID);

    pathReference
        .getDownloadUrl()
        .addOnSuccessListener(p::processResponse)
        .addOnFailureListener(exception -> {});
  }

  public void deleteComment(String postKey, DeletePost dp) {
    DatabaseReference clickPost =
        firebaseDatabase.getReference().child(FirebaseUtils.COMMENTS).child(postKey);
    clickPost.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {
              clickPost.removeValue();
              dp.processResponse(true);
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }
}

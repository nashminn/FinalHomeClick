package csedu.homeclick.androidhomeclick.database;


import android.net.Uri;
import android.util.Log;



import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import csedu.homeclick.androidhomeclick.connector.AdInterface;
import csedu.homeclick.androidhomeclick.connector.UserInterface;
import csedu.homeclick.androidhomeclick.structure.Advertisement;
import csedu.homeclick.androidhomeclick.structure.RentAdvertisement;
import csedu.homeclick.androidhomeclick.structure.SaleAdvertisement;
import csedu.homeclick.androidhomeclick.structure.User;

public class FirestoreDealer implements AdInterface, UserInterface {
    public static final String TAG = "FirestoreDealer";
    public static final String AD_TABLE = "Advertisement";
    public static final String USER_TABLE = "Users";
    public static final String UPLOADS_FOLDER = "uploads";
    public static final String SERVER_ENTRY_TIME = "postDate";

    private static FirestoreDealer fd;
    private final FirebaseFirestore db;

    private QueryDocumentSnapshot lastLoadedAdSnapshot;


    private FirestoreDealer() {
        db = FirebaseFirestore.getInstance();
        lastLoadedAdSnapshot = null;
    }

    public static FirestoreDealer getInstance() {
        if(fd == null) {
            fd = new FirestoreDealer();
        }
        return fd;
    }

    public void addUser(User userMap) {
        if(UserAuth.isSignedIn()) {
            db.collection(USER_TABLE).document(Objects.requireNonNull(UserAuth.getCurrentUserUID())).set(userMap)
                    .addOnSuccessListener(unused -> Log.i("add user", "successful"))
                    .addOnFailureListener(e -> Log.i("add user", "failed"));
        }
    }

    @Override
    public void findUserInfo(OnUserInfoListener<User> onUserInfoListener, String UID) {
        DocumentReference docRef = db.collection(USER_TABLE).document(UID);
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                try {
                    onUserInfoListener.OnUserInfoFound(Objects.requireNonNull(task.getResult()).toObject(User.class));
                } catch(Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            } else {
                if(task.getException() != null)
                    Log.i(TAG,  task.getException().getMessage());
            }

        });
    }

    @Override
    public void updateUserInfo(OnUserInfoUpdateListener<User> onUserInfoUpdateListener, User updatedUser) {
        DocumentReference docRef = db.collection(USER_TABLE).document(updatedUser.getUID());
        docRef.set(updatedUser).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                onUserInfoUpdateListener.OnUserInfoUpdated();
            } else {
                onUserInfoUpdateListener.OnUserInfoUpdateFailed();
            }
        });
    }


    @Override
    public void getAdId(final OnAdIdListener<Advertisement> onAdIdListener) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("UserUID", UserAuth.getCurrentUserUID());

        db.collection("AdID").add(map)
                .addOnSuccessListener(documentReference -> {
                    String adID = documentReference.getId();
                    onAdIdListener.onAdIdObtained(adID);
                })
                .addOnFailureListener(e -> Log.i(TAG, e.getMessage()));
    }

    @Override
    public void setLastFetchedQuerySnapshot(QueryDocumentSnapshot querySnapshot) {
        this.lastLoadedAdSnapshot = querySnapshot;
    }

    @Override
    public void uploadPhoto(Uri uri, String fileExtension, String pathId, OnPhotoUploadListener<String> onPhotoUploadListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String fileName = System.currentTimeMillis() + "." + fileExtension;
        StorageReference ref = storage.getReference(UPLOADS_FOLDER).child(pathId).child(fileName);

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri1 -> onPhotoUploadListener.onCompleteNotify(uri1.toString())))
                .addOnProgressListener(snapshot -> {
                    double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    onPhotoUploadListener.ongoingProgress((int) percent);
                });
    }

    @Override
    public void pushAdIntoDatabase(final Advertisement advert, final OnAdPostSuccessListener<Boolean> onAdPostSuccessListener) {
        db.collection(AD_TABLE).document(advert.getAdvertisementID()).set(advert)
                .addOnSuccessListener(unused -> onAdPostSuccessListener.OnAdPostSuccessful(true))
                .addOnFailureListener(e -> onAdPostSuccessListener.OnAdPostFailed(e.getMessage()));
    }

    @Override
    public void getAdsFromDatabase(final OnAdsFetchedListener<List<Advertisement>> onAdsFetchedListener) {
        Task<QuerySnapshot> getTenAds;

        if(this.lastLoadedAdSnapshot == null) {
            getTenAds = db.collection(AD_TABLE).orderBy(SERVER_ENTRY_TIME, Query.Direction.DESCENDING).limit(10)
                    .get();

            getTenAds
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Advertisement> adList = new ArrayList<>();
                        int total = queryDocumentSnapshots.size();
                        int count = 0;
                        for(QueryDocumentSnapshot singleAd: queryDocumentSnapshots) {
                            count++;
                            adList.add(singleAd.toObject(Advertisement.class));
                            if(count == total) {
                                FirestoreDealer.this.setLastFetchedQuerySnapshot(singleAd);
                                Log.i(TAG, "snapshot change detected, size = " + count);
                            }
                        }

                        onAdsFetchedListener.OnAdsFetchedSuccessfully(adList);
                    })
                    .addOnFailureListener(e -> onAdsFetchedListener.OnAdFetchingFailed(e.getMessage()));
        } else {
            getTenAds = db.collection(AD_TABLE)
                    .orderBy(SERVER_ENTRY_TIME, Query.Direction.DESCENDING).startAfter(lastLoadedAdSnapshot).limit(10)
                    .get();

            getTenAds
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Advertisement> adList = new ArrayList<>();

                        int total = queryDocumentSnapshots.size();
                        int count = 0;

                        for(QueryDocumentSnapshot singleAd: queryDocumentSnapshots) {
                            count++;
                            adList.add(singleAd.toObject(Advertisement.class));
                            if(count == total) {
                                Log.i(TAG, "snapshot change detected, size = " + count);
                                FirestoreDealer.this.setLastFetchedQuerySnapshot(singleAd);
                            }
                        }

                        onAdsFetchedListener.OnAdsFetchedSuccessfully(adList);
                    })
                    .addOnFailureListener(e -> onAdsFetchedListener.OnAdFetchingFailed(e.getMessage()));

        }
    }

    @Override
    public void getFilteredAdsFromDatabase(OnAdsFetchedListener<List<Advertisement>> onFilteredAdsFetchedListener, Query filterQuery) {
        Task<QuerySnapshot> getTenAds;

        if(this.lastLoadedAdSnapshot == null) {
            getTenAds = filterQuery.orderBy(SERVER_ENTRY_TIME, Query.Direction.DESCENDING).limit(10).get();

            getTenAds
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Advertisement> adList = new ArrayList<>();
                        int total = queryDocumentSnapshots.size();
                        int count = 0;
                        for(QueryDocumentSnapshot singleAd: queryDocumentSnapshots) {
                            count++;
                            adList.add(singleAd.toObject(Advertisement.class));
                            if(count == total) {
                                FirestoreDealer.this.setLastFetchedQuerySnapshot(singleAd);
                                Log.i(TAG, "snapshot change detected, size = " + count);
                            }
                        }

                        onFilteredAdsFetchedListener.OnAdsFetchedSuccessfully(adList);
                    })
                    .addOnFailureListener(e -> onFilteredAdsFetchedListener.OnAdFetchingFailed(e.getMessage()));
        } else {
            getTenAds = filterQuery.orderBy(SERVER_ENTRY_TIME, Query.Direction.DESCENDING).limit(10).startAfter(lastLoadedAdSnapshot).get();

            getTenAds
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Advertisement> adList = new ArrayList<>();

                        int total = queryDocumentSnapshots.size();
                        int count = 0;

                        for(QueryDocumentSnapshot singleAd: queryDocumentSnapshots) {
                            count++;
                            adList.add(singleAd.toObject(Advertisement.class));
                            if(count == total) {
                                Log.i(TAG, "snapshot change detected, size = " + count);
                                FirestoreDealer.this.setLastFetchedQuerySnapshot(singleAd);
                            }
                        }

                        onFilteredAdsFetchedListener.OnAdsFetchedSuccessfully(adList);
                    })
                    .addOnFailureListener(e -> onFilteredAdsFetchedListener.OnAdFetchingFailed(e.getMessage()));

        }
    }

    @Override
    public void getMyAds(OnPersonalAdsFetchedListener<List<Advertisement>> onPersonalAdsFetchedListener, final String UserUID) {
        Task<QuerySnapshot> fetchingPersonalAds = db.collection(AD_TABLE).orderBy(SERVER_ENTRY_TIME, Query.Direction.DESCENDING).whereEqualTo("advertiserUID", UserUID).get();

        fetchingPersonalAds
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> adList = new ArrayList<>();

                    for(QueryDocumentSnapshot singleAd: queryDocumentSnapshots) {
                        adList.add(singleAd.toObject(Advertisement.class));
                    }

                    onPersonalAdsFetchedListener.OnPersonalAdsFetched(adList);
                })
                .addOnFailureListener(e -> Log.i(TAG, e.getMessage()));
    }

    @Override
    public void getThisRentAd(final OnParticularAdFetchedListener<RentAdvertisement> onParticularAdFetchedListener, String advertID) {
        db.collection(AD_TABLE).document(advertID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        onParticularAdFetchedListener.OnParticularAdFetched(Objects.requireNonNull(task.getResult()).toObject(RentAdvertisement.class));
                    }
                });
    }

    @Override
    public void getThisSaleAd(OnParticularAdFetchedListener<SaleAdvertisement> onParticularAdFetchedListener, String advertID) {
        db.collection(AD_TABLE).document(advertID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        onParticularAdFetchedListener.OnParticularAdFetched(Objects.requireNonNull(task.getResult()).toObject(SaleAdvertisement.class));
                    }
                });
    }

    @Override
    public void deletePhotoFolder(final String folderName, final OnPhotoFolderDeletedListener<Boolean> onPhotoFolderDeletedListener) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference ref = firebaseStorage.getReference(UPLOADS_FOLDER).child(folderName);
        Log.i(TAG, "folderName = " + folderName);


        ref.listAll()
                .addOnSuccessListener(listResult -> {
                    List<StorageReference> itemList = listResult.getItems();
                    final int toDelete = itemList.size();
                    final int[] count = new int[1];

                    Log.i(TAG, "total to delete = " + toDelete);


                    for( StorageReference item: itemList ) {

                        item.delete()
                                .addOnSuccessListener(unused -> {
                                    count[0]++;
                                    Log.i(TAG, "in here " + count[0]);
                                    if(count[0] == toDelete) {
                                        Log.i(TAG, "total deleted");
                                        onPhotoFolderDeletedListener.OnPhotoFolderDeleted(true, "");
                                    }
                                })
                                .addOnFailureListener(e -> onPhotoFolderDeletedListener.OnPhotoFolderDeleted(false, e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> onPhotoFolderDeletedListener.OnPhotoFolderDeleted(false, e.getMessage()));
    }

    @Override
    public void deleteParticularAd(String id, OnAdDeletedListener<Boolean> onAdDeletedListener) {
        DocumentReference docRef = db.collection(AD_TABLE).document(id);
        final DocumentReference adIdRef = db.collection("AdID").document(id);
        docRef.delete()
                .addOnSuccessListener(unused -> adIdRef.delete()
                        .addOnSuccessListener(unused1 -> onAdDeletedListener.OnAdDeleted(true, ""))
                        .addOnFailureListener(e -> onAdDeletedListener.OnAdDeleted(false, e.getMessage())))
                .addOnFailureListener(e -> onAdDeletedListener.OnAdDeleted(false, e.getMessage()));
    }

    @Override
    public void editParticularAd(final String id, final Advertisement advertisement, final OnAdEditListener<Boolean> onAdEditListener) {
        DocumentReference docRef = db.collection(AD_TABLE).document(id);

        docRef.set(advertisement)
                .addOnSuccessListener(unused -> onAdEditListener.OnAdEdited(true, ""))
                .addOnFailureListener(e -> onAdEditListener.OnAdEdited(false, e.getMessage()));
    }

    @Override
    public void getBookmarkedAds(final String id, final OnBookmarkedAdsFetchListener<List<Advertisement>> onBookmarkedAdsFetchListener) {
        CollectionReference adCollectionRef = db.collection(AD_TABLE);
        Query fetchingBookmarkedAds = adCollectionRef.whereArrayContains("bookmarkedBy", id);

        fetchingBookmarkedAds.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> bookmarkList = new ArrayList<>();

                    for(QueryDocumentSnapshot snap: queryDocumentSnapshots) {
                        bookmarkList.add(snap.toObject(Advertisement.class));
                    }

                    onBookmarkedAdsFetchListener.OnBookmarkedAdsFetched(bookmarkList, null);
                })
                .addOnFailureListener(e -> onBookmarkedAdsFetchListener.OnBookmarkedAdsFetched(null, e.getMessage()));
    }

}

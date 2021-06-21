package csedu.homeclick.androidhomeclick.connector;

import android.net.Uri;

import java.util.List;

import csedu.homeclick.androidhomeclick.structure.Advertisement;
import csedu.homeclick.androidhomeclick.structure.RentAdvertisement;
import csedu.homeclick.androidhomeclick.structure.SaleAdvertisement;

public interface AdInterface {

    //obtaining ad id through one push
    void getAdId(OnAdIdListener<Advertisement> onAdIdListener);

    interface OnAdIdListener<T> {
        void onAdIdObtained(String adId);
    }




    //uploading a single photo
    void uploadPhoto(Uri uri, String fileExtension, String pathId, OnPhotoUploadListener<String> onPhotoUploadListener);

    interface OnPhotoUploadListener<T> {
        void ongoingProgress(int percentage);
        void onCompleteNotify(String downloadUrl);
    }




    //was the ad pushed successfully into the database
    void pushAdIntoDatabase(Advertisement advert, OnAdPostSuccessListener<Boolean> onAdPostSuccessListener);

    interface  OnAdPostSuccessListener<T> {
        void OnAdPostSuccessful(Boolean data);
        void OnAdPostFailed(String error);
    }



    void getAdsFromDatabase(OnAdsFetchedListener<List<Advertisement>> onAdsFetchedListener);

    interface OnAdsFetchedListener<T> {
        void OnAdsFetchedListener(T ads);
    }

    void getMyAds(OnPersonalAdsFetchedListener<List<Advertisement>> onPersonalAdsFetchedListener, String userUID);

    interface OnPersonalAdsFetchedListener<T> {
        void OnPersonalAdsFetchedListener(T ads);
    }

    void getThisRentAd(OnParticularAdFetchedListener<RentAdvertisement> onParticularAdFetchedListener, String advertID);
    void getThisSaleAd(OnParticularAdFetchedListener<SaleAdvertisement> onParticularAdFetchedListener, String advertID);

    interface OnParticularAdFetchedListener<T> {
        void OnParticularAdFetched(T advert);
    }


    //delete folder containing photos of a particular ad
    void deletePhotoFolder(String folderName, int toDelete, OnPhotoFolderDeletedListener<Boolean> onPhotoFolderDeletedListener);

    interface OnPhotoFolderDeletedListener<T> {
        void OnPhotoFolderDeleted(T deleted, String error);
    }


    //delete a particular ad from the database
    void deleteParticularAd(String id,  OnAdDeletedListener<Boolean> onAdDeletedListener);

    interface  OnAdDeletedListener<T> {
        void OnAdDeleted(T deleted, String error);
    }
}

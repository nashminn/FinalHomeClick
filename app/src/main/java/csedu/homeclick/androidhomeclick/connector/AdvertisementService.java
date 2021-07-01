package csedu.homeclick.androidhomeclick.connector;

import android.net.Uri;

import java.util.List;

import csedu.homeclick.androidhomeclick.database.FirestoreDealer;
import csedu.homeclick.androidhomeclick.database.QueryBuilder;
import csedu.homeclick.androidhomeclick.structure.Advertisement;
import csedu.homeclick.androidhomeclick.structure.FilterCriteria;
import csedu.homeclick.androidhomeclick.structure.RentAdvertisement;
import csedu.homeclick.androidhomeclick.structure.SaleAdvertisement;

public class AdvertisementService {
    private UserService userService;
    private AdInterface adDealer;

    public AdvertisementService() {
        userService = new UserService();
        adDealer = FirestoreDealer.getInstance();
    }

    public void refreshAds() {
        adDealer.setLastFetchedQuerySnapshot(null);
    }

    public void getAdId(AdInterface.OnAdIdListener<Advertisement> onAdIdListener) {
        adDealer.getAdId(onAdIdListener);
    }

    public void uploadPhoto(Uri uri, String fileExtension, String pathID, AdInterface.OnPhotoUploadListener<String> onPhotoUploadListener) {
        adDealer.uploadPhoto(uri, fileExtension, pathID, onPhotoUploadListener);
    }

    public void completeAdPost(Advertisement advert, AdInterface.OnAdPostSuccessListener<Boolean> onAdPostSuccessListener) {
        adDealer.pushAdIntoDatabase(advert, onAdPostSuccessListener);
    }


    public void fetchAdvertisements(AdInterface.OnAdsFetchedListener<List<Advertisement>> onAdsFetchedListener) {
        adDealer.getAdsFromDatabase(onAdsFetchedListener);
    }

    public void fetchMyAds(AdInterface.OnPersonalAdsFetchedListener<List<Advertisement>> onPersonalAdsFetchedListener) {
        adDealer.getMyAds(onPersonalAdsFetchedListener, userService.getUserUID());
    }


    //get ads for ad details showing
    public void getRentAd(AdInterface.OnParticularAdFetchedListener<RentAdvertisement> onParticularAdFetchedListener, String adID) {
        adDealer.getThisRentAd(onParticularAdFetchedListener, adID);
    }
    public void getSaleAd(AdInterface.OnParticularAdFetchedListener<SaleAdvertisement> onParticularAdFetchedListener, String advertisementID) {
        adDealer.getThisSaleAd(onParticularAdFetchedListener, advertisementID);
    }

    public void deletePhotoFolder(String folder, AdInterface.OnPhotoFolderDeletedListener<Boolean> onPhotoFolderDeletedListener) {
        adDealer.deletePhotoFolder(folder, onPhotoFolderDeletedListener);
    }

    public void deleteAd(String id, AdInterface.OnAdDeletedListener<Boolean> onAdDeletedListener) {
        adDealer.deleteParticularAd(id, onAdDeletedListener);
    }

    public void editAd(String id, Advertisement advertisement, AdInterface.OnAdEditListener<Boolean> onAdEditListener) {
        adDealer.editParticularAd(id, advertisement, onAdEditListener);
    }

    public void fetchFilteredAds(AdInterface.OnAdsFetchedListener<List<Advertisement>> onAdsFetchedListener, FilterCriteria filterCriteria) {
        QueryBuilder filterQueryBuilder = new QueryBuilder(filterCriteria);
        adDealer.getFilteredAdsFromDatabase(onAdsFetchedListener, filterQueryBuilder.buildQuery());
    }

    public void fetchBookmarkedAds(AdInterface.OnBookmarkedAdsFetchListener<List<Advertisement>> onBookmarkedAdsFetchListener) {
        adDealer.getBookmarkedAds(userService.getUserUID(), onBookmarkedAdsFetchListener);
    }
}

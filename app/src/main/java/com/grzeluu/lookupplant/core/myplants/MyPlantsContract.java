package com.grzeluu.lookupplant.core.myplants;

import com.grzeluu.lookupplant.base.BaseListenerContract;
import com.grzeluu.lookupplant.base.BasePresenterContract;
import com.grzeluu.lookupplant.base.BaseViewContract;
import com.grzeluu.lookupplant.model.UserPlant;

import java.util.List;

public interface MyPlantsContract {
    interface View extends BaseViewContract {
        void updateMyPlants(List<UserPlant> plantList);

        void setNewNotificationForPlant(UserPlant plant);

        void plantDeleted(String id);
    }

    interface Presenter extends BasePresenterContract {
        void getMyPlantsList();

        void deletePlant(UserPlant plant);

        void updatePlantNeeds(UserPlant plant, boolean isWatered, boolean isFertilized, boolean isSprayed);
    }

    interface Interactor {
        void fetchMyPlantList();

        void performDeletePlant(UserPlant plant);

        void performUpdatePlantNeeds(UserPlant plant, boolean isWatered, boolean isFertilized, boolean isSprayed);
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(List<UserPlant> plantList);

        void onFailure(String message);

        void onSuccess(int message);
        
        void onSuccess(UserPlant plant);

        void onPlantDeleted(String id);
    }
}

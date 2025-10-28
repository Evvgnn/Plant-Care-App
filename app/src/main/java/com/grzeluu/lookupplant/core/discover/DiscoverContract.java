package com.grzeluu.lookupplant.core.discover;

import com.grzeluu.lookupplant.base.BaseListenerContract;
import com.grzeluu.lookupplant.base.BasePresenterContract;
import com.grzeluu.lookupplant.base.BaseViewContract;
import com.grzeluu.lookupplant.model.Plant;

import java.util.List;

public interface DiscoverContract {
    interface View extends BaseViewContract {
        void setDiscoverPlantList(List<Plant> plantList);
    }

    interface Presenter extends BasePresenterContract {
        void getAllPlants();

        void getMatchingPlants(String regex);
    }

    interface Interactor {
        void performGetAllPlants();

        void performGetMatchingPlants(String regex);
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(List<Plant> plantList);

        void onFailure(String message);
    }
}

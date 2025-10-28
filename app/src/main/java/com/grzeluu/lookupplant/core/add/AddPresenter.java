package com.grzeluu.lookupplant.core.add;

import android.content.Context;
import android.util.Log;

import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.base.BasePresenter;
import com.grzeluu.lookupplant.model.UserPlant;

public class AddPresenter extends BasePresenter
        implements AddContract.Presenter, AddContract.Listener {

    private AddContract.View addView;
    private AddContract.Interactor addInteractor;

    public AddPresenter(AddContract.View addView, Context context) {
        super(addView);
        this.addView = addView;
        this.addInteractor = new AddInteractor(this, context);
    }


    @Override
    public void addPlant(UserPlant plant, String latinName, String type, String description) {
        if (isPlantCorrect(plant))
            addInteractor.performAddPlant(plant, latinName, type, description);
    }

    private boolean isPlantCorrect(UserPlant plant) {
        if (!plant.getName().isEmpty())
            return true;

        addView.setNameError(R.string.this_field_cant_be_empty);
        return false;
    }

    @Override
    public void onSuccess(int message, UserPlant plant) {
        addView.plantAdded(message, plant);
    }

    @Override
    public void onFailure(String message) {
        addView.showMessage(message);
        Log.e("ADD_PLANT", "Failed: " + message);
    }
}

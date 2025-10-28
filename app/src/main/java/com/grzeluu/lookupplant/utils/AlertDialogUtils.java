package com.grzeluu.lookupplant.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.core.myplants.MyPlantsContract;
import com.grzeluu.lookupplant.model.UserPlant;

public class AlertDialogUtils {
    static public void showDeletePlantDialog(Context context, MyPlantsContract.Presenter presenter, UserPlant plant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_plant);
        builder.setMessage(R.string.delete_plant_question);
        builder.setPositiveButton(R.string.confirm, (dialog, id) -> {
            presenter.deletePlant(plant);
        });

        builder.setNeutralButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

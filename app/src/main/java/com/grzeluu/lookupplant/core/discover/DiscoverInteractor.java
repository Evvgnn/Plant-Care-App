package com.grzeluu.lookupplant.core.discover;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grzeluu.lookupplant.model.Plant;

import java.util.ArrayList;
import java.util.List;

public class DiscoverInteractor implements DiscoverContract.Interactor {

    DiscoverContract.Listener discoverListener;

    public DiscoverInteractor(DiscoverContract.Listener discoverListener) {
        this.discoverListener = discoverListener;
    }

    @Override
    public void performGetAllPlants() {
        discoverListener.onStart();

        final List<Plant> plantList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("Plants")
                .limitToFirst(50)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Plant plant = snapshot.getValue(Plant.class);
                        if (plant != null) {
                            plantList.add(plant);
                            discoverListener.onSuccess(new ArrayList<>(plantList));
                        }
                        discoverListener.onEnd();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Plant changed = snapshot.getValue(Plant.class);
                        if (changed == null) {
                            discoverListener.onEnd();
                            return;
                        }
                        for (int i = 0; i < plantList.size(); i++) {
                            Plant p = plantList.get(i);
                            if (p != null && p.getId() != null && p.getId().equals(changed.getId())) {
                                plantList.set(i, changed);
                                discoverListener.onSuccess(new ArrayList<>(plantList));
                                break;
                            }
                        }
                        discoverListener.onEnd();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Plant removed = snapshot.getValue(Plant.class);
                        if (removed != null) {
                            for (int i = 0; i < plantList.size(); i++) {
                                Plant p = plantList.get(i);
                                if (p != null && p.getId() != null && p.getId().equals(removed.getId())) {
                                    plantList.remove(i);
                                    discoverListener.onSuccess(new ArrayList<>(plantList));
                                    break;
                                }
                            }
                        }
                        discoverListener.onEnd();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        discoverListener.onEnd();
                        discoverListener.onFailure(error.getMessage());
                    }
                });
    }


    @Override
    public void performGetMatchingPlants(String regex) {
        discoverListener.onStart();
        List<Plant> plantList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Plants")
                .orderByChild("commonName")
                .startAt(regex)
                .endAt(regex + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Plant plant = ds.getValue(Plant.class);
                            plantList.add(plant);
                        }
                        discoverListener.onEnd();
                        discoverListener.onSuccess(plantList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        discoverListener.onEnd();
                        discoverListener.onFailure(error.getMessage());
                    }
                });
    }
}
